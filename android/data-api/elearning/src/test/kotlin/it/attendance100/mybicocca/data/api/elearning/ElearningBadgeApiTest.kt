package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningBadgeApiTest : ElearningTestBase() {

    @Test
    fun `getUserBadges returns badges`() = runBlocking {
        val badgesResponse = api.badges.getUserBadges(wsToken)
        
        assertNotNull(badgesResponse)
        assertNotNull(badgesResponse.badges)
        println("Found ${badgesResponse.badges.size} badges")
    }
}
