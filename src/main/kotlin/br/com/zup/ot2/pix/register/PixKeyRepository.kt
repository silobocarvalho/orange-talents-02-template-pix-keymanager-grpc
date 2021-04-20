package br.com.zup.ot2.pix.register

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository: JpaRepository<PixKey, UUID> {
    fun existsByPixKey(pixKey: String): Boolean
}