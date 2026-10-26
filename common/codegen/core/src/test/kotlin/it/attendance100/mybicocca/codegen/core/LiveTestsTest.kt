package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.Effect
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import it.attendance100.mybicocca.codegen.testing.SampleTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PairwiseTest {
    @Test
    fun `covers every pair of values, starting from the first of each`() {
        val sizes = listOf(3, 4, 2, 5)
        val rows = Pairwise.rows(sizes, limit = 100, seed = 1)
        assertEquals(List(sizes.size) { 0 }, rows.first())
        for (i in sizes.indices) for (j in i + 1 until sizes.size) {
            for (a in 0 until sizes[i]) for (b in 0 until sizes[j]) {
                assertTrue(rows.any { it[i] == a && it[j] == b }, "($i=$a, $j=$b) is never tried")
            }
        }
        assertTrue(rows.size < sizes.fold(1) { product, size -> product * size }, "Pairs need fewer rows than every combination")
    }

    @Test
    fun `lists every combination when they fit, and stops at the limit`() {
        assertEquals(listOf(listOf(0, 0), listOf(0, 1), listOf(1, 0), listOf(1, 1)), Pairwise.rows(listOf(2, 2), limit = 4, seed = 1))
        assertEquals(3, Pairwise.rows(listOf(5, 5, 5), limit = 3, seed = 1).size)
        assertEquals(listOf(emptyList<Int>()), Pairwise.rows(emptyList(), limit = 3, seed = 1))
        assertEquals(Pairwise.rows(listOf(4, 3, 3), limit = 6, seed = 7), Pairwise.rows(listOf(4, 3, 3), limit = 6, seed = 7))
    }
}

class TemplatesTest {
    @Test
    fun `parses placeholders, nested in base64`() {
        assertEquals(
            listOf(
                Templates.Part.Literal("Basic "),
                Templates.Part.Base64(listOf(Templates.Part.Env("USER"), Templates.Part.Literal(":"), Templates.Part.Env("PASSWORD"))),
            ),
            Templates.parse("Basic {base64:{env:USER}:{env:PASSWORD}}"),
        )
        assertEquals(listOf(Templates.Part.Today(-3, "yyyy")), Templates.parse("{today-3:yyyy}"))
        assertEquals(listOf("USER", "PASSWORD"), Templates.envVariables("{base64:{env:USER}:{env:PASSWORD}}"))
    }

    @Test
    fun `rejects unknown and unbalanced placeholders`() {
        assertThrows<IllegalArgumentException> { Templates.check("{tomorrow}") }
        assertThrows<IllegalArgumentException> { Templates.check("{env:A") }
        assertThrows<IllegalArgumentException> { Templates.check("a}b") }
    }
}

class LiveTestsConfigTest {
    @Test
    fun `reads values of every kind`() {
        val config = LiveTestsConfig.parse(
            """
            operations:
              A.b:
                inputs:
                  left: null
                  number: 3
                  text: "{today}"
                  ref: ${'$'}result.items[0].id
                  pick: { from: A.list, pick: "[*].id", where: { active: true } }
                  options: { oneOf: [1, 2] }
                  json: { a: [1] }
            """.trimIndent()
        )
        val inputs = config.operations.getValue("A.b").inputs
        assertEquals(ValueSpec.Omit, inputs["left"])
        assertTrue(inputs["number"] is ValueSpec.Scalar)
        assertEquals(ValueSpec.Text("{today}"), inputs["text"])
        assertEquals(ValueSpec.Ref("result", "items[0].id"), inputs["ref"])
        assertEquals("A.list", (inputs["pick"] as ValueSpec.Pick).from)
        assertEquals(2, (inputs["options"] as ValueSpec.OneOf).values.size)
        assertTrue(inputs["json"] is ValueSpec.Json)
    }

    @Test
    fun `rejects unknown keys`() {
        val error = assertThrows<IllegalArgumentException> { LiveTestsConfig.parse("limits: { maxRequest: 3 }") }
        assertTrue("maxRequest" in error.message!!)
    }
}

class EffectsOverlayTest {
    @Test
    fun `sets the effects it names and rejects unknown operations`(@TempDir dir: Path) {
        dir.resolve(EffectsOverlay.FILE_NAME).writeText("""{ "Items.getItems": { "effect": "benign", "reason": "Logs the search." } }""")
        val spec = EffectsOverlay.apply(SampleSpecs.demo, dir)
        assertEquals(Effect.BENIGN, spec.modules.first().operations.first { it.name == "getItems" }.effect)

        dir.resolve(EffectsOverlay.FILE_NAME).writeText("""{ "Items.missing": { "effect": "read", "reason": "-" } }""")
        assertThrows<IllegalArgumentException> { EffectsOverlay.apply(SampleSpecs.demo, dir) }
    }
}

class TestPlannerTest {
    private val plan = SampleTests.plan

    private fun cases(operation: String) = plan.cases.filter { it.operation.id == operation }

    private fun skip(operation: String) = plan.skips.firstOrNull { it.operation == operation }?.reason

    @Test
    fun `skips irreversible operations, missing credentials and what tests yaml skips`() {
        assertEquals("irreversible", skip("Items.deleteItem"))
        assertEquals("needs credentials of Key in account.auth", skip("Search.touch"))
        assertEquals("Paths come from attachments, which no read lists.", skip("Items.downloadAttachment"))
    }

    @Test
    fun `skips operations needing roles the account lacks`() {
        val config = SampleTests.config.copy(account = SampleTests.config.account.copy(roles = listOf("TEACHER")))
        val plan = TestPlanner(SampleSpecs.demo, config).plan()
        assertTrue(plan.skips.first { it.operation == "Items.getItems" }.reason.startsWith("needs one of the roles"))
    }

    @Test
    fun `takes ids from other results by name`() {
        val ownerId = cases("Items.getItems").first().call.args.getValue("ownerId") as Arg.Pick
        assertEquals("Pages.getOwners", ownerId.source.operation.id)
        assertEquals("[*].id", ownerId.path.toString())
    }

    @Test
    fun `takes values from the same element of a result together`() {
        val rowPick = cases("Calendar.runQuery").first().call.rowPicks.single()
        assertEquals("Search.searchItems", rowPick.source.operation.id)
        assertEquals("[*]", rowPick.rows.toString())
        assertEquals(mapOf("ids" to "owner.id", "tags" to "name"), rowPick.fields.mapValues { it.value.toString() })
        assertTrue(cases("Items.getItems").all { it.call.rowPicks.isEmpty() }, "Values from different calls aren't tied")
    }

    @Test
    fun `leaves out an optional input whose default nothing provides`() {
        // The call of Search.searchItems the page comes from can't take it from its own result.
        val page = "  page: { from: Search.searchItems, with: { query: lamp }, pick: \"[*].count\" }\n"
        val plan = TestPlanner(SampleSpecs.demo, LiveTestsConfig.parse(SampleTests.yaml.replace("defaults:\n", "defaults:\n$page"))).plan()
        val source = plan.sources.single { it.operation.id == "Search.searchItems" }
        assertEquals(setOf("query"), source.call.args.keys - Input.AUTH)
        assertTrue(plan.cases.any { it.operation.id == "Search.searchItems" && "page" in it.call.args })
    }

    @Test
    fun `tries pairs of values up to the limit, the first sending only what is needed`() {
        val getItems = cases("Items.getItems").filter { it.kind == CaseKind.SMOKE || it.kind == CaseKind.COMBINATION }
        assertEquals(4, getItems.size)
        assertEquals(CaseKind.SMOKE, getItems.first().kind)
        assertNull(getItems.first().call.args["status"])
        assertEquals(setOf("A", "C"), getItems.mapNotNull { (it.call.args["status"] as? Arg.Literal)?.value?.content }.toSet())
        assertEquals(listOf(0, 1, 2, 3), getItems.map { it.index })
    }

    @Test
    fun `plans the cases expecting errors`() {
        assertEquals(Arg.Unknown, cases("Items.getItems").single { it.kind == CaseKind.UNKNOWN_ID }.call.args["ownerId"])
        val invalid = cases("Calendar.runQuery").single { it.kind == CaseKind.INVALID_CREDENTIALS }
        assertTrue(invalid.call.args[Input.AUTH] is Arg.InvalidCredentials)
        assertTrue(cases("Shared.ping").single { it.kind == CaseKind.ANONYMOUS }.call.anonymous)
    }

    @Test
    fun `compares only reads whose results stay the same`() {
        assertTrue(cases("Pages.getOwners").single().compare)
        assertTrue(cases("Calendar.getCalendar").none { it.compare }, "tests.yaml marks it volatile")
        assertTrue(cases("Items.getItemPdf").none { it.compare }, "Bytes have no result to compare")
    }

    @Test
    fun `undoes writes by a call or by restoring the saved value`() {
        val create = cases("Items.createItem").single()
        val write = create.write!!
        assertEquals("Items.deleteItem", write.undo.operation.id)
        assertEquals(Arg.Ref.Root.RESULT, (write.undo.args.getValue("itemId") as Arg.Ref).root)
        assertEquals("Search.searchItems", write.verify!!.first.operation.id)
        assertTrue(create in plan.replays)

        val rename = cases("Items.renameItem").single().write!!
        assertEquals("[0].name", rename.snapshot!!.second.toString())
        assertEquals(Arg.Ref.Root.BEFORE, (rename.undo.args.getValue("name") as Arg.Ref).root)
    }

    @Test
    fun `gathers the account's environment variables, and the placeholders of JSON values`() {
        assertEquals(listOf("DEMO_TOKEN"), plan.account.env)
        assertTrue((cases("Items.createItem").single().call.args.getValue(Input.BODY) as Arg.Json).templated)
    }

    @Test
    fun `fails on mistakes in tests yaml`() {
        fun planOf(yaml: String) = TestPlanner(SampleSpecs.demo, SampleTests.config.copy(operations = LiveTestsConfig.parse(yaml).operations)).plan()
        val unknown = assertThrows<IllegalArgumentException> { planOf("operations: { Items.missing: { skip: x } }") }
        assertTrue("Items.missing" in unknown.message!!, unknown.message)
        val field = assertThrows<IllegalArgumentException> {
            planOf("operations: { Items.getItemLog: { inputs: { itemId: { from: Pages.getOwners, pick: \"[*].missing\" } } } }")
        }
        assertTrue("no field 'missing'" in field.message!!, field.message)
        val value = assertThrows<IllegalArgumentException> { planOf("operations: { Pages.getRoom: { inputs: { id: abc } } }") }
        assertTrue("isn't a value of" in value.message!!, value.message)
    }
}
