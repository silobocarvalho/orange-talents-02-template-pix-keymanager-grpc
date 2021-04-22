package br.com.zup.ot2.pix.register

import br.com.zup.ot2.PixKeyManagerGrpcServiceGrpc
import br.com.zup.ot2.RegisterPixKeyRequest
import br.com.zup.ot2.RegisterPixKeyResponse
import br.com.zup.ot2.pix.register.externalrequests.ItauAccountInformation
import io.grpc.Status
import io.grpc.Status.INVALID_ARGUMENT
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class RegisterPixKeyEndpoint(@Inject private val itauAccountsClient: ItauAccountInformation, @Inject private val picKeyRepository: PixKeyRepository): PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceImplBase(){

    override fun registerPixKey(
        request: RegisterPixKeyRequest?,
        responseObserver: StreamObserver<RegisterPixKeyResponse>?
    ) {
        //Get data
        val pixKeyRequestDto = request?.toModel()

        if(picKeyRepository.existsByPixKey(pixKeyRequestDto!!.pixKey))
        {
            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Pix Key already registered here.")
                    .asRuntimeException())
            return
        }

        //Request more info from a external system (account data)
        val clientAccountResponse = itauAccountsClient.findAccountByType(pixKeyRequestDto!!.clientId!!, pixKeyRequestDto.accountType!!.name)

        //Create our account model
        val account = try { clientAccountResponse.body().toModel()
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription("Client not found at Itau bank.")
                    .asRuntimeException())
            return
        }

        //Create our pix key with data from Account (external) and request data
        val pixKey = pixKeyRequestDto.toModel(account)

        picKeyRepository.save(pixKey)

        responseObserver?.onNext(RegisterPixKeyResponse.newBuilder()
            .setClientId(pixKey.clientId.toString())
            .setPixKeyId(pixKey.id.toString())
            .build())

        responseObserver?.onCompleted()
    }
}