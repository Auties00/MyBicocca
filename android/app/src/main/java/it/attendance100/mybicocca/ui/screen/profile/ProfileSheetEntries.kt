package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeader
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeaderInPage
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamsByYearPage
import it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade.HypotheticalGradePage
import it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade.hypotheticalGradeHeader

/**
 * The Profilo sheets: the hypothetical-average calculator and the libretto by year, both fed by
 * the shell-scoped [profileViewModel] the profile page renders from. [onOpenAppelli] runs after a
 * course detail's appelli action has closed the libretto sheet.
 */
fun EntryProviderScope<NavKey>.profileSheetEntries(
    profileViewModel: ProfileViewModel,
    onOpenAppelli: (courseKey: String) -> Unit,
) {
    entry<SheetRoute.HypotheticalGrade>(
        metadata = sheetHeader<SheetRoute.HypotheticalGrade> { key ->
            val rollup by profileViewModel.gradeRollup.collectAsStateWithLifecycle()
            hypotheticalGradeHeader(rollup = rollup.valueOrNull(), isWeighted = key.weighted)
        },
    ) { key ->
        val rollup by profileViewModel.gradeRollup.collectAsStateWithLifecycle()
        val stats by profileViewModel.stats.collectAsStateWithLifecycle()
        val current = stats.valueOrNull()
        HypotheticalGradePage(
            rollup = rollup.valueOrNull(),
            currentArithmetic = current?.arithmeticAverage,
            currentWeighted = current?.weightedAverage,
            isWeighted = key.weighted,
        )
    }
    entry<SheetRoute.ExamsByYear>(metadata = sheetHeaderInPage()) { key ->
        val rows by profileViewModel.transcriptRows.collectAsStateWithLifecycle()
        val stats by profileViewModel.stats.collectAsStateWithLifecycle()
        val prerequisiteStatuses by profileViewModel.prerequisiteStatuses.collectAsStateWithLifecycle()
        ExamsByYearPage(
            rows = rows.valueOrNull().orEmpty(),
            stats = stats.valueOrNull(),
            initialMode = key.mode,
            prerequisiteStatuses = prerequisiteStatuses,
            onOpenAppelli = onOpenAppelli,
        )
    }
}
