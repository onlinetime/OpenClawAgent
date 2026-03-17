package com.example.openclawagent.automation

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AgentAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // אנחנו נסרוק את המסך רק כשיש שינוי חלון מהותי, כדי לא להציף את הלוגים
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.d("AgentEyes", "--- מתחיל סריקת מסך עבור: ${event.packageName} ---")

            // מבקשים מאנדרואיד את ה"שורש" של המסך הנוכחי
            val rootNode = rootInActiveWindow

            if (rootNode != null) {
                // מפעילים את פונקציית הסריקה שלנו
                printNodeTree(rootNode)
                rootNode.recycle() // חשוב: משחררים את הזיכרון בסיום
            } else {
                Log.d("AgentEyes", "לא הצלחתי לקרוא את תוכן המסך (ייתכן שזה מסך מאובטח).")
            }

            Log.d("AgentEyes", "--- סיום סריקה ---")
        }
    }

    // פונקציה רקורסיבית שסורקת את כל האלמנטים על המסך
    private fun printNodeTree(node: AccessibilityNodeInfo?) {
        if (node == null) return

        // מוציאים טקסט (למשל מתוך פסקה) או תיאור (למשל מאייקון של כפתור)
        val text = node.text?.toString()
        val contentDesc = node.contentDescription?.toString()
        val isClickable = node.isClickable

        // אם יש באלמנט הזה מידע רלוונטי, נדפיס אותו
        if (!text.isNullOrBlank() || !contentDesc.isNullOrBlank()) {
            val elementInfo = text ?: contentDesc
            val clickableText = if (isClickable) "[לחיץ]" else ""
            Log.d("AgentEyes", "ראיתי אלמנט: '$elementInfo' $clickableText")
        }

        // ממשיכים לסרוק את כל "הילדים" של האלמנט הזה
        for (i in 0 until node.childCount) {
            printNodeTree(node.getChild(i))
        }
    }

    override fun onInterrupt() {
        Log.d("AgentEyes", "שירות הנגישות נקטע.")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AgentEyes", "שירות הנגישות מחובר ומוכן לקרוא מסכים!")
    }
}