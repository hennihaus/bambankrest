package de.hennihaus.plugins

import de.hennihaus.configurations.Configuration.API_VERSION
import de.hennihaus.routes.CreditRoutes.AMOUNT_IN_EUROS_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.CREDIT_PATH
import de.hennihaus.routes.CreditRoutes.DELAY_IN_MILLISECONDS_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.PASSWORD_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.RATING_LEVEL_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.TERM_IN_MONTHS_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.USERNAME_QUERY_PARAMETER
import de.hennihaus.services.validationservices.CreditValidationService
import de.hennihaus.services.validationservices.resources.CreditResource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.request.uri
import org.koin.java.KoinJavaComponent.getKoin

fun Application.configureValidation() = install(plugin = UrlValidation) {
    validate(path = "/$CREDIT_PATH") {
        val resource = CreditResource(
            amountInEuros = it.request.queryParameters[AMOUNT_IN_EUROS_QUERY_PARAMETER],
            termInMonths = it.request.queryParameters[TERM_IN_MONTHS_QUERY_PARAMETER],
            ratingLevel = it.request.queryParameters[RATING_LEVEL_QUERY_PARAMETER],
            delayInMilliseconds = it.request.queryParameters[DELAY_IN_MILLISECONDS_QUERY_PARAMETER],
            username = it.request.queryParameters[USERNAME_QUERY_PARAMETER],
            password = it.request.queryParameters[PASSWORD_QUERY_PARAMETER],
        )

        getKoin().get<CreditValidationService>().validateUrl(resource = resource)
    }
}

private val UrlValidation = createRouteScopedPlugin(
    name = "UrlValidation",
    createConfiguration = ::UrlValidationConfiguration,
) {
    val validations = pluginConfig.validations
    val apiVersion = applicationConfig?.property(path = API_VERSION)?.getString() ?: ""

    onCall { call ->
        validations.toList()
            .find { (path, _) ->
                call.request.uri.replace(oldValue = "/$apiVersion", newValue = "").startsWith(prefix = path)
            }
            ?.also { (_, validation) ->
                validation(call)
            }
    }
}

private class UrlValidationConfiguration {
    var validations: MutableMap<String, suspend (ApplicationCall) -> Unit> = mutableMapOf()

    fun validate(path: String, validation: suspend (ApplicationCall) -> Unit) {
        validations += path to validation
    }
}
