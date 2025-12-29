package org.springframework.data.remote.repository.support;

import jakarta.persistence.Id;
import org.springframework.data.remote.repository.RemoteRepository;
import org.springframework.data.remote.transport.TransportClient;
import org.springframework.data.remote.transport.TransportOperation;
import org.springframework.data.remote.transport.TransportRequest;
import org.springframework.data.remote.transport.TransportResponse;
import org.springframework.data.remote.transport.TransportException;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of {@link RemoteRepository} that delegates to a
 * {@link TransportClient}.
 * This class provides the actual implementation of CRUD operations by building
 * transport
 * requests and processing responses.
 *
 * @param <T>  the entity type
 * @param <ID> the entity's identifier type
 */
public class SimpleRemoteRepository<T, ID> implements RemoteRepository<T, ID> {

    private final RemoteEntityInformation<T, ID> entityInformation;
    private final TransportClient<?> transportClient;

    public SimpleRemoteRepository(RemoteEntityInformation<T, ID> entityInformation,
            TransportClient<?> transportClient) {
        this.entityInformation = entityInformation;
        this.transportClient = transportClient;
    }

    @Override
    @NonNull
    public <S extends T> S save(@NonNull S entity) {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.SAVE)
                .resourceName(entityInformation.getResourceName())
                .entityType(entityInformation.getEntityType())
                .payload(entity)
                .id(extractId(entity))
                .build();

        @SuppressWarnings("unchecked")
        TransportResponse<S> response = (TransportResponse<S>) transportClient.execute(
                request, entityInformation.getEntityType());

        return handleResponse(response, "save");
    }

    @Override
    @NonNull
    public <S extends T> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    @NonNull
    public Optional<T> findById(@NonNull ID id) {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.FIND_BY_ID)
                .resourceName(entityInformation.getResourceName())
                .entityType(entityInformation.getEntityType())
                .id(id)
                .build();

        TransportResponse<T> response = transportClient.execute(request, entityInformation.getEntityType());

        if (!response.isSuccess() && response.getStatusCode() == 404) {
            return Optional.empty();
        }

        return response.getBody();
    }

    @Override
    public boolean existsById(@NonNull ID id) {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.EXISTS)
                .resourceName(entityInformation.getResourceName())
                .entityType(entityInformation.getEntityType())
                .id(id)
                .build();

        TransportResponse<Boolean> response = transportClient.execute(request, Boolean.class);
        return response.getBody().orElse(false);
    }

    @Override
    @NonNull
    public Iterable<T> findAll() {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.FIND_ALL)
                .resourceName(entityInformation.getResourceName())
                .entityType(entityInformation.getEntityType())
                .build();

        TransportResponse<List<T>> response = transportClient.executeForList(
                request, entityInformation.getEntityType());

        return response.getBody().orElse(List.of());
    }

    @Override
    @NonNull
    public Iterable<T> findAllById(@NonNull Iterable<ID> ids) {
        List<T> result = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.COUNT)
                .resourceName(entityInformation.getResourceName())
                .entityType(entityInformation.getEntityType())
                .build();

        TransportResponse<Long> response = transportClient.execute(request, Long.class);
        return response.getBody().orElse(0L);
    }

    @Override
    public void deleteById(@NonNull ID id) {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.DELETE)
                .resourceName(entityInformation.getResourceName())
                .entityType(entityInformation.getEntityType())
                .id(id)
                .build();

        TransportResponse<Void> response = transportClient.execute(request, Void.class);

        if (!response.isSuccess() && response.getStatusCode() != 404) {
            throw new TransportException(
                    response.getErrorMessage().orElse("Delete failed"),
                    response.getStatusCode(),
                    entityInformation.getResourceName(),
                    TransportOperation.DELETE);
        }
    }

    @Override
    public void delete(@NonNull T entity) {
        ID id = extractId(entity);
        if (id != null) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends ID> ids) {
        for (ID id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        // Fetch all and delete each - remote APIs typically don't support bulk delete
        for (T entity : findAll()) {
            delete(entity);
        }
    }

    @Override
    @NonNull
    public T refresh(@NonNull T entity) {
        ID id = extractId(entity);
        if (id == null) {
            throw new IllegalArgumentException("Entity must have an ID to be refreshed");
        }
        return findById(id).orElseThrow(() -> new TransportException(
                "Entity not found for refresh",
                404,
                entityInformation.getResourceName(),
                TransportOperation.FIND_BY_ID));
    }

    /**
     * Extracts the ID from an entity using reflection.
     */
    @SuppressWarnings("unchecked")
    private ID extractId(T entity) {
        if (entity == null) {
            return null;
        }

        try {
            // Try to find @Id annotated field (both Spring Data and Jakarta)
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class) ||
                        field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    return (ID) field.get(entity);
                }
            }

            // Fallback: try field named 'id'
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (ID) idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    @NonNull
    private <S> S handleResponse(TransportResponse<S> response, String operation) {
        if (!response.isSuccess()) {
            throw new TransportException(
                    response.getErrorMessage().orElse(operation + " failed"),
                    response.getStatusCode(),
                    entityInformation.getResourceName(),
                    TransportOperation.SAVE);
        }
        S body = response.getBody().orElse(null);
        if (body == null) {
            throw new TransportException(
                    "Expected non-null response for " + operation,
                    response.getStatusCode(),
                    entityInformation.getResourceName(),
                    TransportOperation.SAVE);
        }
        return body;
    }
}
