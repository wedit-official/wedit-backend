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

    @Test
    void localProfileDefinesMysqlDatasource() {
        Properties properties = loadLocalConfig();

        assertTrue(properties.getProperty("spring.datasource.url").startsWith("${LOCAL_DB_URL:jdbc:mysql://localhost:3306/wedit"));
        assertEquals("${LOCAL_DB_USERNAME:root}", properties.getProperty("spring.datasource.username"));
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

    @Test
    void localDatasourceCanBeOverriddenByEnvironment() throws Exception {
        String localConfig = Files.readString(LOCAL_CONFIG);

        assertTrue(localConfig.contains("${LOCAL_DB_URL:"));
        assertTrue(localConfig.contains("${LOCAL_DB_USERNAME:"));
        assertTrue(localConfig.contains("${LOCAL_DB_PASSWORD:}"));
        assertFalse(localConfig.contains("ohw62459930"));
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
