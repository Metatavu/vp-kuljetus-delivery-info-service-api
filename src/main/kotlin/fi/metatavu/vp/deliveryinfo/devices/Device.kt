package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity class for Device
 */
@Entity
@Table(name = "device")
class Device {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    @NotEmpty
    lateinit var deviceId: String

    @ManyToOne
    lateinit var site: Site
}