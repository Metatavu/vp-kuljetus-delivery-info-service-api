package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Task JPA to REST entity
 */
@ApplicationScoped
class TaskTranslator: AbstractTranslator<Task, fi.metatavu.vp.api.model.Task>() {
    override suspend fun translate(entity: Task): fi.metatavu.vp.api.model.Task {
        return fi.metatavu.vp.api.model.Task(
            id = entity.id,
            freightId = entity.freight.id,
            customerSiteId = entity.site.id!!,
            type = entity.taskType,
            status = entity.status,
            remarks = entity.remarks,
            routeId = entity.routeId,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}