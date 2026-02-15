package com.a.config.jpa;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnBean(DataSource.class)
@EnableJpaRepositories(basePackages = "com.a.repository")
@EntityScan(basePackages = "com.a.entity")
public class JpaConfig {
}
