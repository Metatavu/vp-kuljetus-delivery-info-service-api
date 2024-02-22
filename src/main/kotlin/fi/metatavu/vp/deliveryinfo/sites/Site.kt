package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity class for Site
 */
@Entity
@Table(name = "site")
class Site: Metadata() {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    @NotEmpty
    lateinit var name: String

    @Column(nullable = false)
    var latitude: Double = 0.0

    @Column(nullable = false)
    var longitude: Double = 0.0

    @Column(nullable = false)
    @NotEmpty
    lateinit var address: String

    @Column(nullable = false)
    @NotEmpty
    lateinit var postalCode: String

    @Column(nullable = false)
    @NotEmpty
    lateinit var locality: String

    @Column(nullable = true)
    var additionalInfo: String? = null

    @Column
    var archivedAt: OffsetDateTime? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}