package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * MaxFreightNumber entity repository
 */
@ApplicationScoped
class MaxFreightNumberRepository: AbstractRepository<MaxFreightNumber, UUID>(){

    /**
     * Finds current max freight number
     *
     * @return max freight number
     */
    suspend fun findMaxNumber(): MaxFreightNumber? {
        return listAll().awaitSuspending().first()
    }

}