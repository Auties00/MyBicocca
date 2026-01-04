package it.attendance100.mybicocca.data.api.esse3

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

/**
 * Test suite for all Esse3 API integration tests.
 *
 * This suite runs all API tests in order, sharing the same authenticated session
 * across all tests for efficiency.
 *
 * To run the tests:
 * ```
 * ./gradlew :data-api:esse3:test
 * ```
 *
 * Note: Tests require manual login via Chrome browser on first run.
 * The session is cached for the duration of the test run.
 */
@Suite
@SelectClasses(
    Esse3ProfileApiTest::class,
    Esse3CareerApiTest::class,
    Esse3ExamsApiTest::class,
    Esse3TaxesApiTest::class,
    Esse3InternshipApiTest::class,
    Esse3ProfileApiTest::class,
    Esse3QuestionnaireApiTest::class
)
class Esse3TestSuite
