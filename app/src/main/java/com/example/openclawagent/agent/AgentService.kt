package com.example.openclawagent.agent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.openclawagent.automation.AgentAccessibilityService
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
        var hasPerformedTestClick = false
        serviceScope.launch {
            while (isRunning) {
                try {
                    Log.d("AgentService", "--- תחילת מחזור חשיבה ---")

                    // שלב 1: state = read_context()
                    val currentScreenContext = AgentState.screen_info
                    if (currentScreenContext.isNotBlank()) {
                        Log.d("AgentBrain", "המוח קיבל את תמונת המסך הבאה:\n$currentScreenContext")
                    } else {
                        Log.d("AgentBrain", "המוח לא קיבל מידע חדש מהמסך (המסך ריק או שהעיניים לא סרקו עדיין).")
                    }
                    // --- המוח הטיפש (Mock Logic) נכנס לפעולה! ---

                    // נחפש את אפליקציית ההגדרות (אם האמולטור שלך בעברית, שנה ל-"הגדרות")
                    val targetWord = "Chrome"

                    // אם המילה נמצאת במסך, ועוד לא ביצענו את הלחיצת ניסיון:
                    if (currentScreenContext.contains(targetWord) && !hasPerformedTestClick) {
                        Log.d("AgentBrain", "🧠 המוח זיהה את המילה '$targetWord'! מרים טלפון לידיים...")

                        // קריאה דרך ה-Companion Object ישירות לפונקציית הלחיצה של הנגישות!
                        val isClickSuccessful = AgentAccessibilityService.instance?.clickOnText(targetWord)

                        if (isClickSuccessful == true) {
                            Log.d("AgentBrain", "✅ פקודת הלחיצה בוצעה בהצלחה!")
                            hasPerformedTestClick = true // מסמנים שלחצנו כדי לא ללחוץ שוב ושוב
                        } else {
                            Log.d("AgentBrain", "❌ המוח נתן פקודה, אבל הידיים לא הצליחו ללחוץ.")
                        }
                    }
                    // ----------------------------------------------
                    else {
                        Log.d("AgentBrain", "המוח לא קיבל מידע חדש מהמסך.")
                    }

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
                    delay(4000)

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