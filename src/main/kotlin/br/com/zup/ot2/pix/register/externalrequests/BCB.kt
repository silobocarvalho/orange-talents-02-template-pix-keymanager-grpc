package br.com.zup.ot2.pix.register.externalrequests

import br.com.zup.ot2.AccountType
import br.com.zup.ot2.AccountType.*
import br.com.zup.ot2.KeyType
import br.com.zup.ot2.pix.register.Account
import br.com.zup.ot2.pix.register.PixKey
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
interface BCB {

    @Post(
        value = "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML])
    fun registerPixKey(@Body request: RegisterPixKeyBCBRequest): HttpResponse<RegisterPixKeyBCBResponse>
}


data class RegisterPixKeyBCBRequest(
    //This names of variables where defined by API BCB, we have to use exactly the same names expected at BCB to convert to XML request.
    val keyType: KeyType,
    val key: String,
    val bankAccount: PixKeyTypeBCB.BankAccount,
    val owner: PixKeyTypeBCB.Owner
) {

    companion object{
        fun of(pixKey: PixKey): RegisterPixKeyBCBRequest{
            return RegisterPixKeyBCBRequest(
                keyType = pixKey.keyType,
                key = pixKey.pixKey,
                bankAccount = PixKeyTypeBCB.BankAccount(
                    participant = pixKey.account.institutionIspb,
                    branch = pixKey.account.agencyNumber,
                    accountNumber = pixKey.account.accountNumber,
                    accountType = PixKeyTypeBCB.BankAccount.AccountTypeBCB.by(pixKey.accountType),
                ),
                owner = PixKeyTypeBCB.Owner(
                    type = PixKeyTypeBCB.Owner.OwnerType.NATURAL_PERSON,
                    name = pixKey.account.clientName,
                    taxIdNumber = pixKey.account.clientCpf
                )

            )
        }
    }
}



data class RegisterPixKeyBCBResponse(
    val keyType: PixKeyTypeBCB,
    val key: String,
    val bankAccount: PixKeyTypeBCB.BankAccount,
    val owner: PixKeyTypeBCB.Owner,
    val createdAt: LocalDateTime
) {

}

enum class PixKeyTypeBCB(val domainType: KeyType?) {

    CPF(KeyType.CPF),
    CNPJ(null),
    PHONE(KeyType.CELULAR),
    EMAIL(KeyType.EMAIL),
    RANDOM(KeyType.ALEATORIA);

    companion object {

        private val mapping = PixKeyTypeBCB.values().associateBy(PixKeyTypeBCB::domainType)

        fun by(domainType: KeyType): PixKeyTypeBCB {
            return mapping[domainType]
                ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }

    data class BankAccount(
        /**
         * 60701190 ITAÚ UNIBANCO S.A.
         * https://www.bcb.gov.br/pom/spb/estatistica/port/ASTR003.pdf (line 221)
         */
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: AccountTypeBCB
    ) {

        /**
         * https://open-banking.pass-consulting.com/json_ExternalCashAccountType1Code.html
         */
        enum class AccountTypeBCB() {

            CACC, // Current: Account used to post debits and credits when no specific account has been nominated
            SVGS; // Savings: Savings

            companion object {
                fun by(domainType: AccountType): AccountTypeBCB {
                    return when (domainType) {
                        CONTA_CORRENTE -> CACC
                        CONTA_POUPANCA -> SVGS
                        UNKNOWN_ACCOUNT_TYPE -> TODO()
                        UNRECOGNIZED -> TODO()
                    }
                }
            }
        }

    }

    data class Owner(
        val type: OwnerType,
        val name: String,
        val taxIdNumber: String
    ) {

        enum class OwnerType {
            NATURAL_PERSON,
            LEGAL_PERSON
        }
    }
}