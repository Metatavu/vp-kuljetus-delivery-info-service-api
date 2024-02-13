package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.api.model.TaskType
import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for Task
 */
@ApplicationScoped
class TaskRepository : AbstractRepository<Task, UUID>() {

    /**
     * Creates a new task
     *
     * @param id id
     * @param freight freight
     * @param site site
     * @param type type
     * @param remarks remarks
     * @param routeId route id
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created task
     */
    suspend fun create(
        id: UUID,
        freight: Freight,
        site: Site,
        type: TaskType,
        remarks: String?,
        routeId: UUID?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Task {
        val task = Task()
        task.id = id
        task.freight = freight
        task.site = site
        task.type = type
        task.remarks = remarks
        task.routeId = routeId
        task.creatorId = creatorId
        task.lastModifierId = lastModifierId
        return persistSuspending(task)
    }

    /**
     * Lists tasks
     *
     * @param routeId route id
     * @param assignedToRoute assigned to route
     * @param freight freight
     * @param site customer site
     * @param type type
     * @param first first result
     * @param max max results
     * @return pair of list of tasks and total count
     */
    suspend fun list(
        routeId: UUID? = null,
        assignedToRoute: Boolean? = null,
        freight: Freight? = null,
        site: Site? = null,
        type: TaskType? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<Task>, Long> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (routeId != null) {
            addCondition(queryBuilder, "routeId = :routeId")
            parameters.and("routeId", routeId)
        }

        if (assignedToRoute != null) {
            if (assignedToRoute == true) {
                addCondition(queryBuilder, "routeId IS NOT NULL")
            } else {
                addCondition(queryBuilder, "routeId IS NULL")
            }
        }

        if (freight != null) {
            addCondition(queryBuilder, "freight = :freight")
            parameters.and("freight", freight)
        }

        if (site != null) {
            addCondition(queryBuilder, "site = :site")
            parameters.and("site", site)
        }

        if (type != null) {
            addCondition(queryBuilder, "type = :type")
            parameters.and("type", type)
        }

        queryBuilder.append(" order by modifiedAt desc")
        return applyFirstMaxToQuery(find(queryBuilder.toString(), parameters), first, max)
    }

}