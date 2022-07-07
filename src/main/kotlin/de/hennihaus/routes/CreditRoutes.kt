package de.hennihaus.routes

import de.hennihaus.routes.resources.CreditResource
import de.hennihaus.services.CreditService
import de.hennihaus.services.TrackingService
import de.hennihaus.services.resourceservices.CreditResourceService
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.java.KoinJavaComponent.getKoin

fun Route.registerCreditRoutes() {
    getCredit()
}

private fun Route.getCredit() = get<CreditResource> { request ->
    val creditResource = getKoin().get<CreditResourceService>()
    val credit = getKoin().get<CreditService>()
    val tracking = getKoin().get<TrackingService>()

    creditResource.validate(resource = request).also {
        val lendingRate = credit.calculateCredit(
            amountInEuros = requireNotNull(value = it.amountInEuros),
            termInMonths = requireNotNull(value = it.termInMonths),
            ratingLevel = requireNotNull(value = it.ratingLevel),
            delayInMilliseconds = it.delayInMilliseconds,
        )
        call.respond(
            message = lendingRate.also { _ ->
                tracking.trackRequest(
                    username = it.username ?: "",
                    password = it.password ?: "",
                )
            }
        )
    }
}
