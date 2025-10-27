package com.thiago.android.breakingnews.data.response.source

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceResponse(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null
)