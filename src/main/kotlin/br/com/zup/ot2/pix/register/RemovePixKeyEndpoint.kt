package br.com.zup.ot2.pix.register

import br.com.zup.ot2.*
import br.com.zup.ot2.pix.register.externalrequests.ItauAccountInformation
import br.com.zup.ot2.pix.utils.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemovePixKeyEndpoint(@Inject private val itauAccountsClient: ItauAccountInformation, @Inject private val pixKeyRepository: PixKeyRepository): RemovePixKeyServiceGrpc.RemovePixKeyServiceImplBase(){

    override fun removePixKey(
        request: RemovePixKeyRequest?,
        responseObserver: StreamObserver<RemovePixKeyResponse>?) {


    }

}