package org.springframework.data.remote.repository.support;

import org.junit.jupiter.api.Test;
import org.springframework.data.remote.annotation.RemoteResource;

import static org.junit.jupiter.api.Assertions.*;

class RemoteEntityInformationTest {

    @Test
    void shouldDeriveResourcePathFromClassName() {
        RemoteEntityInformation<SimpleEntity, Long> info = new RemoteEntityInformation<>(SimpleEntity.class,
                Long.class);

        assertEquals("/simpleentitys", info.getResourcePath());
        assertEquals("simpleentitys", info.getResourceName());
        assertEquals("id", info.getIdField());
        assertNull(info.getBaseUrl());
        assertFalse(info.hasBaseUrlOverride());
    }

    @Test
    void shouldUseAnnotationPath() {
        RemoteEntityInformation<AnnotatedEntity, Long> info = new RemoteEntityInformation<>(AnnotatedEntity.class,
                Long.class);

        assertEquals("/api/users", info.getResourcePath());
        assertEquals("api/users", info.getResourceName());
    }

    @Test
    void shouldUseAnnotationBaseUrl() {
        RemoteEntityInformation<AnnotatedWithBaseUrl, Long> info = new RemoteEntityInformation<>(
                AnnotatedWithBaseUrl.class, Long.class);

        assertEquals("https://other-api.com", info.getBaseUrl());
        assertTrue(info.hasBaseUrlOverride());
    }

    @Test
    void shouldUseAnnotationIdField() {
        RemoteEntityInformation<AnnotatedWithIdField, String> info = new RemoteEntityInformation<>(
                AnnotatedWithIdField.class, String.class);

        assertEquals("uuid", info.getIdField());
    }

    // Test entities
    static class SimpleEntity {
        private Long id;
    }

    @RemoteResource(path = "/api/users")
    static class AnnotatedEntity {
        private Long id;
    }

    @RemoteResource(path = "/orders", baseUrl = "https://other-api.com")
    static class AnnotatedWithBaseUrl {
        private Long id;
    }

    @RemoteResource(path = "/documents", idField = "uuid")
    static class AnnotatedWithIdField {
        private String uuid;
    }
}
