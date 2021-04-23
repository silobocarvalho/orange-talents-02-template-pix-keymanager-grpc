package br.com.zup.ot2.pix.register.externalrequests

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.api.Http
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BCB {

    @Post("/api/v1/pix/keys")
    fun registerPixKey(): HttpResponse<AccountResponseDto>
}