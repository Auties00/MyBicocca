package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AssignmentDetailScreen(
    assignId: Int,
    courseId: Int,
    onOpenFile: (url: String, fileName: String?) -> Unit = { _, _ -> },
    viewModel: AssignmentDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is AssignmentDetailOneShotEvent.OpenFile -> onOpenFile(event.url, event.fileName)
                is AssignmentDetailOneShotEvent.RefreshFailed -> Unit
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") assignId
    @Suppress("UNUSED_PARAMETER") courseId
}
