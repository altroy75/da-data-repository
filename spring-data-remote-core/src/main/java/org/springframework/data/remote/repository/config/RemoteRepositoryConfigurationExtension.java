package org.springframework.data.remote.repository.config;

import org.springframework.data.remote.repository.RemoteRepository;
import org.springframework.data.remote.repository.support.RemoteRepositoryFactoryBean;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

import java.util.Collection;
import java.util.Collections;

/**
 * Configuration extension for remote repositories.
 * Provides metadata about the remote repository module to the Spring Data
 * infrastructure.
 */
public class RemoteRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    private static final String MODULE_NAME = "Remote";
    private static final String MODULE_PREFIX = "remote";

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    protected String getModulePrefix() {
        return MODULE_PREFIX;
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return RemoteRepositoryFactoryBean.class.getName();
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(RemoteRepository.class);
    }
}
