package it.attendance100.mybicocca.ui.component.modal

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.update.UpdateModalKind
import org.junit.Test

/**
 * The direction of the channel-change undo, which is the one thing here that is silently wrong
 * rather than visibly broken if it is inverted: backing out of "switch to nightly" would leave the
 * beta switch *on*, and the user would be on the channel they just declined.
 */
class UpdateModalKindTest {

    @Test
    fun `an ordinary update has nothing to undo`() {
        assertThat(UpdateModalKind.Standard.channelSwitch { }).isNull()
    }

    @Test
    fun `backing out of a switch to nightly turns the beta switch back off`() {
        var restoredTo: Boolean? = null

        val switch = UpdateModalKind.SwitchToNightly.channelSwitch { restoredTo = it }
        switch!!.onStay()

        assertThat(switch).isInstanceOf(ChannelSwitch.ToNightly::class.java)
        assertThat(restoredTo).isFalse()
    }

    @Test
    fun `backing out of a restore to stable turns the beta switch back on`() {
        var restoredTo: Boolean? = null

        val switch = UpdateModalKind.RestoreStable.channelSwitch { restoredTo = it }
        switch!!.onStay()

        assertThat(switch).isInstanceOf(ChannelSwitch.ToStable::class.java)
        assertThat(restoredTo).isTrue()
    }

    /** Every kind is accounted for, so adding a fourth is a failing test rather than a silent null. */
    @Test
    fun `only the ordinary kind maps to no undo`() {
        val withoutUndo = UpdateModalKind.entries.filter { it.channelSwitch { } == null }

        assertThat(withoutUndo).containsExactly(UpdateModalKind.Standard)
    }
}
