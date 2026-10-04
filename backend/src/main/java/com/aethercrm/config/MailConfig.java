package com.aethercrm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Mail is configured via spring.mail.* properties.
 * JavaMailSender is auto-configured when spring.mail.host is set.
 * EmailService uses ObjectProvider and falls back to LOG_ONLY mode.
 */
@Configuration
@ConditionalOnProperty(name = "aethercrm.email.enabled", havingValue = "true", matchIfMissing = false)
public class MailConfig {
}
