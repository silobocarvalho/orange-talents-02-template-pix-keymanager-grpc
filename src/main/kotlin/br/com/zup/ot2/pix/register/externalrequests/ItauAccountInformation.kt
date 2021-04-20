package br.com.zup.ot2.pix.register.externalrequests

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.api.Http
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.accounts.url}")
interface ItauAccountInformation {

    @Get("/api/v1/clientes/{clientId}/contas?tipo={accountType}")
    fun findAccountByType(@PathVariable clientId: String, @QueryValue accountType: String): HttpResponse<AccountResponseDto>
}