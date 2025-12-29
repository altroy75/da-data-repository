package org.springframework.data.remote.transport;

/**
 * Enumeration of supported transport operations.
 * These map to standard CRUD operations and are translated
 * by transport implementations into protocol-specific calls.
 */
public enum TransportOperation {

    /**
     * Find a single entity by its identifier.
     * REST: GET /resource/{id}
     * gRPC: GetById(id)
     */
    FIND_BY_ID,

    /**
     * Find all entities of a type.
     * REST: GET /resource
     * gRPC: GetAll()
     */
    FIND_ALL,

    /**
     * Find entities matching specific criteria.
     * REST: GET /resource?param=value
     * gRPC: Query(criteria)
     */
    QUERY,

    /**
     * Save (create or update) an entity.
     * REST: POST/PUT /resource
     * gRPC: Save(entity)
     */
    SAVE,

    /**
     * Delete an entity by its identifier.
     * REST: DELETE /resource/{id}
     * gRPC: Delete(id)
     */
    DELETE,

    /**
     * Check if an entity exists.
     * REST: HEAD /resource/{id} or GET with existence check
     * gRPC: Exists(id)
     */
    EXISTS,

    /**
     * Count all entities.
     * REST: GET /resource/count or HEAD with count header
     * gRPC: Count()
     */
    COUNT
}
