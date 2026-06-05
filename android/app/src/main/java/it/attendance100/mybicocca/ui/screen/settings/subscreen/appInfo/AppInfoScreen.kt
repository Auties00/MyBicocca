package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import java.time.Year

private const val COPYRIGHT_START_YEAR = 2025

private val versionText: String = buildString {
    append("Versione ${BuildConfig.VERSION_NAME}")
    if (BuildConfig.DEBUG) append(" [Debug]")
}

// "© 2025–<year>" once the project has run past its start year, otherwise just "© 2025".
private val copyrightText: String
    get() {
        val current = Year.now().value
        val span =
            if (current > COPYRIGHT_START_YEAR) "$COPYRIGHT_START_YEAR–$current" else "$COPYRIGHT_START_YEAR"
        return "© $span 100% Attendance"
    }

@Composable
fun AppInfoScreen() {
    val context = LocalContext.current
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MyBicoccaWordmark(fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))
        Text(text = versionText, color = secondary, fontSize = 16.sp)

        Spacer(Modifier.height(28.dp))
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo MyBicocca",
            modifier = Modifier.size(180.dp),
        )

        Spacer(Modifier.height(28.dp))
        Text(text = copyrightText, color = secondary, fontSize = 14.sp)

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                // Provided by play-services-oss-licenses; lists the bundled open-source libraries.
                OssLicensesMenuActivity.setActivityTitle("Licenze open source")
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth(0.7f),
        ) {
            Text(text = "Licenze open source", fontSize = 16.sp)
        }
    }
}
