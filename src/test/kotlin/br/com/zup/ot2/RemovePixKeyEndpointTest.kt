package br.com.zup.ot2

import br.com.zup.ot2.pix.register.PixKey
import br.com.zup.ot2.pix.register.externalrequests.AccountResponseDto
import br.com.zup.ot2.pix.register.externalrequests.InstituicaoResponse
import br.com.zup.ot2.pix.register.externalrequests.PixClient
import br.com.zup.ot2.pix.utils.PixKeyRepository
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*


@MicronautTest(transactional = false)
internal class RemovePixKeyEndpointTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: RemovePixKeyServiceGrpc.RemovePixKeyServiceBlockingStub
) {

    @BeforeEach
    fun setup(){
        pixKeyRepository.deleteAll()
    }


    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemovePixKeyServiceGrpc.RemovePixKeyServiceBlockingStub{
            return RemovePixKeyServiceGrpc.newBlockingStub(channel)
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