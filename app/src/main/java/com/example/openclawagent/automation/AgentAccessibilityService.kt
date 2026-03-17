package com.example.openclawagent.automation

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.openclawagent.agent.AgentState

class AgentAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // אנחנו נסרוק את המסך עבור גלילה,פתיחת אפלקציה חדשה,או חלון חדש
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.d("AgentEyes", "--- מתחיל סריקת מסך עבור: ${event.packageName} ---")

            // מבקשים מאנדרואיד את ה"שורש" של המסך הנוכחי
            val rootNode = rootInActiveWindow

            if (rootNode != null) {
                val screenBuilder = StringBuilder()
                extractTextFromTree(rootNode, screenBuilder)
                rootNode.recycle() // חשוב: משחררים את הזיכרון בסיום
                AgentState.screen_info = screenBuilder.toString()
            } else {
                Log.d("AgentEyes", "לא הצלחתי לקרוא את תוכן המסך (ייתכן שזה מסך מאובטח).")
            }

            Log.d("AgentEyes", "--- סיום סריקה ---")
        }
    }

    // פונקציה רקורסיבית שסורקת את כל האלמנטים על המסך
    private fun extractTextFromTree(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        // מוציאים טקסט (למשל מתוך פסקה) או תיאור (למשל מאייקון של כפתור)
        val text = node.text?.toString()
        val contentDesc = node.contentDescription?.toString()
        val isClickable = node.isClickable

        // אם יש באלמנט הזה מידע רלוונטי, נדפיס אותו
        if (!text.isNullOrBlank() || !contentDesc.isNullOrBlank()) {
            val elementInfo = text ?: contentDesc
            val clickableText = if (isClickable) "[לחיץ]" else ""

            builder.append("ראיתי אלמנט: '$elementInfo' $clickableText\n")
            Log.d("AgentEyes", "ראיתי אלמנט: '$elementInfo' $clickableText")
        }

        // ממשיכים לסרוק את כל "הילדים" של האלמנט הזה
        for (i in 0 until node.childCount) {
            extractTextFromTree(node.getChild(i), builder)
        }
    }

    fun clickOnText(textToFind: String): Boolean{
        val rootNode = rootInActiveWindow ?: return false

        val foundNode = rootNode.findAccessibilityNodeInfosByText(textToFind)
        if (foundNode.isNullOrEmpty()){
            Log.d("AgentHands", "לא מצאתי במסך שום אלמנט עם הטקסט: '$textToFind'")
            return false
        }
        for (node in foundNode){
            var clickableNode: AccessibilityNodeInfo? = node
            while (clickableNode != null && !clickableNode.isClickable){
                clickableNode = clickableNode.parent
            }
            if (clickableNode != null && clickableNode.isClickable){
                Log.d("AgentHands", "מצאתי אלמנט לחיץ עבור '$textToFind'! מבצע לחיצה 👆")
                clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        Log.d("AgentHands", "מצאתי את הטקסט '$textToFind' אבל לא הצלחתי ללחוץ עליו.")
        return false
    }

    override fun onInterrupt() {
        Log.d("AgentEyes", "שירות הנגישות נקטע.")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this // <--- הקסם קורה פה! השירות רושם את עצמו במשתנה הסטטי.
        Log.d("AgentEyes", "שירות הנגישות מחובר ומוכן לקרוא מסכים!")
    }

    // פונקציה שמופעלת כשהשירות נסגר
    override fun onDestroy() {
        super.onDestroy()
        instance = null // מנקים את הזיכרון כדי למנוע דליפות
    }
    companion object {
        // משתנה סטטי שמחזיק רפרנס לשירות עצמו. מתחיל כ-null.
        var instance: AgentAccessibilityService? = null
    }
}