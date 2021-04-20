package br.com.zup.ot2.pix.register

import br.com.zup.ot2.AccountType
import br.com.zup.ot2.KeyType
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class PixKeyRequestDto(
    @field:NotBlank
    val clientId: String,
    @field:NotNull
    val keyType: KeyType,
    @field:Size(max = 77)
    val pixKey: String,
    @field:NotNull
    val accountType: AccountType
) {
    fun toModel(account: Account): PixKey {
        return PixKey(
            clientId =  UUID.fromString(this.clientId),
            keyType = KeyType.valueOf(this.keyType.name),
            pixKey = if (this.keyType == KeyType.ALEATORIA) UUID.randomUUID().toString() else this.pixKey,
            accountType = AccountType.valueOf(this.accountType.name),
            account = account
        )
    }
/*
    fun toModel(account: AssociatedAccount): PixKey{
        return PixKey
    }
*/
}
