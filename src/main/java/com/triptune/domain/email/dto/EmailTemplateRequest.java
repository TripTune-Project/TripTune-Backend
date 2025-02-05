package com.triptune.domain.email.dto;

import java.util.Map;

public record EmailTemplateRequest(
        String subject,
        String recipientEmail,
        Map<String, String> emailValues,
        String templateName
) {}
