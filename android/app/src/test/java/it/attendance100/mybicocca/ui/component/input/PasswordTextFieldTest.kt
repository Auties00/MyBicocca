package it.attendance100.mybicocca.ui.component.input

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PasswordTextFieldTest {

    @Test
    fun `a character typed at the end is revealed`() {
        assertThat(singleInsertionIndex("abc", "abcd")).isEqualTo(3)
    }

    @Test
    fun `a character typed in the middle is revealed where it went`() {
        assertThat(singleInsertionIndex("abd", "abcd")).isEqualTo(2)
        assertThat(singleInsertionIndex("", "a")).isEqualTo(0)
    }

    @Test
    fun `a repeated letter reveals the typed position`() {
        assertThat(singleInsertionIndex("aa", "aaa")).isEqualTo(2)
    }

    @Test
    fun `pastes, autofill, deletions and replacements stay hidden`() {
        assertThat(singleInsertionIndex("ab", "abcd")).isEqualTo(-1)
        assertThat(singleInsertionIndex("", "Secret1!")).isEqualTo(-1)
        assertThat(singleInsertionIndex("abc", "ab")).isEqualTo(-1)
        assertThat(singleInsertionIndex("abc", "xyzw")).isEqualTo(-1)
    }
}
