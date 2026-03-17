package com.example.openclawagent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.openclawagent.agent.AgentService
import com.example.openclawagent.ui.theme.OpenClawAgentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenClawAgentTheme {
                AgentControlScreen()
            }
        }
    }
}

@Composable
fun AgentControlScreen() {
    // משיג את ההקשר של האפליקציה (כדי שנוכל להפעיל שירותים)
    val context = LocalContext.current

    // משתנה שזוכר אם הסוכן כרגע פועל או כבוי
    var isAgentRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "סטטוס הסוכן: ${if (isAgentRunning) "פועל 🟢" else "כבוי 🔴"}")

        Spacer(modifier = Modifier.height(16.dp)) // רווח

        Button(onClick = {
            val intent = Intent(context, AgentService::class.java)
            if (isAgentRunning) {
                context.stopService(intent) // עוצר את הסוכן
            } else {
                context.startService(intent) // מתחיל את הסוכן
            }
            isAgentRunning = !isAgentRunning // משנה את הסטטוס
        }) {
            Text(text = if (isAgentRunning) "עצור סוכן" else "הפעל סוכן")
        }
    }
}