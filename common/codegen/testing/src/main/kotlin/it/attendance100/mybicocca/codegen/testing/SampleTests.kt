package it.attendance100.mybicocca.codegen.testing

import it.attendance100.mybicocca.codegen.core.LiveTestsConfig
import it.attendance100.mybicocca.codegen.core.TestPlan
import it.attendance100.mybicocca.codegen.core.TestPlanner

/**
 * A `tests.yaml` for the [SampleSpecs.demo] API using every feature of the live tests: an account
 * with headers, credentials, API parameters and settings, defaults, values from other calls,
 * combinations, expectations, allowed errors, volatile results, and writes undone by a call or a
 * restore.
 */
object SampleTests {
    val yaml = """
        account:
          env: [DEMO_TOKEN]
          roles: [STUDENT]
          headers:
            Authorization: "Basic {base64:demo:{env:DEMO_TOKEN}}"
          parameters: { tenant: acme, region: eu }
          auth:
            Token: { token: "{env:DEMO_TOKEN}" }
            Session: { token: "{env:DEMO_TOKEN}" }
          settings: { language: it }
          anonymous: [Shared.ping]
        limits: { requestsPerSecond: 4, maxRequests: 100, casesPerOperation: 4 }
        defaults:
          itemId: { from: Search.searchItems, with: { query: lamp }, pick: "[*].count" }
        operations:
          Items.getItems:
            inputs: { limit: { oneOf: [10, 50] } }
            expect:
              - nonEmpty: "[*].name"
              - count: { path: "[*]", min: 1, max: 50 }
              - matches: { path: "[*].name", regex: '\S' }
          Items.downloadAttachment:
            skip: Paths come from attachments, which no read lists.
          Items.createItem:
            inputs:
              body: { nome: "Live test {today}", proprietario: { ownerId: 1 } }
            undo: { call: Items.deleteItem, with: { itemId: ${'$'}result.name } }
            verify:
              call: Search.searchItems
              with: { query: ${'$'}result.name }
              contains: { path: "[*].name", value: ${'$'}result.name }
          Items.renameItem:
            inputs: { itemId: "42", name: Renamed }
            undo:
              restore: { read: Search.searchItems, with: { query: "42" }, pick: "[0].name", into: name }
          Search.searchItems:
            inputs: { query: { oneOf: [lamp, desk] } }
            errors: ["404", not_found]
          Calendar.getCalendar:
            inputs: { day: "{today+1}" }
            volatile: true
          Calendar.runQuery:
            inputs: { ids: [1, 2] }
          Pages.getRooms:
            inputs: { buildings: { oneOf: [[U6], [U6, U7]] }, day: "{today}" }
          Pages.getRoom:
            inputs: { id: 7 }
          Pages.getVideoId:
            inputs: { id: 3 }
    """.trimIndent()

    val config: LiveTestsConfig get() = LiveTestsConfig.parse(yaml)

    /** The live tests [yaml] plans for [SampleSpecs.demo]. */
    val plan: TestPlan get() = TestPlanner(SampleSpecs.demo, config).plan()
}
