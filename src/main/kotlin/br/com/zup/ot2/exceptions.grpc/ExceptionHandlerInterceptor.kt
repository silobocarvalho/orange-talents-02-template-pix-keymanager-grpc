package br.com.zup.ot2.exceptions

import br.com.zup.ot2.exceptions.grpc.PixKeyAlreadyRegisteredException
import br.com.zup.ot2.pix.register.RegisterPixKeyEndpoint
import br.com.zup.ot2.pix.remove.RemovePixKeyEndpoint
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.grpc.stub.StreamObservers
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptorRegisterPixKey: MethodInterceptor<RegisterPixKeyEndpoint, Any?> {
    override fun intercept(context: MethodInvocationContext<RegisterPixKeyEndpoint, Any?>): Any? {

        try {
            return context.proceed()
        }catch (t: Throwable){

            println("entrou no handlers \n\n\n\n\n")
            println("${t.toString()}")

            val statusError = when(t){
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(t.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(t.message).asRuntimeException()
                is PixKeyAlreadyRegisteredException -> Status.ALREADY_EXISTS.withDescription(t.message).asRuntimeException()
                is NoSuchElementException -> Status.NOT_FOUND.withDescription(t.message).asRuntimeException()

                else -> Status.UNKNOWN.withDescription(t.message).asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null
        }
    }
}

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptorRemovePixKey: MethodInterceptor<RemovePixKeyEndpoint, Any?> {
    override fun intercept(context: MethodInvocationContext<RemovePixKeyEndpoint, Any?>): Any? {

        try {
            return context.proceed()
        }catch (t: Throwable){

            println("entrou no handlers \n\n\n\n\n")
            println("${t.toString()}")

            val statusError = when(t){
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(t.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(t.message).asRuntimeException()
                is PixKeyAlreadyRegisteredException -> Status.ALREADY_EXISTS.withDescription(t.message).asRuntimeException()
                is NoSuchElementException -> Status.NOT_FOUND.withDescription(t.message).asRuntimeException()

                else -> Status.UNKNOWN.withDescription(t.message).asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null
        }
    }
}