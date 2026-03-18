package com.example.openclawagent.agent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.openclawagent.automation.AgentAccessibilityService
import com.example.openclawagent.llm.LocalBrain
import kotlinx.coroutines.*

class AgentService : Service() {

    // יצירת סביבת עבודה אסינכרונית כדי שהחשיבה לא תתקע את מסך הטלפון
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isRunning = false

    // הפונקציה הזו מופעלת כשהשירות מתחיל לרוץ
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            Log.d("AgentService", "הסוכן הופעל ומתחיל לחשוב...")
            startReasoningLoop()
        }
        // מבקש ממערכת ההפעלה להשאיר את השירות חי ברקע
        return START_STICKY
    }

    // זוהי הלולאה המרכזית (Agent main loop) מההנחיות
    private fun startReasoningLoop() {
        // יוצרים מופע של המוח
        val localBrain = LocalBrain(this)

        serviceScope.launch {
            var hasPerformedTestClick = false

            // אתחול המודל (בדיקה שהקובץ קיים בתיקיית ההורדות)
            localBrain.initModel()

            while (isRunning) {
                try {
                    val currentScreenContext = AgentState.screen_info

                    if (currentScreenContext.isNotBlank() && !hasPerformedTestClick) {
                        Log.d("AgentBrain", "המוח שולח את המסך לבינה המלאכותית...")

                        // שיניתי כאן מ-thinkAndDecide ל-generateResponse כדי שיתאים ל-LocalBrain החדש
                        val aiCommand = localBrain.generateResponse(currentScreenContext)
                        Log.d("AgentBrain", "הבינה המלאכותית החליטה: $aiCommand")

                        if (aiCommand.startsWith("CLICK:")) {
                            val targetApp = aiCommand.removePrefix("CLICK:").trim()
                            val isClickSuccessful = AgentAccessibilityService.instance?.clickOnText(targetApp)

                            if (isClickSuccessful == true) {
                                Log.d("AgentBrain", "✅ הלחיצה בוצעה בהצלחה!")
                                hasPerformedTestClick = true
                            }
                        }
                    } else {
                        Log.d("AgentBrain", "ממתין למידע חדש מהמסך...")
                    }

                    delay(5000)

                } catch (e: Exception) {
                    Log.e("AgentBrain", "שגיאה בלולאת החשיבה: ${e.message}")
                }
            }
        }
    }    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel() // סגירת תהליכי הרקע
        Log.d("AgentService", "הסוכן כובה.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // אין צורך בחיבור ישיר לממשק כרגע
    }
}