package dev.martins.marcio.studies.springbooti18n

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.Locale

@Hidden
@Controller
@RequestMapping("/api-docs/errors")
class ApiErrorsController {
    @GetMapping("/v1/validation-failure")
    fun validationFailureV1(model: Model?, locale: Locale): String {
        model?.addAttribute("errorFile", "/api-docs/errors/validation-failure-v1")
        model?.addAttribute("locale", locale)
        return "/api-docs/errors/index-v1"
    }
}
