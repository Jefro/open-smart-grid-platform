/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.oslp.elster.application.config;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alliander.osgp.adapter.protocol.oslp.elster.domain.repositories.OslpDeviceRepository;
import com.alliander.osgp.shared.application.config.AbstractPersistenceConfig;
import com.alliander.osgp.shared.infra.db.DefaultConnectionPoolFactory;
import com.zaxxer.hikari.HikariDataSource;

/**
 * An application context Java configuration class.
 */
@EnableJpaRepositories(entityManagerFactoryRef = "oslpEntityManagerFactory", basePackageClasses = {
        OslpDeviceRepository.class })
@Configuration
@EnableTransactionManagement()
@PropertySource("classpath:osgp-adapter-protocol-oslp-elster.properties")
@PropertySource(value = "file:${osgp/Global/config}", ignoreResourceNotFound = true)
@PropertySource(value = "file:${osgp/AdapterProtocolOslpElster/config}", ignoreResourceNotFound = true)
public class OslpPersistenceConfig extends AbstractPersistenceConfig {

    @Value("${db.username.oslp}")
    private String username;

    @Value("${db.password.oslp}")
    private String password;

    @Value("${db.host.oslp}")
    private String databaseHost;

    @Value("${db.port.oslp}")
    private int databasePort;

    @Value("${db.name.oslp}")
    private String databaseName;

    @Value("${entitymanager.packages.to.scan.oslp}")
    private String entitymanagerPackagesToScan;

    private HikariDataSource dataSourceOslp;

    public OslpPersistenceConfig() {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    /**
     * Method for creating the Data Source.
     *
     * @return DataSource
     */
    public DataSource getDataSourceOslp() {
        if (this.dataSourceOslp == null) {
            final DefaultConnectionPoolFactory.Builder builder = super.builder().withUsername(this.username)
                    .withPassword(this.password).withDatabaseHost(this.databaseHost).withDatabasePort(this.databasePort)
                    .withDatabaseName(this.databaseName);
            final DefaultConnectionPoolFactory factory = builder.build();
            this.dataSourceOslp = factory.getDefaultConnectionPool();
        }
        return this.dataSourceOslp;
    }

    @Override
    @Bean
    public JpaTransactionManager transactionManager() {
        return super.transactionManager();
    }

    @Bean(initMethod = "migrate")
    public Flyway oslpFlyway() {
        return super.createFlyway(this.getDataSourceOslp());
    }

    @Override
    @Bean(name = "oslpEntityManagerFactory")
    @DependsOn("oslpFlyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        return super.entityManagerFactory("OSGP_PROTOCOL_ADAPTER_OSLP_SETTINGS", this.getDataSourceOslp(),
                this.entitymanagerPackagesToScan);
    }

    @Override
    @PreDestroy
    public void destroyDataSource() {
        if (this.dataSourceOslp != null) {
            this.dataSourceOslp.close();
        }
    }

}
