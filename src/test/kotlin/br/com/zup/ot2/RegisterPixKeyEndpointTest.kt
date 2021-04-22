package br.com.zup.ot2

import br.com.zup.ot2.pix.register.PixKey
import br.com.zup.ot2.pix.register.PixKeyRepository
import br.com.zup.ot2.pix.register.PixKeyRequestDto
import br.com.zup.ot2.pix.register.externalrequests.AccountResponseDto
import br.com.zup.ot2.pix.register.externalrequests.InstituicaoResponse
import br.com.zup.ot2.pix.register.externalrequests.ItauAccountInformation
import br.com.zup.ot2.pix.register.externalrequests.PixClient
import br.com.zup.ot2.pix.register.toModel
import io.grpc.Grpc
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegisterPixKeyEndpointTest(
    val grpcClient: PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceBlockingStub,
    val pixKeyRepository: PixKeyRepository
) {

    @Inject
    lateinit var itauClient: ItauAccountInformation

    @BeforeEach
    fun setup(){
        pixKeyRepository.deleteAll()
    }


    @MockBean(ItauAccountInformation::class)
    fun itauClient(): ItauAccountInformation? {
        return Mockito.mock(ItauAccountInformation::class.java)
    }

    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceBlockingStub{
            return PixKeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    //Create a expected response from external system
    private fun fakeDataAccountResponseItau(): AccountResponseDto {
        return AccountResponseDto(
            accountType = "CONTA_CORRENTE",
            institution = InstituicaoResponse(name= "UNIBANCO ITAU SA", ispb = "60701190"),
            agency = "1218",
            accountNumber = "291900",
            pixClient = PixClient(name = "Rafael Ponte", cpf= "63657520325")
        )
    }

    val fakePixKeyRequest = RegisterPixKeyRequest
        .newBuilder()
        .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
        .setKeyType(KeyType.CPF)
        .setPixKey("63657520325")
        .setAccountType(AccountType.CONTA_CORRENTE)
        .build()

    @Test
    fun `should register a new pix key using CPF`(){

    `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
        .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))


    val registerPixKeyResponse = grpcClient.registerPixKey(fakePixKeyRequest)

    with(registerPixKeyResponse){
        Assertions.assertNotNull(this.pixKeyId)
        Assertions.assertEquals(this.clientId, fakePixKeyRequest.clientId)
        Assertions.assertTrue(pixKeyRepository.existsByPixKey(fakePixKeyRequest.pixKey))
    }

}

    @Test
    fun `should register a new pix key using Email`(){

        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.pixKey = "sidartha@zup.com.br"
        newFakePixKey.keyType = KeyType.EMAIL

        val registerPixKeyResponse = grpcClient.registerPixKey(newFakePixKey.build())

        with(registerPixKeyResponse){
            Assertions.assertNotNull(this.pixKeyId)
            Assertions.assertEquals(this.clientId, newFakePixKey.clientId)
            Assertions.assertTrue(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }
    }

    @Test
    fun `should register a new pix key using phone number`(){

        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.pixKey = "+5585999067836"
        newFakePixKey.keyType = KeyType.CELULAR

        val registerPixKeyResponse = grpcClient.registerPixKey(newFakePixKey.build())

        with(registerPixKeyResponse){
            Assertions.assertNotNull(this.pixKeyId)
            Assertions.assertEquals(this.clientId, newFakePixKey.clientId)
            Assertions.assertTrue(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }
    }

    @Test
    fun `should register a new pix key using Aleatory Key`(){

        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.pixKey = ""
        newFakePixKey.keyType = KeyType.ALEATORIA

        val registerPixKeyResponse = grpcClient.registerPixKey(newFakePixKey.build())

        with(registerPixKeyResponse){
            Assertions.assertNotNull(this.pixKeyId)
            Assertions.assertEquals(this.clientId, newFakePixKey.clientId)
            Assertions.assertTrue(pixKeyRepository.existsById(UUID.fromString(this.pixKeyId)))
        }
    }

    @Test
    fun `should NOT register a pix key that already exists`() {

        val pixKey = PixKey(
            clientId = UUID.fromString(fakePixKeyRequest.clientId),
            keyType = fakePixKeyRequest.keyType,
            pixKey = fakePixKeyRequest.pixKey,
            accountType = fakePixKeyRequest.accountType,
            account = fakeDataAccountResponseItau().toModel()
        )

        pixKeyRepository.save(pixKey)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(fakePixKeyRequest)
        }

        println("Error eh: ${error.toString()}")

        with(error){
            Assertions.assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Test
    fun `should NOT register a pix with invalid CPF`() {
        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.pixKey = "12345678900"
        newFakePixKey.keyType = KeyType.CPF
        println(newFakePixKey)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(newFakePixKey.build())
        }

        with(error){
            //Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }
    }

    @Test
    fun `should NOT register a pix with invalid PHONE NUMBER`() {
        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.pixKey = "+85999067836" //without 55 brazil code
        newFakePixKey.keyType = KeyType.CELULAR

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(newFakePixKey.build())
        }

        with(error){
            //Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }
    }

    @Test
    fun `should NOT register a pix with invalid EMAIL`() {
        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.pixKey = "sidarthaAtZup.com.br"
        newFakePixKey.keyType = KeyType.EMAIL

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(newFakePixKey.build())
        }

        with(error){
            //Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }
    }

    @Test
    fun `should NOT register a new pix key when Client does not have been registered at Itau`(){

        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.clientId = UUID.randomUUID().toString() //invalid client Id

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(newFakePixKey.build())
        }

        with(error){
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }

    }

    @Test
    fun `should NOT register a pix with invalid Account Type`() {
        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        var newFakePixKey = fakePixKeyRequest.toBuilder()
        newFakePixKey.accountType = AccountType.UNKNOWN_ACCOUNT_TYPE
        println(newFakePixKey)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(newFakePixKey.build())
        }

        with(error){
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(newFakePixKey.pixKey))
        }
    }
}