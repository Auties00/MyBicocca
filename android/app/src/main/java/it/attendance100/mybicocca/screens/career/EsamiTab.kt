package it.attendance100.mybicocca.screens.career

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.ui.theme.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExamsTab(passedExams: List<Exam>, pendingExams: List<Exam>) {

  // State for expanding/collapsing
  var isPassedExpanded by remember { mutableStateOf(true) }
  var isPendingExpanded by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
  ) {
    // Passed
    stickyHeader {
      ExpandableHeader(
        title = "Passed Exams",
        count = passedExams.size,
        isExpanded = isPassedExpanded,
        titleColor = MaterialTheme.colorScheme.primary,
        onToggle = { isPassedExpanded = !isPassedExpanded }
      )
    }

    if (isPassedExpanded) {
      items(passedExams) { exam ->
        ExamCard(exam = exam)
      }
    }

    // Pending
    stickyHeader {
      ExpandableHeader(
        title = "Pending Exams",
        count = pendingExams.size,
        isExpanded = isPendingExpanded,
        titleColor = MaterialTheme.colorScheme.primary,
        onToggle = { isPendingExpanded = !isPendingExpanded }
      )
    }

    if (isPendingExpanded) {
      items(pendingExams) { exam ->
        ExamCard(exam = exam)
      }
    }
  }
}

@Composable
fun ExpandableHeader(
  title: String,
  count: Int,
  isExpanded: Boolean,
  titleColor: Color,
  onToggle: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier
        .fillMaxWidth()
        .clickable { onToggle() }
  ) {
    Row(
      modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = titleColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        CountBadge(
          count = count
        )
      }

      Icon(
        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
        contentDescription = "Expand",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun CountBadge(count: Int) {
  Surface(
    shape = RoundedCornerShape(percent = 50),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
  ) {
    Text(
      text = count.toString(),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.primary
    )
  }
}

@Composable
fun ExamCard(exam: Exam) {
  val isPassed = exam.status == "S" || (exam.grade != null && (exam.grade.toIntOrNull() ?: 0) >= 18)

  // Colors
  val cardBackground = if (isPassed) MaterialTheme.colorScheme.surfaceContainer else TextColorLight
  // Left pill color
  val statusColor = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
  // Text colors
  val gradeColor = MaterialTheme.colorScheme.primary

  Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp, horizontal = 16.dp),
    colors = CardDefaults.cardColors(
      containerColor = cardBackground
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .height(IntrinsicSize.Min)
    ) {
      // Vertical Status Pill
      Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(6.dp),
        color = statusColor
      ) {}

      Row(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {

        // Title + Date
        Column(
          modifier = Modifier
              .weight(1f)
              .padding(end = 8.dp),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = exam.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              lineHeight = 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )

          // Date
          if (exam.date != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = exam.date.split(" ").firstOrNull() ?: exam.date,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Voto + CFU
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(start = 4.dp)
        ) {
          // Voto
          if (isPassed && exam.grade != null) {
            Surface(
              color = gradeColor.copy(alpha = 0.1f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = exam.grade,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = gradeColor
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
          }

          // CFU
          Text(
            text = "${exam.cfu} CFU",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
    }
  }
}