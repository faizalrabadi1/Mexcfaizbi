package com.example

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AccessManager {
    private const val SECRET_KEY = "yemen_bot_secret_2026"

    /**
     * يولد كود تفعيل صالح لعدد معين من الساعات.
     * الكود عبارة عن دمج بين وقت الانتهاء (Millisecond) و التوقيع (Hash).
     */
    fun generateCode(hours: Int): String {
        val expiryTime = System.currentTimeMillis() + (hours * 60L * 60L * 1000L)
        val hash = generateHash(expiryTime.toString())
        // نأخذ أول 6 رموز من الهاش للتبسيط
        val shortHash = hash.take(6).uppercase(Locale.getDefault())
        // الكود النهائي: time_hash
        return "${expiryTime}_$shortHash"
    }

    /**
     * يتحقق من صلاحية الكود.
     */
    fun isValid(code: String): Boolean {
        try {
            val parts = code.split("_")
            if (parts.size != 2) return false
            
            val expiryTimeStr = parts[0]
            val expectedHash = parts[1]
            
            val expiryTime = expiryTimeStr.toLong()
            if (System.currentTimeMillis() > expiryTime) {
                return false // منتهي الصلاحية
            }
            
            val actualHash = generateHash(expiryTimeStr).take(6).uppercase(Locale.getDefault())
            return actualHash == expectedHash
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * يعيد تاريخ الانتهاء كنص مقروء.
     */
    fun getExpiryDateStr(code: String): String {
        try {
            val parts = code.split("_")
            if (parts.isEmpty()) return ""
            val expiryTime = parts[0].toLong()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(expiryTime))
        } catch (e: Exception) {
            return ""
        }
    }

    private fun generateHash(input: String): String {
        val data = input + SECRET_KEY
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
