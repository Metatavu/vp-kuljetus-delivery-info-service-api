package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.persistence.Metadata
import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

/**
 * Freight entity
 */
@Entity
@Table(name = "freight")
class Freight: Metadata() {

    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var pointOfDepartureSite: Site

    @ManyToOne
    lateinit var destinationSite: Site

    @ManyToOne
    lateinit var senderSite: Site

    @ManyToOne
    lateinit var recipientSite: Site

    @Column(insertable = false, updatable = false)
    var freightNumber: Int? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}