package com.example.openclawagent.agent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
        serviceScope.launch {
            while (isRunning) {
                try {
                    Log.d("AgentService", "--- תחילת מחזור חשיבה ---")

                    // שלב 1: state = read_context()
                    // val state = "כאן נקרא את מה שיש על המסך"

                    // שלב 2: prompt = build_prompt(state)
                    // val prompt = buildPrompt(state)

                    // שלב 3: response = LLM(prompt)
                    // val response = "כאן נפעיל את המודל המקומי"

                    // שלב 4: action = parse_action(response)
                    // val action = "כאן נהפוך את התשובה ל-JSON"

                    // שלב 5: if action == TOOL -> execute_tool()
                    // כאן נפעיל את הכלי (למשל לחיצה על המסך)

                    Log.d("AgentService", "מחזור הסתיים. ממתין לפני הפעולה הבאה...")

                    // השהייה קלה בין פעולה לפעולה כדי לא לשרוף את הסוללה
                    delay(3000)

                } catch (e: Exception) {
                    Log.e("AgentService", "שגיאה בלולאת החשיבה: ${e.message}")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel() // סגירת תהליכי הרקע
        Log.d("AgentService", "הסוכן כובה.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // אין צורך בחיבור ישיר לממשק כרגע
    }
}