package com.kangfa.klog

import android.app.Application
import android.os.Process
import android.util.Log
import org.joda.time.Instant
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

/**
 * Author:coffee
 *
 * Date:2021/12/30
 * Time:9:37 上午
 * Description: 轻量级日志工具类
 */
object KLog : Thread.UncaughtExceptionHandler {

    /**
     * 程序对象
     */
    private var application: Application? = null

    /**
     * 日志输出Tag
     */
    private var TAG = "KLog"

    /**
     * 线程池
     */
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * 系统默认的UncaughtException处理类
     */
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * 是否抓取闪退奔溃日志
     */
    private var isCatchCrashLog = false

    /**
     * 日志缓存目录
     */
    private lateinit var logDir: File

    /**
     * 日志文件名
     */
    private lateinit var logName: String

    /**
     * 是否是调试模式，调试模式下不写入文件
     */
    private var isDebug = true

    /**
     * 初始化
     * 注：这个方法使用前一定要调用，且传application
     */
    fun init(context: Application): KLog {
        application = context
        setLogDir(application!!.defaultLogDir("log"))
        setLogName("log")
        return this
    }

    /**
     * 是否抓取全局错误日志
     */
    fun catchCrashLog(): KLog {
        isCatchCrashLog = true

        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
        return this
    }

    /**
     * 设置打印tag
     */
    fun setTag(tag: String): KLog {
        TAG = tag
        return this
    }

    /**
     * 设置日志缓存目录
     */
    fun setLogDir(dir: File): KLog {
        logDir = dir
        return this
    }

    /**
     * 设置日志缓存文件名
     */
    fun setLogName(name: String): KLog {
        logName = name
        return this
    }

    /**
     * 设置日志文件最大值
     */
    fun setLogLimitSize(size: Long): KLog {
        logLimitSize = size
        return this
    }

    /**
     * 是否是debug模式
     */
    fun setDebug(debug: Boolean): KLog {
        isDebug = debug
        return this
    }

    /**
     * 输出日志 i
     */
    @JvmStatic
    fun i(content: String) {
        Log.i(TAG, content)
        if (!isDebug) {
            writeLogToLocal(logDir, logName, content)
        }
    }

    /**
     * 输出日志 e
     */
    @JvmStatic
    fun e(content: String) {
        Log.e(TAG, content)
        if (!isDebug) {
            writeLogToLocal(logDir, logName, content)
        }
    }

    /**
     * 全局错误处理
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handleException(e) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler?.uncaughtException(t, e)
        } else {
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            // 退出程序
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }

        // 当前时间
        val instant = Instant.now()

        // 时间戳转换
        val stringBuilder = StringBuilder(instant.toDateTime().toString("yyyy/MM/dd HH:mm:ss"))
        stringBuilder.append(":\n")

        // 获取错误信息
        stringBuilder.append(ex.message)
        stringBuilder.append("\n")

        // 获取堆栈信息
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        stringBuilder.append(sw.toString())

        writeLogToLocal(logDir, logName, stringBuilder.toString())

        return true
    }

    /**
     * 写入日志到本地
     */
    private fun writeLogToLocal(dir: File, logName: String, content: String) {
        executor.execute {
            writeLog(dir, logName, content)
        }
    }

}