package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity for thermometers
 */
@Entity
@Table(name = "thermometer")
class Thermometer: Metadata() {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var hardwareSensorId: String

    @Column(nullable = false)
    lateinit var espMacAddress: String

    @Column
    var name: String? = null

    @ManyToOne
    var site: Site? = null

    @Column
    var archivedAt: OffsetDateTime? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}
