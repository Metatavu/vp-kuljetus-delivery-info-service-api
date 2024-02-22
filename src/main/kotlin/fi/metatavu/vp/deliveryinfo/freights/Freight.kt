package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Freight entity
 */
@Entity
@Table(name = "freight")
class Freight: Metadata() {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    @NotEmpty
    lateinit var pointOfDeparture: String

    @Column(nullable = false)
    @NotEmpty
    lateinit var destination: String

    @Column(nullable = false)
    @NotEmpty
    lateinit var sender: String

    @Column(nullable = false)
    @NotEmpty
    lateinit var recipient: String

    @Column
    var payer: String? = null

    @Column
    var shipmentInfo: String? = null

    @Column
    var temperatureMin: Double? = null

    @Column
    var temperatureMax: Double? = null

    @Column
    var reservations: String? = null

    @Column(insertable = false, updatable = false)
    var freightNumber: Int? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}