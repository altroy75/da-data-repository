package org.springframework.data.remote.repository.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.remote.transport.TransportClient;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * Factory bean for creating remote repository instances.
 * This is the bean that gets registered in the Spring context for each
 * repository interface.
 *
 * @param <T>  the repository type
 * @param <S>  the entity type
 * @param <ID> the entity's identifier type
 */
public class RemoteRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    private TransportClient<?> transportClient;

    /**
     * Creates a new factory bean for the given repository interface.
     *
     * @param repositoryInterface the repository interface class
     */
    public RemoteRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Autowired
    public void setTransportClient(TransportClient<?> transportClient) {
        this.transportClient = transportClient;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new RemoteRepositoryFactory(transportClient);
    }
}
