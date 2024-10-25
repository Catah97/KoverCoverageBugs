package cz.quanti.cover.cache.model

import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

@InternalCacheApi
object CacheConfiguration {
    val cacheTimeout = 10.minutes
    val cacheJson = Json
}
