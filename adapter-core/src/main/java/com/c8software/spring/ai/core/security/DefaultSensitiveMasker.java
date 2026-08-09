package com.c8software.spring.ai.core.security;

import com.c8software.spring.ai.core.annotation.SensitiveType;

/** Default regex-free sensitive value masker. */
public class DefaultSensitiveMasker implements SensitiveMasker {
    public Object mask(Object value, SensitiveType sensitiveType) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (text.length() <= 2) {
            return "**";
        }
        if (SensitiveType.MOBILE.equals(sensitiveType) && text.length() >= 7) {
            return text.substring(0, 3) + "****" + text.substring(text.length() - 4);
        }
        if (SensitiveType.ID_CARD.equals(sensitiveType) && text.length() >= 8) {
            return text.substring(0, 4) + "********" + text.substring(text.length() - 4);
        }
        if (SensitiveType.BANK_CARD.equals(sensitiveType) && text.length() >= 8) {
            return "**** **** **** " + text.substring(text.length() - 4);
        }
        return text.charAt(0) + "***" + text.charAt(text.length() - 1);
    }
}
