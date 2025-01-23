package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity for thermometers
 */
@Entity
@Table(name = "thermometer")
class Thermometer {
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

    @Column
    var lastModifierId: UUID? = null

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    var modifiedAt: OffsetDateTime? = null

    /**
     * JPA pre-persist event handler
     */
    @PrePersist
    fun onCreate() {
        val odtNow = OffsetDateTime.now()
        createdAt = odtNow
        modifiedAt = odtNow
    }

    /**
     * JPA pre-update event handler
     */
    @PreUpdate
    fun onUpdate() {
        modifiedAt = OffsetDateTime.now()
    }
}
