package com.digitalize.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Quando DATABASE_URL existe (Neon, Render), ela substitui as propriedades
 * spring.datasource.* do application.properties.
 */
@Configuration
@ConditionalOnExpression("!'${DATABASE_URL:}'.isBlank()")
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        DatabaseUrl url = DatabaseUrl.parse(databaseUrl);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url.jdbcUrl());
        dataSource.setUsername(url.usuario());
        dataSource.setPassword(url.senha());
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }
}
