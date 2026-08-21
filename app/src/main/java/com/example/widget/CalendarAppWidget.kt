package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.CalendarDatabase
import com.example.util.CalendarUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class CalendarAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_WIDGET"
        const val EXTRA_TARGET_TAB = "extra_target_tab"
        const val EXTRA_OPEN_ADD_EVENT = "extra_open_add_event"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CalendarAppWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_calendar_modern)

            val cal = Calendar.getInstance()
            val dayNameEn = SimpleDateFormat("EEEE", Locale.ENGLISH).format(cal.time).uppercase()
            val dayNumEn = SimpleDateFormat("d", Locale.ENGLISH).format(cal.time)
            val monthYearEn = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
            val dateFullEn = "$dayNumEn $monthYearEn"

            val hijri = CalendarUtils.getHijriDate(cal)
            val bangla = CalendarUtils.getBanglaDate(cal)

            val banglaDayDigit = CalendarUtils.toBanglaDigit(bangla.day)
            val banglaYearDigit = CalendarUtils.toBanglaDigit(bangla.year)
            val banglaFullStr = "$banglaDayDigit ${bangla.monthNameBn} $banglaYearDigit বঙ্গাব্দ"
            val hijriFullStr = "🌙 ${CalendarUtils.toBanglaDigit(hijri.day)} ${hijri.monthNameBn} ${CalendarUtils.toBanglaDigit(hijri.year)} হিজরি"

            val seasonName = CalendarUtils.getBengaliSeason(bangla.monthNameEn)
            val seasonDisplay = "🌿 $seasonName"

            // Bind dates
            views.setTextViewText(R.id.widget_day_english, "$dayNameEn, $dayNumEn ${SimpleDateFormat("MMM", Locale.ENGLISH).format(cal.time).uppercase()}")
            views.setTextViewText(R.id.widget_date_full_en, dateFullEn)
            views.setTextViewText(R.id.widget_badge_season, seasonDisplay)
            views.setTextViewText(R.id.widget_bangla_digit, banglaDayDigit)
            views.setTextViewText(R.id.widget_bangla_full, banglaFullStr)
            views.setTextViewText(R.id.widget_hijri_full, hijriFullStr)

            // Season & Hijri visibility
            val showSeason = WidgetPreferences.isShowSeason(context)
            views.setViewVisibility(R.id.widget_badge_season, if (showSeason) View.VISIBLE else View.GONE)

            val showHijri = WidgetPreferences.isShowHijri(context)
            views.setViewVisibility(R.id.widget_hijri_full, if (showHijri) View.VISIBLE else View.GONE)

            // Configure Photo Background & Overlay
            val customPhotoUri = WidgetPreferences.getPhotoUri(context)
            val presetImage = WidgetPreferences.getPresetImage(context)
            val overlayOpacity = WidgetPreferences.getOverlayOpacity(context)

            // Apply overlay dimming color
            val alphaInt = (overlayOpacity * 255).toInt().coerceIn(0, 255)
            val overlayColor = Color.argb(alphaInt, 0, 0, 0)
            views.setInt(R.id.widget_dark_overlay, "setBackgroundColor", overlayColor)

            // Load and set background image safely
            val bitmap = loadWidgetBitmap(context, customPhotoUri, presetImage)
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widget_photo_bg, bitmap)
                views.setViewVisibility(R.id.widget_photo_bg, View.VISIBLE)
            } else {
                when (presetImage) {
                    "islamic" -> views.setImageViewResource(R.id.widget_photo_bg, R.drawable.img_widget_islamic_preset)
                    "nature" -> views.setImageViewResource(R.id.widget_photo_bg, R.drawable.img_widget_nature_preset)
                    "header" -> views.setImageViewResource(R.id.widget_photo_bg, R.drawable.default_header_cover)
                    else -> views.setViewVisibility(R.id.widget_photo_bg, View.GONE)
                }
            }

            // Bind PendingIntents for Clicks
            // 1. Root tap -> Open MainActivity Home
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, 0)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 1001, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // 2. Open Calendar Tab Button
            val openCalendarIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, 1)
            }
            val openCalendarPendingIntent = PendingIntent.getActivity(
                context, 1002, openCalendarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_open_app, openCalendarPendingIntent)

            // 3. Add Event Button
            val addEventIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, 1)
                putExtra(EXTRA_OPEN_ADD_EVENT, true)
            }
            val addEventPendingIntent = PendingIntent.getActivity(
                context, 1003, addEventIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add_event, addEventPendingIntent)

            // 4. Refresh Button
            val refreshIntent = Intent(context, CalendarAppWidget::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 1004, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // 5. Customize Style / Photo Button -> Open Settings Tab
            val customizeIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, 4)
            }
            val customizePendingIntent = PendingIntent.getActivity(
                context, 1005, customizeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_customize, customizePendingIntent)

            // Asynchronously fetch today's events from Room Database
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = CalendarDatabase.getInstance(context)
                    val todayEvents = db.eventDao().getEventsForDaySync(startOfDay, endOfDay)
                    val todayTasks = db.taskDao().getTasksForDaySync(startOfDay, endOfDay)

                    val summaryText = when {
                        todayEvents.isNotEmpty() -> {
                            val first = todayEvents.first()
                            val timeStr = if (first.isAllDay) "সারাদিন" else CalendarUtils.formatTime(first.startDate)
                            "📌 ${first.title} ($timeStr)"
                        }
                        todayTasks.isNotEmpty() -> {
                            val first = todayTasks.first()
                            "☑️ টাস্ক: ${first.title}"
                        }
                        else -> "📅 আজকের কর্মসূচি: কোনো বিশেষ ইভেন্ট নেই"
                    }

                    views.setTextViewText(R.id.widget_event_task_strip, summaryText)
                    withContext(Dispatchers.Main) {
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    // Fallback to update widget immediately
                    withContext(Dispatchers.Main) {
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun loadWidgetBitmap(context: Context, photoUriStr: String?, preset: String): Bitmap? {
            if (!photoUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(photoUriStr)
                    val input: InputStream? = context.contentResolver.openInputStream(uri)
                    if (input != null) {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeStream(input, null, options)
                        input.close()

                        // Calculate sample size
                        val targetWidth = 600
                        val targetHeight = 350
                        var inSampleSize = 1
                        if (options.outHeight > targetHeight || options.outWidth > targetWidth) {
                            val halfHeight = options.outHeight / 2
                            val halfWidth = options.outWidth / 2
                            while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= targetWidth) {
                                inSampleSize *= 2
                            }
                        }

                        val secondInput = context.contentResolver.openInputStream(uri)
                        val decodeOptions = BitmapFactory.Options().apply {
                            this.inSampleSize = inSampleSize
                        }
                        val decoded = BitmapFactory.decodeStream(secondInput, null, decodeOptions)
                        secondInput?.close()
                        return decoded
                    }
                } catch (e: Exception) {
                    // Ignore and fallback
                }
            }

            // Fallback presets decoding if requested as bitmap
            val drawableRes = when (preset) {
                "islamic" -> R.drawable.img_widget_islamic_preset
                "nature" -> R.drawable.img_widget_nature_preset
                "header" -> R.drawable.default_header_cover
                else -> null
            }

            if (drawableRes != null) {
                try {
                    val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                    return BitmapFactory.decodeResource(context.resources, drawableRes, options)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            return null
        }
    }
}
