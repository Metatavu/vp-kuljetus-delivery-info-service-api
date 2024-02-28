package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.api.model.TaskStatus
import fi.metatavu.vp.api.model.TaskType
import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity class for Task
 */
@Entity
@Table(name = "task")
class Task: Metadata() {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var freight: Freight

    @ManyToOne
    lateinit var site: Site

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    lateinit var taskType: TaskType

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    lateinit var status: TaskStatus

    @Column(nullable=false)
    var groupNumber: Int = 0

    @Column
    var remarks: String? = null

    @Column
    var routeId: UUID? = null

    @Column
    var startedAt: OffsetDateTime? = null

    @Column
    var finishedAt: OffsetDateTime? = null

    @Column
    var orderNumber: Int? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}