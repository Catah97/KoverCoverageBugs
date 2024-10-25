package cz.quanti.cover.cache.domain

import cz.quanti.cover.cache.model.CacheConfiguration
import cz.quanti.cover.cache.model.CacheDatabaseRecord
import cz.quanti.cover.cache.model.CachedType
import cz.quanti.cover.cache.model.InternalCacheApi
import cz.quanti.cover.cache.model.KeyModel
import cz.quanti.cover.cache.model.NetworkEntityModel
import cz.quanti.cover.cache.model.OutputModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.After
import org.junit.Test

private typealias Key = KeyModel
private typealias NetworkEntity = NetworkEntityModel
private typealias Output = OutputModel

@OptIn(InternalCacheApi::class)
class CacheStoreFactoryTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should create store correctly`() {
        val cacheKey: Key = mockk()
        val cacheSource: CacheSource = mockk()
        val cachedType: CachedType = mockk()
        val networkEntity: NetworkEntity = NetworkEntity()
        val storeFactory: CacheStoreFactory = mockk()
        val expectedResult = mockk<CacheStore<Key, Output>>()
        val expectedReadFeedLambdaResult: Flow<CacheDatabaseRecord?> = mockk()
        val fetch: suspend (key: Key) -> NetworkEntity = mockk()
        val networkFlowMapping: suspend (NetworkEntity, Key) -> Output = mockk()

        every { cacheSource.read(cachedType, cacheKey) } returns expectedReadFeedLambdaResult
        coEvery { cacheSource.write(cachedType, cacheKey, networkEntity) } just runs
        coEvery { cacheSource.delete(cachedType, cacheKey) } just runs
        coEvery { cacheSource.deleteAll(cachedType) } just runs
        every {
            storeFactory.createStore<Key, NetworkEntity, Output>(
                fetch = fetch,
                reader = any(),
                networkFlowMapping = any(),
                writer = any(),
                delete = any(),
                deleteAll = any(),
            )
        } answers {
            /**
             * When I used coAnswers test crash with java.lang.NullPointerException
             * Probably bug in mockk this is workaround.
             */
            runBlocking {
                arg<(Key) -> Flow<CacheDatabaseRecord?>>(1)
                    .invoke(cacheKey) shouldBe expectedReadFeedLambdaResult

                val output: Output = mockk()
                val record = mockk<CacheDatabaseRecord> {
                    every { value } returns CacheConfiguration.cacheJson.encodeToString(networkEntity)
                }
                coEvery { networkFlowMapping(networkEntity, cacheKey) } returns output

                arg<suspend (CacheDatabaseRecord, Key) -> Output>(2)
                    .invoke(record, cacheKey) shouldBe output

                arg<suspend (Key, NetworkEntity) -> Unit>(3)
                    .invoke(cacheKey, networkEntity)

                arg<suspend (Key) -> Unit>(4)
                    .invoke(cacheKey)

                arg<suspend () -> Unit>(5)
                    .invoke()
            }

            expectedResult
        }

        storeFactory.createStore(
            cacheSource,
            cachedType,
            fetch,
            networkFlowMapping,
        ) shouldBe expectedResult

        coVerify(exactly = 1) { cacheSource.write(cachedType, cacheKey, networkEntity) }
        coVerify(exactly = 1) { cacheSource.delete(cachedType, cacheKey) }
        coVerify(exactly = 1) { cacheSource.deleteAll(cachedType) }
    }
}
