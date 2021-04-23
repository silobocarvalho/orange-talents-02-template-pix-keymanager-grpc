package br.com.zup.ot2.pix.register

import br.com.zup.ot2.RegisterPixKeyRequest
import br.com.zup.ot2.RegisterPixKeyResponse
import br.com.zup.ot2.RegisterPixKeyServiceGrpc
import br.com.zup.ot2.pix.register.externalrequests.ItauAccountInformation
import br.com.zup.ot2.pix.utils.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterPixKeyEndpoint(@Inject private val itauAccountsClient: ItauAccountInformation, @Inject private val pixKeyRepository: PixKeyRepository): RegisterPixKeyServiceGrpc.RegisterPixKeyServiceImplBase(){

    override fun registerPixKey(
        request: RegisterPixKeyRequest?,
        responseObserver: StreamObserver<RegisterPixKeyResponse>?
    ) {
        //Get data
        val pixKeyRequestDto = request?.toModel()

        if(pixKeyRepository.existsByPixKey(pixKeyRequestDto!!.pixKey))
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

        pixKeyRepository.save(pixKey)

        responseObserver?.onNext(RegisterPixKeyResponse.newBuilder()
            .setClientId(pixKey.clientId.toString())
            .setPixKeyId(pixKey.id.toString())
            .build())

        responseObserver?.onCompleted()
    }
}