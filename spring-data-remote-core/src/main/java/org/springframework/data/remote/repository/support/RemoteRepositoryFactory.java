package org.springframework.data.remote.repository.support;

import jakarta.persistence.Id;
import org.springframework.data.remote.transport.TransportClient;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

/**
 * Factory for creating remote repository instances.
 * This is the core factory that Spring Data uses to create repository proxies.
 */
public class RemoteRepositoryFactory extends RepositoryFactorySupport {

    private final TransportClient<?> transportClient;

    public RemoteRepositoryFactory(TransportClient<?> transportClient) {
        this.transportClient = transportClient;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T, ID> EntityInformation<T, ID> getEntityInformation(@NonNull Class<T> domainClass) {
        return new RemoteEntityInformationAdapter<>(domainClass);
    }

    @Override
    @NonNull
    protected Object getTargetRepository(@NonNull RepositoryInformation metadata) {
        RemoteEntityInformation<?, ?> entityInfo = new RemoteEntityInformation<>(
                metadata.getDomainType(),
                metadata.getIdType());
        return new SimpleRemoteRepository<>(entityInfo, transportClient);
    }

    @Override
    @NonNull
    protected Class<?> getRepositoryBaseClass(@NonNull RepositoryMetadata metadata) {
        return SimpleRemoteRepository.class;
    }

    /**
     * Adapter to convert RemoteEntityInformation to Spring Data's
     * EntityInformation.
     */
    private static class RemoteEntityInformationAdapter<T, ID> implements EntityInformation<T, ID> {

        private final Class<T> domainClass;

        RemoteEntityInformationAdapter(Class<T> domainClass) {
            this.domainClass = domainClass;
        }

        @Override
        public boolean isNew(@NonNull T entity) {
            // Consider an entity new if it has no ID
            try {
                ID id = getId(entity);
                return id == null;
            } catch (Exception e) {
                return true;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public ID getId(@NonNull T entity) {
            try {
                for (Field field : entity.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class) ||
                            field.isAnnotationPresent(Id.class)) {
                        field.setAccessible(true);
                        return (ID) field.get(entity);
                    }
                }
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                return (ID) idField.get(entity);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        public Class<ID> getIdType() {
            try {
                for (Field field : domainClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class) ||
                            field.isAnnotationPresent(Id.class)) {
                        return (Class<ID>) field.getType();
                    }
                }
                return (Class<ID>) domainClass.getDeclaredField("id").getType();
            } catch (Exception e) {
                @SuppressWarnings("unchecked")
                Class<ID> objectClass = (Class<ID>) Object.class;
                return objectClass;
            }
        }

        @Override
        @NonNull
        public Class<T> getJavaType() {
            return domainClass;
        }
    }
}
