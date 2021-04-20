package br.com.zup.ot2.pix.register

import org.hibernate.validator.constraints.CompositionType
import org.hibernate.validator.constraints.ConstraintComposition
import org.hibernate.validator.constraints.br.CNPJ
import org.hibernate.validator.constraints.br.CPF
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@CPF
@CNPJ
@Email
@Pattern(regexp="(^\\+[1-9][0-9]\\d{11}\$)") //Phone number
@ValidUUID
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = [])
@ReportAsSingleViolation
annotation class ValidPixKey(val message: String = "Document is not a valid Pix Key.",
                             val groups: Array<KClass<Any>> = [],
                             val payload: Array<KClass<Payload>> = []
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = [])
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
    flags = [Pattern.Flag.CASE_INSENSITIVE])
annotation class ValidUUID(val message: String = "Document is not a valid Pix Key.",
                           val groups: Array<KClass<Any>> = [],
                           val payload: Array<KClass<Payload>> = []
)