package br.com.zup.ot2.pix.register

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class Account(
    @field:NotBlank
    @Column(nullable = false)

    val institutionName: String,

    @field:NotBlank

    @Column(nullable = false)
    val institutionIspb: String,

    @field:NotBlank
    @Column(nullable = false)

    val clientName: String,

    @field:NotBlank
    @field:Size(max = 11)
    @Column(length = 11, nullable = false)
    @JsonProperty("titular.cpf")
    val clientCpf: String,

    @field:NotBlank
    @Column(nullable = false)
    @JsonProperty("agencia")
    val agencyNumber: String,

    @field:NotBlank
    @JsonProperty("numero",)
    @Column(nullable = false)
    val accountNumber: String
) {
    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}
