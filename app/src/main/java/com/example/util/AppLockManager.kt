package com.example.util

import com.example.data.repository.CalendarRepository

object AppLockManager {
    const val KEY_LOCK_ENABLED = "APP_LOCK_ENABLED"
    const val KEY_LOCK_TYPE = "APP_LOCK_TYPE" // "PIN", "PASSWORD", "BIOMETRIC"
    const val KEY_LOCK_CODE = "APP_LOCK_CODE"

    suspend fun isLockEnabled(repository: CalendarRepository): Boolean {
        return repository.getSetting(KEY_LOCK_ENABLED) == "true"
    }

    suspend fun getLockType(repository: CalendarRepository): String {
        return repository.getSetting(KEY_LOCK_TYPE) ?: "PIN"
    }

    suspend fun getLockCode(repository: CalendarRepository): String {
        return repository.getSetting(KEY_LOCK_CODE) ?: ""
    }

    suspend fun setAppLock(repository: CalendarRepository, enabled: Boolean, type: String, code: String) {
        repository.setSetting(KEY_LOCK_ENABLED, if (enabled) "true" else "false")
        repository.setSetting(KEY_LOCK_TYPE, type)
        repository.setSetting(KEY_LOCK_CODE, code)
    }

    suspend fun verifyCode(repository: CalendarRepository, inputCode: String): Boolean {
        val savedCode = getLockCode(repository)
        return savedCode.isEmpty() || savedCode == inputCode
    }
}
