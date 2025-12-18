package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.SuiteDisplayName

@Suite
@SuiteDisplayName("BicoccApp API Integration Test Suite")
@SelectClasses(
    BicoccappAuthApiTest::class,
    BicoccappUserApiTest::class,
    BicoccappCalendarApiTest::class,
    BicoccappWizardApiTest::class,
    BicoccappMessagesApiTest::class,
    BicoccappCampusApiTest::class
)
class BicoccappApiTestSuite
