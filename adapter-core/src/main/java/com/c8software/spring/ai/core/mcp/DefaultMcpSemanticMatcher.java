package com.c8software.spring.ai.core.mcp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DefaultMcpSemanticMatcher implements McpSemanticMatcher {

    public List<McpSkillMatch> match(McpSemanticRequest request, List<McpCapabilityDescriptor> candidates) {
        List<McpSkillMatch> matches = new ArrayList<McpSkillMatch>();
        String utterance = request == null || request.getUtterance() == null
                ? ""
                : request.getUtterance().toLowerCase(Locale.ENGLISH);
        for (McpCapabilityDescriptor candidate : candidates) {
            double score = score(utterance, candidate);
            if (score > 0.0d) {
                matches.add(new McpSkillMatch(candidate, score, "matched semantic capability tags"));
            }
        }
        matches.sort(Comparator.comparingDouble(McpSkillMatch::getScore).reversed());
        return matches;
    }

    private double score(String utterance, McpCapabilityDescriptor candidate) {
        if (utterance == null || utterance.trim().isEmpty()) {
            return 0.0d;
        }
        double score = 0.0d;
        for (String tag : candidate.getSemanticTags()) {
            if (tag != null && utterance.contains(tag.toLowerCase(Locale.ENGLISH))) {
                score += 1.0d;
            }
        }
        return score / Math.max(1, candidate.getSemanticTags().size());
    }
}
