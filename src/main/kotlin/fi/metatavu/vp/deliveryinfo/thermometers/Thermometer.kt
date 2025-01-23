package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity for thermometers
 */
@Entity
@Table(name = "thermometer")
class Thermometer : Metadata() {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var hardwareSensorId: String

    @Column(nullable = false)
    lateinit var deviceIdentifier: String

    @Column
    var name: String? = null

    @ManyToOne
    lateinit var site: Site

    @Column
    var archivedAt: OffsetDateTime? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}
