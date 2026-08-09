package com.c8software.spring.ai.core.security;

import com.c8software.spring.ai.core.annotation.SensitiveType;

/** SPI for masking sensitive values. */
public interface SensitiveMasker {
    /** Masks a value. */
    Object mask(Object value, SensitiveType sensitiveType);
}
