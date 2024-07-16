package fi.metatavu.vp.deliveryinfo.persistence

import fi.metatavu.coroutine.CoroutineUtils
import io.quarkus.hibernate.reactive.panache.PanacheQuery
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.jboss.logging.Logger
import fi.metatavu.coroutine.CoroutineUtils.logSessionDetails

/**
 * Abstract base class for all Repository classes
 *
 * @author Jari Nykänen
 *
 * @param <T> entity type
 */
abstract class AbstractRepository<Entity, Id> : PanacheRepositoryBase<Entity, Id> {

    private val log: Logger = Logger.getLogger(CoroutineUtils::class.java)

    /**
     * Adds condition to the list of conditions. Note: parameters are not added here
     *
     * @param stringBuilder string builder
     * @param newCondition new condition
     */
    fun addCondition(stringBuilder: java.lang.StringBuilder, newCondition: String) {
        if (stringBuilder.toString().isNotBlank() || stringBuilder.toString().isNotEmpty()) {
            stringBuilder.append(" and $newCondition ")
        } else {
            stringBuilder.append(" $newCondition ")
        }
    }

    /**
     * Applies order parameter to query
     *
     * @param queryString original query string
     * @param asc if ordering is ascending (default is false)
     * @return formatter query string
     */
    fun applyOrder(queryString: String, asc: Boolean? = false): String {
        if (asc == true) {
            return "$queryString order by createdAt asc"
        }

        return "$queryString order by createdAt desc"
    }

    /**
     * Persists entity and waits for result
     *
     * @param entity new entity
     * @return saved entity
     */
    open suspend fun persistSuspending(entity: Entity): Entity {
        return try {
            persist(entity).awaitSuspending()
        } catch (e: Exception) {
            logSessionDetails()
            throw e
        }
    }

    /**
     * Deletes entity and waits for result
     *
     * @param entity entity to delete
     * @return void
     */
    open suspend fun deleteSuspending(entity: Entity) {
        try {
            delete(entity).awaitSuspending()
        } catch (e: Exception) {
            logSessionDetails()
            throw e
        }
    }

    /**
     * Applies range to query and executes it
     *
     * @param query find query
     * @param firstIndex first index
     * @param maxResults max results
     * @return entities
     */
    open suspend fun applyFirstMaxToQuery(
        query: PanacheQuery<Entity>,
        firstIndex: Int? = null,
        maxResults: Int? = null
    ): Pair<List<Entity>, Long> {
        val count = query.count().awaitSuspending()
        return if (firstIndex != null || maxResults != null) {
            val first = firstIndex ?: 0
            val max = maxResults ?: 10
            Pair(query.range<Entity>(first,  max + first - 1).list<Entity>().awaitSuspending(), count)
        } else
            Pair(query.list<Entity>().awaitSuspending(), count)
    }

    /**
     * Finds entity by id
     *
     * @param id id
     * @return entity if found
     */
    open suspend fun findByIdSuspending(id: Id): Entity? {
        return try {
            findById(id).awaitSuspending()
        } catch (e: Exception) {
            logSessionDetails()
            throw e
        }
    }
}