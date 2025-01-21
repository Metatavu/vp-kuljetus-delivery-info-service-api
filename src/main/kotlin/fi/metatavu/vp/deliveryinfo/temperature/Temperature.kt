package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.thermometers.Thermometer
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "temperature")
class Temperature {
    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var thermometer: Thermometer

    @Column(nullable = false)
    var value: Float? = 0.0F

    @Column(nullable = false)
    var timestamp: Long? = null
}