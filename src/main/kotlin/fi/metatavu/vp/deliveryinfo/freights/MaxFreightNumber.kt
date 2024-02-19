package fi.metatavu.vp.deliveryinfo.freights

import jakarta.persistence.*
import java.util.*

/**
 * MaxFreightNumber entity
 */
@Entity
@Table(name = "maxfreightnumber")
class MaxFreightNumber {

    @Id
    var id: UUID? = null

    @Column(unique = true, nullable = false)
    var maxFreightNumber: Long? = null
}