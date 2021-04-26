package br.com.zup.ot2

import br.com.zup.ot2.pix.register.Account
import br.com.zup.ot2.pix.register.PixKey
import br.com.zup.ot2.pix.register.externalrequests.*
import br.com.zup.ot2.pix.register.toModel
import br.com.zup.ot2.pix.utils.PixKeyRepository
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyObject
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.internal.matchers.Any
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.owner
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.jvm.internal.impl.load.java.structure.JavaClass

@MicronautTest(transactional = false)
class RegisterPixKeyEndpointTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: RegisterPixKeyGrpc.RegisterPixKeyBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauAccountInformation

    @Inject
    lateinit var BCBClient: BCB

    @BeforeEach
    fun setup(){
        pixKeyRepository.deleteAll()
    }


    @MockBean(ItauAccountInformation::class)
    fun itauClient(): ItauAccountInformation? {
        return Mockito.mock(ItauAccountInformation::class.java)
    }

    @MockBean(BCB::class)
    fun BCBClient(): BCB? {
        return Mockito.mock(BCB::class.java)
    }

    @Factory
    class ClientsRegister{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegisterPixKeyGrpc.RegisterPixKeyBlockingStub{
            return RegisterPixKeyGrpc.newBlockingStub(channel)
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

    //Create a expected response from external system
    private fun fakeDataAccountResponseBCB(): RegisterPixKeyBCBResponse {
        return RegisterPixKeyBCBResponse(
            keyType = PixKeyTypeBCB.CPF,
            key = "63657520325",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }


    val fakePixKeyRequest = RegisterPixKeyRequest
        .newBuilder()
        .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
        .setKeyType(KeyType.CPF)
        .setPixKey("63657520325")
        .setAccountType(AccountType.CONTA_CORRENTE)
        .build()

    var fakePixKeyRequestBCB = RegisterPixKeyBCBRequest(
            keyType = KeyType.CPF,
            key = "63657520325",
        bankAccount = bankAccount(),
            owner = owner())

    private fun owner(): PixKeyTypeBCB.Owner {
        return PixKeyTypeBCB.Owner(
            type = PixKeyTypeBCB.Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
    }

    private fun bankAccount(): PixKeyTypeBCB.BankAccount {
        return PixKeyTypeBCB.BankAccount(
            participant = "63657520325",
            branch = "001",
            accountNumber = "123455",
            accountType = PixKeyTypeBCB.BankAccount.AccountTypeBCB.CACC
        )
    }

    object MockitoHelper {
        fun <T> anyObject(): T {
            Mockito.any<T>()
            return uninitialized()
        }
        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T =  null as T
    }



    @Test
    fun `should register a new pix key using CPF`(){

    `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
        .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

    `when`(BCBClient.registerPixKey(MockitoHelper.anyObject()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseBCB()))

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

        `when`(BCBClient.registerPixKey(MockitoHelper.anyObject()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseBCB()))

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
    fun `should register a new pix key using phone number`() {
    }

    @Test
    fun `should register a new pix key using Aleatory Key`(){

        `when`(itauClient.findAccountByType(clientId = fakePixKeyRequest.clientId, accountType = AccountType.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseItau()))

        `when`(BCBClient.registerPixKey(MockitoHelper.anyObject()))
            .thenReturn(HttpResponse.ok(fakeDataAccountResponseBCB()))

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