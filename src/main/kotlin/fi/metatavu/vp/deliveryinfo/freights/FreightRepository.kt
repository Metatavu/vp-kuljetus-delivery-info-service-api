package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Repository for freights
 */
@ApplicationScoped
class FreightRepository: AbstractRepository<Freight, UUID>() {

    /**
     * Creates a new freight
     *
     * @param id id
     * @param pointOfDeparture point of departure
     * @param destination destination
     * @param sender sender
     * @param recipient recipient
     * @param payer payer
     * @param shipmentInfo shipment info
     * @param temperatureMin minimum temperature
     * @param temperatureMax maximum temperature
     * @param reservations reservations
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created freight
     */
    suspend fun create(
        id: UUID,
        pointOfDeparture: String,
        destination: String,
        sender: String,
        recipient: String,
        payer: String?,
        shipmentInfo: String?,
        temperatureMin: Double?,
        temperatureMax: Double?,
        reservations: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Freight {
        val freight = Freight()
        freight.id = id
        freight.pointOfDeparture = pointOfDeparture
        freight.destination = destination
        freight.sender = sender
        freight.recipient = recipient
        freight.payer = payer
        freight.shipmentInfo = shipmentInfo
        freight.temperatureMin = temperatureMin
        freight.temperatureMax = temperatureMax
        freight.reservations = reservations
        freight.creatorId = creatorId
        freight.lastModifierId = lastModifierId
        return persistSuspending(freight)
    }
}