package it.attendance100.mybicocca.screens

import android.content.res.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import androidx.lifecycle.*
import androidx.navigation.*
import com.google.gson.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.api.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import javax.inject.*

data class ApiEndpointUiModel(
  val id: String,
  val method: String,
  val path: String,
  val description: String,
  val response: String? = null,
  val isLoading: Boolean = false,
  val isExpanded: Boolean = false,
)

@HiltViewModel
class ApiTestViewModel @Inject constructor(
  private val apiService: MyBicoccaApiService,
  private val preferencesManager: PreferencesManager,
) : ViewModel() {

  var endpoints = mutableStateListOf<ApiEndpointUiModel>()
    private set

  private val gson = GsonBuilder().setPrettyPrinting().create()

  init {
    endpoints.add(ApiEndpointUiModel("profile", "GET", "/user_profile", "Get user profile"))
    endpoints.add(ApiEndpointUiModel("career", "GET", "/user_career", "Get user career"))
    endpoints.add(ApiEndpointUiModel("exams", "GET", "/user_exams", "Get user exams"))
  }

  fun toggleExpand(id: String) {
    val index = endpoints.indexOfFirst { it.id == id }
    if (index != -1) {
      val item = endpoints[index]
      endpoints[index] = item.copy(isExpanded = !item.isExpanded)
    }
  }

  fun execute(id: String) {
    when (id) {
      "profile" -> testUserProfile(id)
      "career" -> testUserCareer(id)
      "exams" -> testUserExams(id)
    }
  }

  private fun updateState(id: String, isLoading: Boolean? = null, response: String? = null) {
    val index = endpoints.indexOfFirst { it.id == id }
    if (index != -1) {
      val item = endpoints[index]
      endpoints[index] = item.copy(
        isLoading = isLoading ?: item.isLoading,
        response = response ?: item.response,
        isExpanded = if (response != null) true else item.isExpanded // Auto expand on result
      )
    }
  }

  private fun testUserProfile(id: String) {
    viewModelScope.launch {
      updateState(id, isLoading = true)
      android.util.Log.d("ApiTestViewModel", "testUserProfile: Starting...")
      try {
        val fiscalCode = preferencesManager.authFiscalCode
        android.util.Log.d("ApiTestViewModel", "testUserProfile: FiscalCode: $fiscalCode")
        val response = apiService.getUserProfile(fiscalCode)

        // Cache IDs
        val career = response.careers.firstOrNull()
        if (career != null) {
          preferencesManager.userStudentId = career.studentId
          preferencesManager.userMatricId = career.matricId
          preferencesManager.userPersonId = response.user.personId
          preferencesManager.userTypeTitleCode = career.typeTitleCode
        }

        updateState(id, isLoading = false, response = gson.toJson(response))
        android.util.Log.d("ApiTestViewModel", "testUserProfile: Success")
      } catch (e: Exception) {
        updateState(id, isLoading = false, response = "Error: ${e.message}\n\nStack Trace:\n${e.stackTraceToString()}")
        android.util.Log.e("ApiTestViewModel", "testUserProfile: Error", e)
      }
    }
  }

  private fun testUserCareer(id: String) {
    viewModelScope.launch {
      updateState(id, isLoading = true)
      android.util.Log.d("ApiTestViewModel", "testUserCareer: Starting...")
      try {
        val fiscalCode = preferencesManager.authFiscalCode

        var stuId: Int? = preferencesManager.userStudentId.takeIf { it != -1 }
        var matId: Int? = preferencesManager.userMatricId.takeIf { it != -1 }
        var personId: Int? = preferencesManager.userPersonId.takeIf { it != -1 }
        var typeTitleCode: String? = preferencesManager.userTypeTitleCode

        if (stuId == null || matId == null || personId == null) {
          if (fiscalCode != null) {
            try {
              android.util.Log.d("ApiTestViewModel", "testUserCareer: Fetching profile for IDs...")
              val profile = apiService.getUserProfile(fiscalCode)
              val career = profile.careers.firstOrNull()
              stuId = career?.studentId
              matId = career?.matricId
              personId = profile.user.personId
              typeTitleCode = career?.typeTitleCode

              // Cache
              if (career != null) {
                preferencesManager.userStudentId = career.studentId
                preferencesManager.userMatricId = career.matricId
                preferencesManager.userPersonId = profile.user.personId
                preferencesManager.userTypeTitleCode = career.typeTitleCode
              }

              android.util.Log.d("ApiTestViewModel", "testUserCareer: IDs found - stuId: $stuId, matId: $matId, personId: $personId, typeTitleCode: $typeTitleCode")
            } catch (e: Exception) {
              updateState(id, isLoading = false, response = "Error fetching profile to get IDs: ${e.message}\n${e.stackTraceToString()}")
              android.util.Log.e("ApiTestViewModel", "testUserCareer: Error fetching profile", e)
              return@launch
            }
          } else {
            android.util.Log.w("ApiTestViewModel", "testUserCareer: FiscalCode is null")
          }
        } else {
          android.util.Log.d("ApiTestViewModel", "testUserCareer: IDs found in cache.")
        }

        android.util.Log.d("ApiTestViewModel", "testUserCareer: Calling getUserCareer...")
        val response = apiService.getUserCareer(stuId, matId, personId, typeTitleCode)
        updateState(id, isLoading = false, response = gson.toJson(response))
        android.util.Log.d("ApiTestViewModel", "testUserCareer: Success")
      } catch (e: Exception) {
        updateState(id, isLoading = false, response = "Error: ${e.message}\n\nStack Trace:\n${e.stackTraceToString()}")
        android.util.Log.e("ApiTestViewModel", "testUserCareer: Error", e)
      }
    }
  }

  private fun testUserExams(id: String) {
    viewModelScope.launch {
      updateState(id, isLoading = true)
      android.util.Log.d("ApiTestViewModel", "testUserExams: Starting...")
      try {
        val fiscalCode = preferencesManager.authFiscalCode
        var matId: Int? = preferencesManager.userMatricId.takeIf { it != -1 }

        if (matId == null) {
          if (fiscalCode != null) {
            try {
              android.util.Log.d("ApiTestViewModel", "testUserExams: Fetching profile for IDs...")
              val profile = apiService.getUserProfile(fiscalCode)
              val career = profile.careers.firstOrNull()
              matId = career?.matricId

              // Cache (partial)
              if (career != null) {
                preferencesManager.userStudentId = career.studentId
                preferencesManager.userMatricId = career.matricId
                preferencesManager.userPersonId = profile.user.personId
                preferencesManager.userTypeTitleCode = career.typeTitleCode
              }

              android.util.Log.d("ApiTestViewModel", "testUserExams: IDs found - matId: $matId")
            } catch (e: Exception) {
              updateState(id, isLoading = false, response = "Error fetching profile to get IDs: ${e.message}\n${e.stackTraceToString()}")
              android.util.Log.e("ApiTestViewModel", "testUserExams: Error fetching profile", e)
              return@launch
            }
          } else {
            android.util.Log.w("ApiTestViewModel", "testUserExams: FiscalCode is null")
          }
        } else {
          android.util.Log.d("ApiTestViewModel", "testUserExams: IDs found in cache.")
        }

        android.util.Log.d("ApiTestViewModel", "testUserExams: Calling getUserExams...")
        val response = apiService.getUserExams(matId)
        updateState(id, isLoading = false, response = gson.toJson(response))
        android.util.Log.d("ApiTestViewModel", "testUserExams: Success")
      } catch (e: Exception) {
        updateState(id, isLoading = false, response = "Error: ${e.message}\n\nStack Trace:\n${e.stackTraceToString()}")
        android.util.Log.e("ApiTestViewModel", "testUserExams: Error", e)
      }
    }
  }
}

@Composable
fun ApiTestScreen(
  navController: NavHostController,
  viewModel: ApiTestViewModel = hiltViewModel(),
) {
  val textColor = MaterialTheme.colorScheme.onBackground
  val backgroundColor = MaterialTheme.colorScheme.background

  Scaffold(
    containerColor = backgroundColor,
    topBar = {
      Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = backgroundColor
      ) {
        Row(
          modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 13.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.arrow_back),
              tint = textColor
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "API Test",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(viewModel.endpoints) { endpoint ->
        SwaggerEndpointItem(
          endpoint = endpoint,
          onToggleExpand = { viewModel.toggleExpand(endpoint.id) },
          onExecute = { viewModel.execute(endpoint.id) }
        )
      }
    }
  }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = false)
@Composable
fun SwaggerEndpointItemPreview() {
  val endpoint = ApiEndpointUiModel("test", "GET", "/test", "Test endpoint")
  MaterialTheme(
    colorScheme = MyBicoccaDarkColorScheme
  ) {
    Box(
      modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
          .padding(8.dp)
    ) {
      SwaggerEndpointItem(endpoint = endpoint, onToggleExpand = {}, onExecute = {})
    }
  }
}

@Composable
fun SwaggerEndpointItem(
  endpoint: ApiEndpointUiModel,
  onToggleExpand: () -> Unit,
  onExecute: () -> Unit,
) {
  val methodColor = when (endpoint.method) {
    "GET" -> Color(0xFF61AFFE)
    "POST" -> Color(0xFF49CC90)
    "PUT" -> Color(0xFFFCA130)
    "DELETE" -> Color(0xFFF93E3E)
    else -> Color.Gray
  }

  val borderColor = MaterialTheme.colorScheme.onSecondary // methodColor.copy(alpha = 0.5f)
  val backgroundColor = MaterialTheme.colorScheme.surfaceDim // methodColor.copy(alpha = 0.1f)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, borderColor),
    colors = CardDefaults.cardColors(containerColor = backgroundColor)
  ) {
    Column(modifier = Modifier.animateContentSize()) {
      // Header
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Method Badge (Clickable to execute)
        Surface(
          modifier = Modifier
              .width(80.dp)
              .height(32.dp)
              .clickable { onExecute() },
          shape = RoundedCornerShape(4.dp),
          color = methodColor
        ) {
          Box(contentAlignment = Alignment.Center) {
            if (endpoint.isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
            } else {
              Text(
                text = endpoint.method,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Path & Description
        Column(
          modifier = Modifier
              .weight(1f)
              .offset(y = (3).dp)
        ) {
          Text(
            text = endpoint.path,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            modifier = Modifier.offset(y = (-6).dp),
            text = endpoint.description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
          )
        }

        // Expand Icon
        val rotation by animateFloatAsState(
          targetValue = if (endpoint.isExpanded) 180f else 0f,
          label = "rotation"
        )
        Icon(
          imageVector = Icons.Default.ExpandMore,
          contentDescription = "Expand",
          modifier = Modifier.rotate(rotation),
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      // Expanded Content
      if (endpoint.isExpanded) {
        // HorizontalDivider(color = borderColor)
        Box(
          modifier = Modifier
              .fillMaxWidth()
              .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
              .clip(shape = RoundedCornerShape(size = 4.dp))
              .heightIn(max = 300.dp) // Approx 10 lines
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
              .padding(8.dp)
              .verticalScroll(rememberScrollState())
              .horizontalScroll(rememberScrollState())
        ) {
          Text(
            text = endpoint.response ?: "Click the ${endpoint.method} button to execute.",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}
