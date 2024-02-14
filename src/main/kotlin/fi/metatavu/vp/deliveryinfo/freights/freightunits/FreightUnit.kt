package fi.metatavu.vp.deliveryinfo.freights.freightunits

import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * One unit of a freight
 */
@Entity
@Table(name = "freight_unit")
class FreightUnit: Metadata() {

    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var freight: Freight

    @Column(nullable=false)
    @NotEmpty
    lateinit var type: String

    @Column(nullable=false)
    @NotEmpty
    lateinit var quantity: String

    @Column(nullable=false)
    @NotEmpty
    lateinit var quantityUnit: String

    @Column
    var reservations: String? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID

}