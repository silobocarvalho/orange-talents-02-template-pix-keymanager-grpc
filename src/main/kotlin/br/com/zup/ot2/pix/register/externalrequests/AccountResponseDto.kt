package br.com.zup.ot2.pix.register.externalrequests

import br.com.zup.ot2.pix.register.Account
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.Column

data class AccountResponseDto(
    @JsonProperty("tipo")
    val accountType: String,

    @JsonProperty("instituicao")
    val institution: InstituicaoResponse,

    @JsonProperty("agencia")
    val agency: String,

    @JsonProperty("numero")
    val accountNumber: String,

    @JsonProperty("titular")
    val pixClient: PixClient,


) {
    fun toModel(): Account {
        return Account(
            institutionName = this.institution.name,
            institutionIspb = this.institution.ispb,
            clientName = this.pixClient.name,
            clientCpf = this.pixClient.cpf,
            agencyNumber = this.agency,
            accountNumber = this.accountNumber
        )

    }
}


data class PixClient(
    @JsonProperty("nome")
    val name: String,
    val cpf: String)

data class InstituicaoResponse(
    @JsonProperty("nome")
    val name: String,
    val ispb: String)
