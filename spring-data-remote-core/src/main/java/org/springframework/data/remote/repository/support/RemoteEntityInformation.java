package org.springframework.data.remote.repository.support;

import org.springframework.data.remote.annotation.RemoteResource;

import java.util.Locale;

/**
 * Holds metadata about a remote entity.
 * Extracted from the entity class and its annotations.
 */
public class RemoteEntityInformation<T, ID> {

    private final Class<T> entityType;
    private final Class<ID> idType;
    private final String resourcePath;
    private final String baseUrl;
    private final String idField;

    public RemoteEntityInformation(Class<T> entityType, Class<ID> idType) {
        this.entityType = entityType;
        this.idType = idType;

        RemoteResource annotation = entityType.getAnnotation(RemoteResource.class);
        if (annotation != null) {
            this.resourcePath = annotation.path().isEmpty()
                    ? deriveResourcePath(entityType)
                    : annotation.path();
            this.baseUrl = annotation.baseUrl().isEmpty() ? null : annotation.baseUrl();
            this.idField = annotation.idField();
        } else {
            this.resourcePath = deriveResourcePath(entityType);
            this.baseUrl = null;
            this.idField = "id";
        }
    }

    /**
     * Derives the resource path from the entity class name.
     * Converts to lowercase and pluralizes (simple 's' suffix).
     */
    private String deriveResourcePath(Class<?> entityType) {
        String simpleName = entityType.getSimpleName();
        return "/" + simpleName.toLowerCase(Locale.ROOT) + "s";
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public Class<ID> getIdType() {
        return idType;
    }

    /**
     * Returns the resource path for API calls.
     * This is the path segment appended to the base URL.
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Returns the resource name (path without leading slash).
     */
    public String getResourceName() {
        return resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
    }

    /**
     * Returns the optional base URL override for this entity.
     * May be null if using the transport's default base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the name of the ID field in JSON responses.
     */
    public String getIdField() {
        return idField;
    }

    /**
     * Checks if this entity has a base URL override.
     */
    public boolean hasBaseUrlOverride() {
        return baseUrl != null && !baseUrl.isEmpty();
    }
}
