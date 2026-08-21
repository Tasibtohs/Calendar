package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startDate: Long,
    val endDate: Long,
    val isAllDay: Boolean = false,
    val location: String = "",
    val categoryId: Long = 1,
    val colorHex: String = "#3F51B5",
    val isCompleted: Boolean = false,
    val reminderMinutes: Int = 15,
    val reminderMinutesList: String = "15", // Comma-separated list of reminder offsets e.g. "0,15,60"
    val repeatType: String = "NONE", // "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "CUSTOM"
    val repeatEndType: String = "NEVER", // "NEVER", "DATE", "COUNT"
    val repeatUntilDate: Long? = null,
    val repeatCount: Int = 0,
    val parentEventId: Long? = null, // ID of series master if this is an occurrence
    val participants: String = "", // Comma-separated names/emails
    val attachmentUri: String? = null, // URI string for image, pdf, doc, or file
    val linkUrl: String = "", // Web link URL
    val notes: String = "", // Private notes
    val isArchived: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val isCompleted: Boolean = false,
    val categoryId: Long = 1,
    val reminderMinutes: Int? = null,
    val status: String = "Pending", // "Pending", "Completed", "Overdue"
    val isArchived: Boolean = false
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String = "#2D3748",
    val isPinned: Boolean = false,
    val categoryId: Long = 1,
    val associatedDate: Long? = null,
    val isChecklist: Boolean = false,
    val checklistJson: String = "",
    val isLocked: Boolean = false,
    val pinCode: String = "",
    val tags: String = "",
    val reminderTime: Long? = null,
    val drawingData: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val birthDate: Long, // timestamp for day/month
    val birthYear: Int? = null,
    val reminderMinutes: Int = 1440,
    val notes: String = "",
    val avatarUri: String? = null,
    val notificationEnabled: Boolean = true
)

@Entity(tableName = "anniversaries")
data class Anniversary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,
    val year: Int? = null,
    val reminderMinutes: Int = 1440,
    val notes: String = "",
    val notificationEnabled: Boolean = true
)

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameBn: String = "",
    val date: Long,
    val type: String = "Bangladesh Public Holiday", // "Bangladesh Public Holiday", "National Day", "Islamic Holiday", "International Day", "Custom Holiday"
    val calendarType: String = "Gregorian", // "Gregorian", "Hijri", "Bangla"
    val isCustom: Boolean = false,
    val description: String = "" // Holiday reason / significance
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String = "Event",
    val isCustom: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetDate: Long,
    val category: String = "Event", // "Birthday", "Exam", "Wedding", "Travel", "Important Event"
    val colorHex: String = "#3F51B5",
    val notes: String = ""
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "SYSTEM", // "EVENT", "TASK", "BIRTHDAY", "ANNIVERSARY", "HOLIDAY", "SYSTEM"
    val targetId: Long? = null,
    val isRead: Boolean = false
)
