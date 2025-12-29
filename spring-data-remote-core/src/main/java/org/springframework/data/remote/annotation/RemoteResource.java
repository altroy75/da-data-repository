package org.springframework.data.remote.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a remote resource entity.
 * This annotation configures how the entity maps to a remote API resource.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * &#64;RemoteResource(path = "/api/users")
 * public class User {
 *     &#64;Id
 *     private Long id;
 *     private String name;
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteResource {

    /**
     * The path segment for this resource.
     * This is appended to the base URL configured in the transport.
     * <p>
     * If not specified, defaults to the plural lowercase form of the entity name
     * (e.g., "User" becomes "users").
     *
     * @return the resource path
     */
    String path() default "";

    /**
     * The base URL for this specific resource.
     * Overrides the default base URL from the transport configuration.
     * <p>
     * This is useful when different entities are served by different APIs.
     *
     * @return the base URL, or empty to use the transport default
     */
    String baseUrl() default "";

    /**
     * The name of the ID field/property in the JSON response.
     * Defaults to "id".
     *
     * @return the ID field name
     */
    String idField() default "id";
}
