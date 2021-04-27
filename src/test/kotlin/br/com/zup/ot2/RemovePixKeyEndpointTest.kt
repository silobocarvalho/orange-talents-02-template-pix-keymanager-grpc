package br.com.zup.ot2

import br.com.zup.ot2.pix.register.PixKey
import br.com.zup.ot2.pix.register.externalrequests.*
import br.com.zup.ot2.pix.utils.PixKeyRepository
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
class RemovePixKeyEndpointTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: RemovePixKeyGrpc.RemovePixKeyBlockingStub
) {

    @BeforeEach
    fun setup(){
        pixKeyRepository.deleteAll()
    }


    @Factory
    class ClientsRemove{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemovePixKeyGrpc.RemovePixKeyBlockingStub{
            return RemovePixKeyGrpc.newBlockingStub(channel)
        }
    }

    @Inject
    lateinit var BCBClient: BCB
    @MockBean(BCB::class)
    fun BCBClient(): BCB? {
        return Mockito.mock(BCB::class.java)
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
    fun `should REMOVE a pix key using CPF`(){


        val pixKey = PixKey(
            clientId = UUID.fromString(fakePixKeyRequest.clientId),
            keyType = fakePixKeyRequest.keyType,
            pixKey = fakePixKeyRequest.pixKey,
            accountType = fakePixKeyRequest.accountType,
            account = fakeDataAccountResponseItau().toModel()
        )

        pixKeyRepository.save(pixKey) //after saved, the object reflects the database, containing a Id now

        val fakeRemovePixKeyRequest = RemovePixKeyRequest
            .newBuilder()
            .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setPixId(pixKey.id.toString())
            .build()

        val registerPixKeyResponse = grpcClient.removePixKey(fakeRemovePixKeyRequest)

        with(registerPixKeyResponse){
            Assertions.assertEquals(this.pixId, fakeRemovePixKeyRequest.pixId)
            Assertions.assertEquals(this.clientId, fakeRemovePixKeyRequest.clientId)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(pixKey.id.toString()))
        }
    }

    @Test
    fun `should REMOVE a pix key using Email`(){

        val pixKey = PixKey(
            clientId = UUID.fromString(fakePixKeyRequest.clientId),
            keyType = KeyType.EMAIL,
            pixKey = "sid@zup.com.br",
            accountType = fakePixKeyRequest.accountType,
            account = fakeDataAccountResponseItau().toModel()
        )

        pixKeyRepository.save(pixKey) //after saved, the object reflects the database, containing a Id now

        val fakeRemovePixKeyRequest = RemovePixKeyRequest
            .newBuilder()
            .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setPixId(pixKey.id.toString())
            .build()

        val registerPixKeyResponse = grpcClient.removePixKey(fakeRemovePixKeyRequest)

        with(registerPixKeyResponse){
            Assertions.assertEquals(this.pixId, fakeRemovePixKeyRequest.pixId)
            Assertions.assertEquals(this.clientId, fakeRemovePixKeyRequest.clientId)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(pixKey.id.toString()))
        }
    }

    @Test
    fun `should REMOVE a pix key using Phone Number`(){

        val pixKey = PixKey(
            clientId = UUID.fromString(fakePixKeyRequest.clientId),
            keyType = KeyType.CELULAR,
            pixKey = "+5585999067835",
            accountType = fakePixKeyRequest.accountType,
            account = fakeDataAccountResponseItau().toModel()
        )

        pixKeyRepository.save(pixKey) //after saved, the object reflects the database, containing a Id now

        val fakeRemovePixKeyRequest = RemovePixKeyRequest
            .newBuilder()
            .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setPixId(pixKey.id.toString())
            .build()

        val registerPixKeyResponse = grpcClient.removePixKey(fakeRemovePixKeyRequest)

        with(registerPixKeyResponse){
            Assertions.assertEquals(this.pixId, fakeRemovePixKeyRequest.pixId)
            Assertions.assertEquals(this.clientId, fakeRemovePixKeyRequest.clientId)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(pixKey.id.toString()))
        }



    }

    @Test
    fun `should REMOVE a pix key using Aleatory Key`(){

        val pixKey = PixKey(
            clientId = UUID.fromString(fakePixKeyRequest.clientId),
            keyType = KeyType.ALEATORIA,
            pixKey = "",
            accountType = fakePixKeyRequest.accountType,
            account = fakeDataAccountResponseItau().toModel()
        )

        pixKeyRepository.save(pixKey) //after saved, the object reflects the database, containing a Id now

        val fakeRemovePixKeyRequest = RemovePixKeyRequest
            .newBuilder()
            .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setPixId(pixKey.id.toString())
            .build()

        val registerPixKeyResponse = grpcClient.removePixKey(fakeRemovePixKeyRequest)

        with(registerPixKeyResponse){
            Assertions.assertEquals(this.pixId, fakeRemovePixKeyRequest.pixId)
            Assertions.assertEquals(this.clientId, fakeRemovePixKeyRequest.clientId)
            Assertions.assertFalse(pixKeyRepository.existsByPixKey(pixKey.id.toString()))
        }
    }

}