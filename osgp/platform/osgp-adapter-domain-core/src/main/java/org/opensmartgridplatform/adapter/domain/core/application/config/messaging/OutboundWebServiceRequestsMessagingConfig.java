/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.domain.core.application.config.messaging;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLException;

import org.opensmartgridplatform.shared.application.config.jms.JmsConfigurationNames;
import org.opensmartgridplatform.shared.application.config.messaging.DefaultJmsConfiguration;
import org.opensmartgridplatform.shared.application.config.messaging.JmsConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;

/**
 * Configuration class for outbound web service requests
 */
@Configuration
public class OutboundWebServiceRequestsMessagingConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundWebServiceRequestsMessagingConfig.class);

    private JmsConfigurationFactory jmsConfigurationFactory;

    public OutboundWebServiceRequestsMessagingConfig(final Environment environment,
            final DefaultJmsConfiguration defaultJmsConfiguration) throws SSLException {
        this.jmsConfigurationFactory = new JmsConfigurationFactory(environment, defaultJmsConfiguration,
                JmsConfigurationNames.JMS_COMMON_DOMAIN_TO_WS_REQUESTS);
    }

    @Bean(destroyMethod = "stop", name = "domainCoreOutboundWebServiceRequestsConnectionFactory")
    public ConnectionFactory connectionFactory() {
        LOGGER.info("Initializing domainCoreOutboundWebServiceRequestsConnectionFactory bean.");
        return this.jmsConfigurationFactory.getPooledConnectionFactory();
    }

    @Bean(name = "domainCoreOutboundWebServiceRequestsJmsTemplate")
    public JmsTemplate jmsTemplate() {
        return this.jmsConfigurationFactory.initJmsTemplate();
    }
}
