package br.com.zup.ot2.pix.register

import br.com.zup.ot2.AccountType
import br.com.zup.ot2.KeyType
import io.micronaut.validation.Validated
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.validation.executable.ValidateOnExecution
import kotlin.math.max

@Entity
@ValidateOnExecution
data class PixKey(

    @field:NotNull
    @Column(nullable = false)
    val clientId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val keyType: KeyType,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    @field:Size(max = 77)
    @ValidPixKey
    val pixKey: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accountType: AccountType,

    @field:Valid
    @Embedded
    val account: Account
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    override fun toString(): String {
        return "PixKey(id=$id, clientId=$clientId, keyType=$keyType, pixKey='$pixKey', accountType=$accountType, accountNumber=${account.accountNumber}, agencyNumber=${account.agencyNumber}, clientCpf=${account.clientCpf}, accountName=${account.clientName}, institutionName=${account.institutionName}, institutionIspb=${account.institutionIspb}, createdAt=$createdAt)"
    }


}
