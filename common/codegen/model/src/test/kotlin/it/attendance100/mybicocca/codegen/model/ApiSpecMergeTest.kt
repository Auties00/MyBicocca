package it.attendance100.mybicocca.codegen.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ApiSpecMergeTest {
    private val status = EnumType("DemoStatus", listOf(EnumValue("Open", "open")))

    private fun part(
        module: String,
        enums: List<EnumType> = emptyList(),
        servers: List<Server> = emptyList(),
        id: String = "demo",
    ) = ApiSpec(id, "Demo", listOf(ApiModule(module, "/", emptyList(), emptyList())), enums, servers)

    @Test
    fun `a single spec is the API`() {
        val only = part("Items")
        assertSame(only, ApiSpec.merge(listOf(only)))
    }

    @Test
    fun `joins the modules, enums and servers of every part`() {
        val rest = part("Items", enums = listOf(status))
        val pages = part("Pages", enums = listOf(status), servers = listOf(Server("Web", "https://example.com")))
        val api = ApiSpec.merge(listOf(rest, pages))
        assertEquals(listOf("Items", "Pages"), api.modules.map { it.name })
        assertEquals(listOf(status), api.enums)
        assertEquals(listOf("Web"), api.servers.map { it.name })
    }

    @Test
    fun `refuses parts of different APIs`() {
        val error = assertThrows<IllegalArgumentException> { ApiSpec.merge(listOf(part("Items"), part("Pages", id = "other"))) }
        assertContains(error.message.orEmpty(), "share its id")
    }

    @Test
    fun `refuses a declaration the parts disagree on`() {
        val other = status.copy(values = listOf(EnumValue("Closed", "closed")))
        val error = assertThrows<IllegalArgumentException> {
            ApiSpec.merge(listOf(part("Items", enums = listOf(status)), part("Pages", enums = listOf(other))))
        }
        assertContains(error.message.orEmpty(), "Enum 'DemoStatus'")
    }

    @Test
    fun `takes the client of the part describing it`() {
        val client = ClientSpec(baseUrl = "https://example.com/api", emptyStatuses = setOf(404))
        val api = ApiSpec.merge(listOf(part("Items"), part("Client").copy(client = client)))
        assertEquals(client, api.client)
    }

    @Test
    fun `refuses two parts describing the client`() {
        val client = ClientSpec(baseUrl = "https://example.com/api")
        val error = assertThrows<IllegalArgumentException> {
            ApiSpec.merge(listOf(part("Items").copy(client = client), part("Pages").copy(client = client)))
        }
        assertContains(error.message.orEmpty(), "describe its client")
    }
}
