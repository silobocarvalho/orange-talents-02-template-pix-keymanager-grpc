package br.com.zup.ot2.pix.utils

import br.com.zup.ot2.pix.register.PixKey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository: JpaRepository<PixKey, UUID> {

    fun existsByPixKey(pixKey: String): Boolean
    fun findByIdAndClientId(id: UUID, clientId: UUID ) : Optional<PixKey>
}