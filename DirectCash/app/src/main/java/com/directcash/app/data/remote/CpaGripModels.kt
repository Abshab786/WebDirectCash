package com.directcash.app.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CpaGripResponse(
    @Json(name = "offers") val offers: List<CpaGripOffer>?
)

@JsonClass(generateAdapter = true)
data class CpaGripOffer(
    @Json(name = "offerid") val offerId: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "link") val link: String?,
    @Json(name = "payout") val payout: String?,
    @Json(name = "country") val country: String?
)

object CpaGripConfig {
    const val VAL_USD_TO_INR = 85.0
    const val USER_REWARD_PERCENTAGE = 0.60
}
