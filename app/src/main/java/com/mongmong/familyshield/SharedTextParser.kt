package com.mongmong.familyshield

object SharedTextParser {
    fun composeSharedText(text: CharSequence?, subject: CharSequence? = null): String? {
        val cleanSubject = subject?.toString()?.trim().orEmpty()
        val cleanText = text?.toString()?.trim().orEmpty()

        return when {
            cleanSubject.isBlank() && cleanText.isBlank() -> null
            cleanSubject.isBlank() -> cleanText
            cleanText.isBlank() -> cleanSubject
            cleanText.startsWith(cleanSubject) -> cleanText
            else -> "$cleanSubject\n\n$cleanText"
        }
    }
}
