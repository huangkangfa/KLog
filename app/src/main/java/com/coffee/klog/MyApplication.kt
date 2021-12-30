package com.coffee.klog

import android.app.Application
import com.kangfa.klog.KLog

/**
 * Author:coffee
 *
 * Date:2021/12/30
 * Time:11:49 上午
 * Description:
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KLog.init(this)
            .catchCrashLog()
            .setLogName("testLog")
            .setDebug(BuildConfig.DEBUG)
    }

}