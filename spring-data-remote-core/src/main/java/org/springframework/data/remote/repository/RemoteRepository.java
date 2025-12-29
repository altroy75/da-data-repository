package org.springframework.data.remote.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Remote repository interface for accessing entities via remote API calls.
 * This is the main interface that user repositories should extend.
 *
 * <p>
 * Extends Spring Data's {@link CrudRepository} to provide standard CRUD
 * operations
 * that are executed via the configured transport layer (REST, gRPC, etc.).
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * public interface UserRepository extends RemoteRepository&lt;User, Long&gt; {
 *     // Standard CRUD methods are inherited:
 *     // - save(entity)
 *     // - findById(id)
 *     // - findAll()
 *     // - deleteById(id)
 *     // - count()
 *     // - existsById(id)
 * }
 * </pre>
 *
 * @param <T>  the entity type
 * @param <ID> the entity's identifier type
 */
@NoRepositoryBean
public interface RemoteRepository<T, ID> extends CrudRepository<T, ID> {

    /**
     * Refreshes an entity from the remote source.
     * This is useful when you suspect the local state may be stale.
     *
     * @param entity the entity to refresh (used to obtain the ID)
     * @return the refreshed entity from the remote source
     * @throws org.springframework.data.remote.transport.TransportException if the
     *                                                                      remote
     *                                                                      call
     *                                                                      fails
     */
    T refresh(T entity);
}
