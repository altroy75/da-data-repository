package org.springframework.data.remote.repository.config;

import org.springframework.context.annotation.Import;
import org.springframework.data.remote.repository.support.RemoteRepositoryFactoryBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable remote repository scanning.
 * Add this annotation to a configuration class to activate remote repository
 * support.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * &#64;Configuration
 * &#64;EnableRemoteRepositories(basePackages = "com.example.repository")
 * public class AppConfig {
 *     &#64;Bean
 *     public TransportClient transportClient() {
 *         return new RestTransportClient(restTransportConfig());
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RemoteRepositoriesRegistrar.class)
public @interface EnableRemoteRepositories {

    /**
     * Alias for {@link #basePackages()}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for remote repository interfaces.
     * If not specified, scanning will start from the package of the
     * configuration class that declares this annotation.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying packages to
     * scan.
     * The package of each class specified will be scanned.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Repository factory bean class to use.
     * Defaults to {@link RemoteRepositoryFactoryBean}.
     */
    Class<?> repositoryFactoryBeanClass() default RemoteRepositoryFactoryBean.class;
}
