package dev.martins.marcio.studies.springbooti18n

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonMappingException
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.UriComponentsBuilder
import java.util.Locale
import kotlin.reflect.jvm.javaMethod

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    @Value("\${app.api-errors.baseUrl}")
    private var apiErrorsBaseUrl: String = ""

    @Autowired
    private lateinit var messageSource: MessageSource

    /**
     * Handle exception when there is a missing property in the JSON payload
     */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = when (val cause = ex.cause) {
            is JsonMappingException -> cause.path.map {
                MyProblemDetail.Error(
                    detail = messageSource.getMessage(
                        "jakarta.validation.constraints.NotNull.message", null, request.locale
                    ),
                    pointer = it.fieldName
                )
            }

            else -> {
                listOf(MyProblemDetail.Error(detail = "Unmapped error: ${cause?.message}", pointer = "Unknown"))
            }
        }

        return handleValidationFailure(ProblemDetail.forStatus(status), errors, request.locale)
    }

    /**
     * For jakarta validations using @Valid annotation
     */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = ex.fieldErrors
            .map {
                MyProblemDetail.Error(
                    detail = it.defaultMessage ?: "Default message not provided",
                    pointer = it.field,
                )
            }

        return handleValidationFailure(ex.body, errors, request.locale)
    }

    private fun handleValidationFailure(
        otherProblemDetail: ProblemDetail,
        errors: List<MyProblemDetail.Error>,
        locale: Locale,
    ): ResponseEntity<Any> {
        val problemDetail = MyProblemDetail(otherProblemDetail, errors)
        problemDetail.title = messageSource.getMessage(
            "dev.martins.marcio.springbooti18n.ProblemDetail.ValidationFailure.title", null, locale
        )
        problemDetail.detail = null

        val baseURI = UriComponentsBuilder.fromHttpUrl(apiErrorsBaseUrl)
        problemDetail.type = MvcUriComponentsBuilder
            .fromMethod(
                baseURI,
                ApiErrorsController::class.java,
                ApiErrorsController::validationFailureV1.javaMethod!!,
                null,
                Locale.ENGLISH
            )
            .build()
            .toUri()

        return ResponseEntity(problemDetail, HttpStatus.BAD_REQUEST)
    }

    @Suppress("unused")
    class MyProblemDetail(
        @Schema(hidden = true) otherProblemDetail: ProblemDetail,
        val errors: List<Error>?,
    ) : ProblemDetail(otherProblemDetail) {
        @Schema(hidden = true)
        override fun getProperties(): MutableMap<String, Any>? = super.getProperties()

        @JsonInclude(JsonInclude.Include.NON_NULL)
        data class Error(
            val detail: String,
            val pointer: String?,
            val errorCode: String?,
        ) {
            constructor(detail: String, pointer: String?) : this(
                detail = errorCodeRegex.replace(detail, ""),
                pointer = pointer,
                errorCode = detail.let { errorCodeRegex.find(it)?.groups?.get(1)?.value },
            )

            companion object {
                val errorCodeRegex = "^\\[([0-9]{5})] ".toRegex()
            }
        }
    }
}

