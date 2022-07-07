package de.hennihaus.objectmothers

import de.hennihaus.models.Bank
import de.hennihaus.models.CreditConfiguration
import de.hennihaus.models.Group
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.getCreditConfiguration

object BankObjectMother {

    const val SCHUFA_BANK_NAME = "schufa"
    const val SYNC_BANK_NAME = "vbank"
    const val JMS_BANK_A_NAME = "jmsBankA"

    const val DEFAULT_THUMBNAIL_URL = "http://localhost:8085/picture.jpg"
    const val DEFAULT_IS_ACTIVE = true

    fun getSchufaBank(
        jmsQueue: String = SCHUFA_BANK_NAME,
        name: String = SCHUFA_BANK_NAME,
        thumbnailUrl: String = DEFAULT_THUMBNAIL_URL,
        isAsync: Boolean = false,
        isActive: Boolean = DEFAULT_IS_ACTIVE,
        creditConfiguration: CreditConfiguration? = null,
        groups: List<Group> = emptyList(),
    ) = Bank(
        jmsQueue = jmsQueue,
        name = name,
        thumbnailUrl = thumbnailUrl,
        isAsync = isAsync,
        isActive = isActive,
        creditConfiguration = creditConfiguration,
        groups = groups,
    )

    fun getSyncBank(
        jmsQueue: String = SYNC_BANK_NAME,
        name: String = SYNC_BANK_NAME,
        thumbnailUrl: String = "http://localhost:8085/picture.jpg",
        isAsync: Boolean = false,
        isActive: Boolean = DEFAULT_IS_ACTIVE,
        creditConfiguration: CreditConfiguration? = getCreditConfiguration(),
        groups: List<Group> = emptyList(),
    ) = Bank(
        jmsQueue = jmsQueue,
        name = name,
        thumbnailUrl = thumbnailUrl,
        isAsync = isAsync,
        isActive = isActive,
        creditConfiguration = creditConfiguration,
        groups = groups,
    )
}
