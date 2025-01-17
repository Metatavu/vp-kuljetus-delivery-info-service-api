package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.util.*


/**
 * Entity class for TemperatureRecord
 */
@Entity
@Table(name = "temperaturerecord")
class TemperatureRecord: Metadata() {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    @NotEmpty
    lateinit var deviceId: String

    @Column(nullable = false)
    @NotEmpty
    lateinit var sensorId: String

    @Column(nullable = false)
    var value: Float? = null

    @Column(nullable = false)
    var timestamp: Long? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}