package cz.quanti.cover.cache.domain

import cz.quanti.cover.cache.model.CacheConfiguration.cacheJson
import cz.quanti.cover.cache.model.CacheDatabaseRecord
import cz.quanti.cover.cache.model.CachedType
import cz.quanti.cover.cache.model.InternalCacheApi
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString

interface CacheSource {

    @InternalCacheApi
    fun read(type: CachedType, key: String): Flow<CacheDatabaseRecord?>

    @InternalCacheApi
    suspend fun write(type: CachedType, key: String, value: String)

    @InternalCacheApi
    suspend fun delete(type: CachedType, key: String)

    @InternalCacheApi
    suspend fun deleteAll(type: CachedType)
}

@InternalCacheApi
inline fun <reified Key> CacheSource.read(
    type: CachedType,
    key: Key,
): Flow<CacheDatabaseRecord?> {
    val keyJson = cacheJson.encodeToString(key)
    return read(type, keyJson)
}

@InternalCacheApi
suspend inline fun <reified Key, reified Value> CacheSource.write(
    type: CachedType,
    key: Key,
    value: Value,
) {
    val keyJson = cacheJson.encodeToString(key)
    val valueJson = cacheJson.encodeToString(value)
    return write(type, keyJson, valueJson)
}

@InternalCacheApi
suspend inline fun <reified Key> CacheSource.delete(
    type: CachedType,
    key: Key,
) {
    val keyJson = cacheJson.encodeToString(key)
    return delete(type, keyJson)
}
