package org.springframework.data.remote.repository.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.remote.repository.support.RemoteRepositoryFactoryBean;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationDelegate;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * Registrar for enabling remote repositories.
 * Processes the {@link EnableRemoteRepositories} annotation and registers
 * repository beans.
 */
public class RemoteRepositoriesRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableRemoteRepositories.class.getName(), false));

        if (attributes == null) {
            return;
        }

        AnnotationRepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(
                importingClassMetadata,
                EnableRemoteRepositories.class,
                null,
                null,
                registry,
                null);

        RepositoryConfigurationExtension extension = new RemoteRepositoryConfigurationExtension();
        RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(
                configurationSource, null, null);

        delegate.registerRepositoriesIn(registry, extension);
    }
}
