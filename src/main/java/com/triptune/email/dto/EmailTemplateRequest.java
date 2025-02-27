package com.triptune.email.dto;

import java.util.Map;

public record EmailTemplateRequest(
        String subject,
        String recipientEmail,
        Map<String, String> emailValues,
        String templateName
) {}
