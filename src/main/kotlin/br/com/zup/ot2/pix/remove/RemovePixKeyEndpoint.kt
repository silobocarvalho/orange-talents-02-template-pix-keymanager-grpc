package br.com.zup.ot2.pix.remove

import br.com.zup.ot2.*
import br.com.zup.ot2.pix.utils.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemovePixKeyEndpoint(@Inject private val pixKeyRepository: PixKeyRepository): RemovePixKeyGrpc.RemovePixKeyImplBase(){

    override fun removePixKey(request: RemovePixKeyRequest?, responseObserver: StreamObserver<RemovePixKeyResponse>?) {
        val pixFromDb = request?.let {
            val pixId = UUID.fromString(it.pixId)
            val clientId = UUID.fromString(it.clientId)
            pixKeyRepository.findByIdAndClientId(id = pixId, clientId = clientId)
        }

        // if client data and pix key is not present into database, return error
        if(pixFromDb?.get() == null){
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription("Pix Key or Client Id not present.")
                    .asRuntimeException()
            )
            return
        }

        pixKeyRepository.delete(pixFromDb.get())

        responseObserver?.onNext(RemovePixKeyResponse.newBuilder()
            .setPixId(pixFromDb.get().id.toString())
            .setClientId(pixFromDb.get().clientId.toString())
            .build())

        responseObserver?.onCompleted()
    }

}