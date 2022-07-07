package de.hennihaus.services.callservices.resources

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object BankPaths {
    const val BANKS_PATH = "/banks"
    const val JMS_QUEUE_PATH = "/{jmsQueue}"
}

@Serializable
@Resource(BankPaths.BANKS_PATH)
class Banks {

    @Serializable
    @Resource(BankPaths.JMS_QUEUE_PATH)
    data class JmsQueue(val parent: Banks = Banks(), val jmsQueue: String)
}
