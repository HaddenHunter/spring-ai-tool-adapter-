package com.c8software.spring.ai.core.enterprise;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Tool marketplace entry with governance metadata. */
public final class ToolMarketplaceItem {
    private final String name;
    private final String group;
    private final String version;
    private final String visibility;
    private final String riskLevel;
    private final String requiredPermission;
    private final Set<String> samples;

    public ToolMarketplaceItem(String name, String group, String version, String visibility,
                               String riskLevel, String requiredPermission, Set<String> samples) {
        this.name = name;
        this.group = group;
        this.version = version;
        this.visibility = visibility;
        this.riskLevel = riskLevel;
        this.requiredPermission = requiredPermission;
        this.samples = Collections.unmodifiableSet(new LinkedHashSet<String>(
                samples == null ? Collections.<String>emptySet() : samples));
    }

    public String getName() { return name; }
    public String getGroup() { return group; }
    public String getVersion() { return version; }
    public String getVisibility() { return visibility; }
    public String getRiskLevel() { return riskLevel; }
    public String getRequiredPermission() { return requiredPermission; }
    public Set<String> getSamples() { return samples; }
}
