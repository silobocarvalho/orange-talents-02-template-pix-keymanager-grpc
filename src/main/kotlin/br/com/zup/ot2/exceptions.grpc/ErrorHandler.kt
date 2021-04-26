package br.com.zup.ot2.exceptions

import io.micronaut.aop.Around

@Around
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ErrorHandler
