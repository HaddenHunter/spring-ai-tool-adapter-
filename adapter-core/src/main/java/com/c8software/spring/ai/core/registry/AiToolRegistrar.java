package com.c8software.spring.ai.core.registry;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolContextKey;
import com.c8software.spring.ai.core.annotation.AiToolParam;
import com.c8software.spring.ai.core.annotation.AiToolSensitive;
import com.c8software.spring.ai.core.annotation.Sensitive;
import com.c8software.spring.ai.core.annotation.ToolGroup;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.definition.ToolMetadata;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.exception.AiToolRegistrationException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/** BeanPostProcessor that scans @AiTool methods and registers definitions. */
public class AiToolRegistrar implements BeanPostProcessor {
    private final ToolRegistry registry;
    private final AiToolProperties properties;
    private final ToolGovernanceAnnotationProcessor governanceAnnotationProcessor;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AiToolRegistrar(ToolRegistry registry, AiToolProperties properties) {
        this(registry, properties, new DefaultToolGovernanceAnnotationProcessor());
    }

    public AiToolRegistrar(ToolRegistry registry, AiToolProperties properties,
                           ToolGovernanceAnnotationProcessor governanceAnnotationProcessor) {
        this.registry = registry;
        this.properties = properties;
        this.governanceAnnotationProcessor = governanceAnnotationProcessor == null
                ? new DefaultToolGovernanceAnnotationProcessor()
                : governanceAnnotationProcessor;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> registerMethod(bean, targetClass, method),
                method -> method.isAnnotationPresent(AiTool.class));
        return bean;
    }

    private void registerMethod(Object bean, Class<?> targetClass, Method method) {
        AiTool aiTool = method.getAnnotation(AiTool.class);
        if (!properties.isToolEnabled(aiTool.name(), aiTool.enabled())) {
            return;
        }
        ToolGroup methodGroup = method.getAnnotation(ToolGroup.class);
        ToolGroup classGroup = targetClass.getAnnotation(ToolGroup.class);
        String group = methodGroup != null ? methodGroup.value() : classGroup != null ? classGroup.value() : "default";
        ToolMetadata.Builder metadataBuilder = ToolMetadata.builder()
                .group(group)
                .enabled(true)
                .timeoutMillis(properties.getDefaultTimeoutMillis());
        ToolMetadata metadata = governanceAnnotationProcessor.enrich(method, aiTool, metadataBuilder).build();
        registry.register(new ReflectionToolDefinition(aiTool.name(), aiTool.description(),
                buildParameters(method, aiTool.paramDescriptions()), metadata, bean, method, buildHandle(method)));
    }

    private List<ToolParameter> buildParameters(Method method, String[] descriptions) {
        List<ToolParameter> result = new ArrayList<ToolParameter>();
        String[] discoveredNames = parameterNameDiscoverer.getParameterNames(method);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Sensitive sensitive = parameter.getAnnotation(Sensitive.class);
            AiToolSensitive aiToolSensitive = parameter.getAnnotation(AiToolSensitive.class);
            AiToolParam aiToolParam = parameter.getAnnotation(AiToolParam.class);
            AiToolContextKey contextKey = parameter.getAnnotation(AiToolContextKey.class);
            String name = discoveredNames != null ? discoveredNames[i] : "arg" + i;
            String description = aiToolParam != null ? aiToolParam.description()
                    : descriptions != null && descriptions.length > i ? descriptions[i] : name;
            boolean required = aiToolParam == null ? isRequired(parameter) : aiToolParam.required();
            String defaultValue = aiToolParam == null || aiToolParam.defaultValue().trim().isEmpty()
                    ? null
                    : aiToolParam.defaultValue();
            result.add(new ToolParameter(name, description, parameter.getType(), parameter.getParameterizedType(),
                    required, defaultValue, sensitiveType(sensitive, aiToolSensitive),
                    validationValue(parameter, "Min"), validationValue(parameter, "Max"),
                    contextKey == null ? null : contextKey.store(), contextKey != null && contextKey.confirmed()));
        }
        return result;
    }

    private com.c8software.spring.ai.core.annotation.SensitiveType sensitiveType(Sensitive sensitive,
                                                                                 AiToolSensitive aiToolSensitive) {
        if (aiToolSensitive != null) {
            return aiToolSensitive.type();
        }
        return sensitive == null ? null : sensitive.value();
    }

    private boolean isRequired(Parameter parameter) {
        if (parameter.getType().isPrimitive()) {
            return true;
        }
        for (Annotation annotation : parameter.getAnnotations()) {
            String name = annotation.annotationType().getName();
            if ("javax.validation.constraints.NotNull".equals(name) || "jakarta.validation.constraints.NotNull".equals(name)) {
                return true;
            }
            if ("org.springframework.lang.Nullable".equals(name) || "javax.annotation.Nullable".equals(name)
                    || "jakarta.annotation.Nullable".equals(name)) {
                return false;
            }
        }
        return true;
    }

    private String validationValue(Parameter parameter, String simpleName) {
        for (Annotation annotation : parameter.getAnnotations()) {
            String name = annotation.annotationType().getName();
            if (name.equals("javax.validation.constraints." + simpleName)
                    || name.equals("jakarta.validation.constraints." + simpleName)) {
                try {
                    Object value = annotation.annotationType().getMethod("value").invoke(annotation);
                    return String.valueOf(value);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private MethodHandle buildHandle(Method method) {
        try {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException ex) {
            throw new AiToolRegistrationException("AIT_REG_003", "Cannot access tool method: " + method.getName(), ex);
        }
    }
}
