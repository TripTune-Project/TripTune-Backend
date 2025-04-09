package com.triptune.email.dto.request;

import java.util.Map;

public record EmailTemplateRequest(
        String subject,
        String recipientEmail,
        Map<String, String> emailValues,
        String templateName
) {}
