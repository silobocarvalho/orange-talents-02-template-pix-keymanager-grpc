package br.com.zup.ot2.exceptions.grpc

import java.lang.RuntimeException

class PixKeyAlreadyRegisteredException(message: String?) : RuntimeException(message)