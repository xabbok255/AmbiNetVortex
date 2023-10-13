package com.xabbok.ambinetvortex.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager
import androidx.core.content.ContextCompat

fun setStatusBarColor(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val isDarkMode =
            activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val color = if (isDarkMode) getThemeColor(activity, "colorSurface") else getThemeColor(activity, "colorPrimary")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.statusBarColor = color
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
        }
    }
}

@SuppressLint("DiscouragedApi")
fun getThemeColor(context: Context, attrName: String): Int {
    val typedValue = TypedValue()
    val theme = context.theme
    val colorAttr = context.resources.getIdentifier(attrName, "attr", context.packageName)
    theme.resolveAttribute(colorAttr, typedValue, true)
    val colorResId = typedValue.resourceId
    return ContextCompat.getColor(context, colorResId)
}