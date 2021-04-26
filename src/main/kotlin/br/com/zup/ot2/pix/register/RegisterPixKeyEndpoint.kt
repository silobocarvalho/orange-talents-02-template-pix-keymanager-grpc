package br.com.zup.ot2.pix.register

import br.com.zup.ot2.RegisterPixKeyGrpc
import br.com.zup.ot2.RegisterPixKeyRequest
import br.com.zup.ot2.RegisterPixKeyResponse
import br.com.zup.ot2.exceptions.ErrorHandler
import br.com.zup.ot2.exceptions.grpc.PixKeyAlreadyRegisteredException
import br.com.zup.ot2.pix.register.externalrequests.BCB
import br.com.zup.ot2.pix.register.externalrequests.ItauAccountInformation
import br.com.zup.ot2.pix.register.externalrequests.RegisterPixKeyBCBRequest
import br.com.zup.ot2.pix.utils.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@ErrorHandler
class RegisterPixKeyEndpoint(@Inject private val itauAccountsClient: ItauAccountInformation, @Inject private val BCBInformation: BCB, @Inject private val pixKeyRepository: PixKeyRepository): RegisterPixKeyGrpc.RegisterPixKeyImplBase(){

    override fun registerPixKey(
        request: RegisterPixKeyRequest?,
        responseObserver: StreamObserver<RegisterPixKeyResponse>?
    ) {
        //Get data
        val pixKeyRequestDto = request?.toModel()

        if(pixKeyRepository.existsByPixKey(pixKeyRequestDto!!.pixKey))
        {
            throw PixKeyAlreadyRegisteredException("Pix Key already registered here.")
        }

        //Request more info from a external system (account data)
        val clientAccountResponse = itauAccountsClient.findAccountByType(pixKeyRequestDto!!.clientId!!, pixKeyRequestDto.accountType!!.name) ?: throw  NoSuchElementException("Client not found at Itau bank.")

        //Create our account model
        val account = clientAccountResponse.body().toModel() ?: throw  NoSuchElementException("Client not found at Itau bank.")

        //Create our pix key with data from Account (external) and request data
        val pixKey = pixKeyRequestDto.toModel(account)

        SaveAndPublishToBCB(pixKey)

        responseObserver?.onNext(RegisterPixKeyResponse.newBuilder()
            .setClientId(pixKey.clientId.toString())
            .setPixKeyId(pixKey.id.toString())
            .build())

        responseObserver?.onCompleted()
    }

    @Transactional
    private fun SaveAndPublishToBCB(
        @Valid pixKey: PixKey
    ){
        pixKeyRepository.save(pixKey)
        val bcbResponse = BCBInformation.registerPixKey(RegisterPixKeyBCBRequest.of(pixKey)) ?: throw IllegalArgumentException("Error saving PixKey into BCB system.")
    }
}