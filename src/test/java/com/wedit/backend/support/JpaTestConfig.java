package com.wedit.backend.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DataJpaTest 슬라이스는 @SpringBootApplication 을 로딩하지 않으므로
 * @EnableJpaAuditing 이 비활성화 됩니다.
 * 이 설정을 @Import 하여 createdAt / updatedAt 이 정상 동작하도록 합니다.
 */
@TestConfiguration
@EnableJpaAuditing
public class JpaTestConfig {

    /**
     * JPA 통합 테스트에서 공통으로 사용하는 합성 애노테이션.
     * 클래스에 @JpaIntegrationTest 를 붙이면 @DataJpaTest + JpaTestConfig 가 함께 적용됩니다.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @DataJpaTest
    @Import(JpaTestConfig.class)
    // application.properties 의 ddl-auto=none 이 @SpringBootTest 전용이므로
    // @DataJpaTest(H2) 컨텍스트에서는 create-drop 으로 명시적으로 재지정한다
    @TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
    public @interface JpaIntegrationTest {
    }
}
