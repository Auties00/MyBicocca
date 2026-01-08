package it.attendance100.mybicocca.data.api.elearning

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningBadgeApiTest : ElearningTestBase() {

    @Test
    suspend fun getUserBadges() {
        val badgesResponse = api.badges.getUserBadges(session.wsToken)
        assertNotNull(badgesResponse)
        assertNotNull(badgesResponse.badges)
    }
}
