package de.hennihaus.routes

import de.hennihaus.routes.CreditRoutes.AMOUNT_IN_EUROS_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.CREDIT_PATH
import de.hennihaus.routes.CreditRoutes.DELAY_IN_MILLISECONDS_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.PASSWORD_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.RATING_LEVEL_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.TERM_IN_MONTHS_QUERY_PARAMETER
import de.hennihaus.routes.CreditRoutes.USERNAME_QUERY_PARAMETER
import de.hennihaus.services.CreditService
import de.hennihaus.services.TrackingService
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import org.koin.java.KoinJavaComponent.getKoin

object CreditRoutes {
    const val CREDIT_PATH = "credit"

    const val AMOUNT_IN_EUROS_QUERY_PARAMETER = "amountInEuros"
    const val TERM_IN_MONTHS_QUERY_PARAMETER = "termInMonths"
    const val RATING_LEVEL_QUERY_PARAMETER = "ratingLevel"
    const val DELAY_IN_MILLISECONDS_QUERY_PARAMETER = "delayInMilliseconds"
    const val USERNAME_QUERY_PARAMETER = "username"
    const val PASSWORD_QUERY_PARAMETER = "password"
}

fun Route.registerCreditRoutes() {
    getCredit()
}

private fun Route.getCredit() = get(path = "/$CREDIT_PATH") {
    val credit = getKoin().get<CreditService>()
    val tracking = getKoin().get<TrackingService>()

    with(receiver = call) {
        val amountInEuros = request.queryParameters.getOrFail(name = AMOUNT_IN_EUROS_QUERY_PARAMETER)
        val termInMonths = request.queryParameters.getOrFail(name = TERM_IN_MONTHS_QUERY_PARAMETER)
        val ratingLevel = request.queryParameters.getOrFail(name = RATING_LEVEL_QUERY_PARAMETER)
        val delayInMilliseconds = request.queryParameters.getOrFail(name = DELAY_IN_MILLISECONDS_QUERY_PARAMETER)
        val username = request.queryParameters.getOrFail(name = USERNAME_QUERY_PARAMETER)
        val password = request.queryParameters.getOrFail(name = PASSWORD_QUERY_PARAMETER)

        val lendingRate = credit.calculateCredit(
            amountInEuros = amountInEuros.toInt(),
            termInMonths = termInMonths.toInt(),
            ratingLevel = ratingLevel,
            delayInMilliseconds = delayInMilliseconds.toLongOrNull(),
        )

        call.respond(
            message = lendingRate.also {
                tracking.trackRequest(
                    username = username,
                    password = password,
                )
            }
        )
    }
}
