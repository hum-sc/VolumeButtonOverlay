package com.humsc.actionbarservice

import android.app.Application
import com.google.android.material.color.DynamicColors

class ActionBarApp: Application(){
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}