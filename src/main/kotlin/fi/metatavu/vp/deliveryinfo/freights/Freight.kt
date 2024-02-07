package fi.metatavu.vp.deliveryinfo.freights

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Entity
@Table(name = "freight")
class Freight {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    @NotEmpty
    lateinit var name: String

    @Column
    lateinit var location: String

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}