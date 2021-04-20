package br.com.zup.ot2.pix.register

import br.com.zup.ot2.AccountType
import br.com.zup.ot2.KeyType
import br.com.zup.ot2.RegisterPixKeyRequest

fun RegisterPixKeyRequest.toModel() : PixKeyRequestDto{
    return PixKeyRequestDto(
        clientId = clientId,
        keyType = when(keyType){
            KeyType.UNKNOWN_KEY_TYPE -> null
            else -> KeyType.valueOf(keyType.name)
        }!!,
        pixKey = pixKey,
        accountType = when(accountType){
            AccountType.UNKNOWN_ACCOUNT_TYPE -> null
            else -> AccountType.valueOf(accountType.name)
        }!!
    )
}