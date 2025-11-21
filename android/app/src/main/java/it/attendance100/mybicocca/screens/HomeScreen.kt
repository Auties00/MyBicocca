package it.attendance100.mybicocca.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.composables.*
import it.attendance100.mybicocca.ui.theme.*
import kotlinx.coroutines.*


@Composable
fun HomeContentScreen(
  pagerState: PagerState,
  coroutineScope: CoroutineScope,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val surfaceColor = MaterialTheme.colorScheme.surface
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = stringResource(R.string.bottom_navbar_carriera),
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
    // Top section: Student info + Avatar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Avatar Card
      Surface(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        tonalElevation = 2.dp
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Avatar(size = 90.dp)
        }
      }
      // Student Info Card
      Surface(
        modifier = Modifier
            .weight(1f)
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        tonalElevation = 2.dp,
        onClick = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(4)
          }
        }
      ) {
        Column(
          modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
              .padding(start = 8.dp),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Mario Rossi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Computer Science",
            fontSize = 14.sp,
            color = grayColor
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Mat. 123456",
            fontSize = 13.sp,
            color = primaryColor,
            fontWeight = FontWeight.Medium
          )
        }
      }


    }

    // Unread Notifications Section
    val hasUnreadNotifications = true // Mock data
    if (hasUnreadNotifications) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
        Text(
          text = "Unread Notifications",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = textColor
        )

        // Notification 1
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = surfaceColor,
          tonalElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Notifications,
              contentDescription = null,
              tint = primaryColor,
              modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "New exam results available",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
              Text(
                text = "2 hours ago",
                fontSize = 12.sp,
                color = grayColor
              )
            }
          }
        }

        // Notification 2
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = surfaceColor,
          tonalElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Event,
              contentDescription = null,
              tint = primaryColor,
              modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Upcoming lecture tomorrow",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
              Text(
                text = "5 hours ago",
                fontSize = 12.sp,
                color = grayColor
              )
            }
          }
        }

        // See more button
        TextButton(
          onClick = { /* Navigate to notifications */ },
          modifier = Modifier.align(Alignment.End)
        ) {
          Text("See all notifications")
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Shortcuts Section
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Shortcuts",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = textColor
        )
        IconButton(onClick = { /* Edit shortcuts */ }) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit shortcuts",
            tint = grayColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Shortcuts Grid - 2x3 layout with varying sizes
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1: [2x1 large] [1x1 small]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Large shortcut - 2x1
          Surface(
            modifier = Modifier
                .weight(2f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Row(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
              )
              Text(
                text = "My Courses",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
            }
          }

          // Small shortcut - 1x1
          Surface(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }

        // Row 2: [1x1 with label] [2x1 large]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Small shortcut with label - 1x1
          Surface(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Column(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Exams",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
            }
          }

          // Large shortcut - 2x1
          Surface(
            modifier = Modifier
                .weight(2f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Row(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
              )
              Text(
                text = "Library",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
            }
          }
        }

        // Row 3: [1x1 text] [1x1 icon] [1x1 icon+text]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Small shortcut with short text - 1x1
          Surface(
            modifier = Modifier
                .weight(1f)
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Mails",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
            }
          }

          // Icon only - 1x1
          Surface(
            modifier = Modifier
                .weight(1f)
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          // Icon with short text - 1x1
          Surface(
            modifier = Modifier
                .weight(1f)
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 2.dp,
            onClick = { /* Shortcut action */ }
          ) {
            Column(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Mensa",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
            }
          }
        }
      }
    }

    // Bottom spacing
    Spacer(modifier = Modifier.height(16.dp))
  }
}
