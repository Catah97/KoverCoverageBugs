package cz.quanti.cover.cache.domain

import cz.quanti.cover.cache.model.CacheConfiguration
import cz.quanti.cover.cache.model.CacheDatabaseRecord
import cz.quanti.cover.cache.model.CachedType
import cz.quanti.cover.cache.model.InternalCacheApi
import cz.quanti.cover.cache.model.KeyModel
import cz.quanti.cover.cache.model.OutputModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Test

@OptIn(InternalCacheApi::class)
class CacheSourceTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should read from cache store and encode key to string by cache json`() {
        mockkObject(CacheConfiguration)

        val cacheSource: CacheSource = mockk()
        val cachedType: CachedType = mockk()
        val key: KeyModel = mockk()
        val keyString = "keyString"
        val json: Json.Default = mockk()
        val expectedResult: Flow<CacheDatabaseRecord?> = mockk()

        every { CacheConfiguration.cacheJson } returns json
        every { json.encodeToString(key) } returns keyString
        every { cacheSource.read(cachedType, keyString) } returns expectedResult

        cacheSource.read(cachedType, key) shouldBe expectedResult
    }

    @Test
    fun `should write to cache store and encode key and value to string by cache json`() = runTest {
        mockkObject(CacheConfiguration)

        val cacheSource: CacheSource = mockk()
        val cachedType: CachedType = mockk()
        val key: KeyModel = mockk()
        val value: OutputModel = mockk()
        val keyString = "keyString"
        val valueString = "valueString"
        val json: Json.Default = mockk()

        every { CacheConfiguration.cacheJson } returns json
        every { json.encodeToString(key) } returns keyString
        every { json.encodeToString(value) } returns valueString
        coEvery { cacheSource.write(cachedType, keyString, valueString) } just runs

        cacheSource.write(cachedType, key, value)

        coVerify(exactly = 1) { cacheSource.write(cachedType, keyString, valueString) }
    }

    @Test
    fun `should delete from cache store and encode key to string by cache json`() = runTest {
        mockkObject(CacheConfiguration)

        val cacheSource: CacheSource = mockk()
        val cachedType: CachedType = mockk()
        val key: KeyModel = mockk()
        val keyString = "keyString"
        val json: Json.Default = mockk()

        every { CacheConfiguration.cacheJson } returns json
        every { json.encodeToString(key) } returns keyString
        coEvery { cacheSource.delete(cachedType, keyString) } just runs

        cacheSource.delete(cachedType, key)

        coVerify(exactly = 1) { cacheSource.delete(cachedType, keyString) }
    }
}
