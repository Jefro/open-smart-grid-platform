/**
 * Copyright 2020 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.kafka.logging.config;

import org.opensmartgridplatform.adapter.kafka.da.avro.GridMeasurementPublishedEvent;
import org.opensmartgridplatform.shared.application.config.kafka.AbstractKafkaConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@Conditional(GridMeasurementPublishedEventLoggingEnabled.class)
public class GridMeasurementPublishedEventKafkaConsumerConfig
        extends AbstractKafkaConsumerConfig<String, GridMeasurementPublishedEvent> {

    public GridMeasurementPublishedEventKafkaConsumerConfig(final Environment environment,
            @Value("${grid.measurement.published.event.kafka.common.properties.prefix}") final String propertiesPrefix,
            @Value("${grid.measurement.published.event.kafka.topic}") final String topic,
            @Value("${grid.measurement.published.event.kafka.consumer.concurrency}") final int concurrency,
            @Value("${grid.measurement.published.event.kafka.consumer.poll.timeout}") final int pollTimeout) {

        super(environment, propertiesPrefix, topic, concurrency, pollTimeout);
    }

    @Bean("gridMeasurementPublishedEventConsumerFactory")
    @Override
    public ConsumerFactory<String, GridMeasurementPublishedEvent> consumerFactory() {
        return this.getConsumerFactory();
    }

    @Bean("gridMeasurementPublishedEventKafkaListenerContainerFactory")
    @Override
    public ConcurrentKafkaListenerContainerFactory<String, GridMeasurementPublishedEvent> kafkaListenerContainerFactory() {
        return this.getKafkaListenerContainerFactory();
    }

}
