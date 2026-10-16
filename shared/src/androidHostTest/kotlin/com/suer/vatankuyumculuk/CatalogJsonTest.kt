package com.suer.vatankuyumculuk

import com.suer.vatankuyumculuk.data.market.Catalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The admin panel reads webApp/src/webMain/resources/catalog.json; it must match [Catalog]. */
class CatalogJsonTest {
    @Test
    fun catalogJsonMatchesKotlinCatalog() {
        val file = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "webApp/src/webMain/resources/catalog.json") }
            .first { it.exists() }
        val products = Json.parseToJsonElement(file.readText()).jsonObject["products"]!!.jsonArray.map { it.jsonObject }

        assertEquals(Catalog.products.map { it.code }, products.map { it["code"]!!.jsonPrimitive.content })
        for ((p, j) in Catalog.products.zip(products)) {
            assertEquals(p.name, j["name"]!!.jsonPrimitive.content, p.code)
            assertEquals(p.variant, j["variant"]!!.jsonPrimitive.takeUnless { it.content == "null" && !it.isString }?.content, p.code)
            assertEquals(p.group.name, j["group"]!!.jsonPrimitive.content, p.code)
            assertEquals(p.decimals, j["decimals"]!!.jsonPrimitive.int, p.code)
            assertEquals(p.currency, j["currency"]!!.jsonPrimitive.content, p.code)
        }
    }
}
