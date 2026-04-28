package com.wedit.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

class LocalConfigContractTest {

    private static final Path LOCAL_CONFIG = Path.of("config/src/main/resources/application-local.yml");
    private static final String LOCAL_DB_URL = "jdbc:mysql://localhost:3306/wedit?"
            + "serverTimezone=Asia/Seoul&characterEncoding=UTF-8&sessionVariables="
            + "sql_mode='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'";

    @Test
    void localProfileDefinesMysqlDatasource() {
        Properties properties = loadLocalConfig();

        assertEquals("${LOCAL_DB_URL:" + LOCAL_DB_URL + "}", properties.getProperty("spring.datasource.url"));
        assertEquals("${LOCAL_DB_USERNAME:root}", properties.getProperty("spring.datasource.username"));
        assertEquals("${LOCAL_DB_PASSWORD:}", properties.getProperty("spring.datasource.password"));
        assertFalse(properties.getProperty("spring.datasource.password").contains("ohw62459930"));
        assertEquals("com.mysql.cj.jdbc.Driver", properties.getProperty("spring.datasource.driver-class-name"));
    }

    @Test
    void localDatasourcePropertiesArePresent() {
        Properties properties = loadLocalConfig();

        assertRequiredProperty(properties, "spring.datasource.url");
        assertRequiredProperty(properties, "spring.datasource.username");
        assertRequiredProperty(properties, "spring.datasource.password");
        assertRequiredProperty(properties, "spring.datasource.driver-class-name");
    }

    private static Properties loadLocalConfig() {
        assertTrue(Files.exists(LOCAL_CONFIG), "application-local.yml must exist in the config submodule");

        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new FileSystemResource(LOCAL_CONFIG));
        Properties properties = factory.getObject();

        assertTrue(properties != null, "application-local.yml must be readable as YAML properties");
        return properties;
    }

    private static void assertRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);

        assertTrue(value != null, key + " must be configured");
        assertFalse(value.isBlank(), key + " must not be blank");
    }
}
