package cz.quanti.cover.cache.domain

import cz.quanti.cover.cache.model.CacheConfiguration.cacheJson
import cz.quanti.cover.cache.model.CacheDatabaseRecord
import cz.quanti.cover.cache.model.CachedType
import cz.quanti.cover.cache.model.InternalCacheApi
import kotlinx.coroutines.flow.Flow

interface CacheStoreFactory {

    @InternalCacheApi
    fun <Key : Any, NetworkEntity : Any, Output : Any> createStore(
        fetch: suspend (key: Key) -> NetworkEntity,
        reader: (Key) -> Flow<CacheDatabaseRecord?>,
        networkFlowMapping: suspend (CacheDatabaseRecord, Key) -> Output,
        writer: suspend (Key, NetworkEntity) -> Unit,
        delete: (suspend (Key) -> Unit),
        deleteAll: (suspend () -> Unit),
    ): CacheStore<Key, Output>
}

@OptIn(InternalCacheApi::class)
inline fun <reified Key : Any, reified NetworkEntity : Any, reified Output : Any> CacheStoreFactory.createStore(
    cacheSource: CacheSource,
    cachedType: CachedType,
    noinline fetch: suspend (key: Key) -> NetworkEntity,
    crossinline networkFlowMapping: suspend (NetworkEntity, Key) -> Output,
): CacheStore<Key, Output> {
    return createStore(
        fetch = fetch,
        reader = {
            println("cacheSource.read")
            cacheSource.read(cachedType, it)
        },
        networkFlowMapping = { record, key ->
            println("networkFlowMapping")
            val json = record.value.let { value ->
                cacheJson.decodeFromString<NetworkEntity>(value)
            }
            networkFlowMapping(json, key)
        },
        writer = { key, value ->
            println("cacheSource.write")
            cacheSource.write(cachedType, key, value)
        },
        delete = { key ->
            println("cacheSource.delete")
            cacheSource.delete(cachedType, key)
        },
        deleteAll =  // Kover 0.8.3 thinks that this block is not tested even it is.
        {
            println("cacheSource.deleteAll")
            cacheSource.deleteAll(cachedType)
        },
    )
}
