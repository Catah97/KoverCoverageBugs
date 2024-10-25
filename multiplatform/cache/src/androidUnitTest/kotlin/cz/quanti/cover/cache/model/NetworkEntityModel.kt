package cz.quanti.cover.cache.model

import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkEntityModel(
    val text: String = "text",
)
