package com.elta.android.common.logger

object LogMasker {
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val JWT_REGEX = Regex("eyJ[a-zA-Z0-9-_=]+\\.[a-zA-Z0-9-_=]+\\.[a-zA-Z0-9-_=]+")
    private val SENSITIVE_KEYS = listOf("password", "token", "accessToken", "refreshToken", "cookie", "authorization")

    fun mask(message: String): String {
        if (message.isBlank()) return message
        var masked = message

        // 1. Mask JWT tokens
        masked = JWT_REGEX.replace(masked, "***JWT_MASKED***")

        // 2. Mask emails
        masked = EMAIL_REGEX.replace(masked, "***EMAIL_MASKED***")

        // 3. Mask sensitive keys in JSON and URL parameters
        for (key in SENSITIVE_KEYS) {
            // Match JSON: "password":"value" or "password" : "value"
            val jsonRegex = Regex("\"$key\"\\s*:\\s*\"[^\"]+\"", RegexOption.IGNORE_CASE)
            masked = jsonRegex.replace(masked, "\"$key\":\"***MASKED***\"")

            // Match URL query parameters: key=value
            val queryRegex = Regex("(?<=[?&])$key=[^&\\s]+", RegexOption.IGNORE_CASE)
            masked = queryRegex.replace(masked, "$key=***MASKED***")
        }

        return masked
    }
}
