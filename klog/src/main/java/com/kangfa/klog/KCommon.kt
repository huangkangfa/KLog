package com.kangfa.klog

import android.content.Context
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 最大日志大小  默认最大 50M
 */
var logLimitSize: Long = 50 * 1024 * 1024

/**
 * 写日志
 */
@Synchronized
fun writeLog(dir: File, logName: String, content: String) {
    try {
        val text = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date().time) + " " + content + "\r\n"

        val file = File("${dir}/$logName")

        if (!file.exists()) {
            file.createNewFile()
        }

        checkLimit(file)

        val fos = FileOutputStream(file, true)
        fos.write(text.toByteArray())
        fos.close()

    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 文件大小超出限制处理
 */
@Synchronized
private fun checkLimit(file: File) {
    // 如果大于文件最大值，删除文件中前一半内容
    if (file.length() > logLimitSize) {
        try {
            // 创建临时备份文件
            val tempFile = File("${file.absolutePath}Temp")
            if (!tempFile.exists()) {
                tempFile.createNewFile()
            }
            file.copyTo(tempFile, true)

            // 边读取临时文件中的内容边输出到当前日志文件
            val readFile = FileInputStream(tempFile)
            val writeFile = FileOutputStream(file, false)
            var totalBytes = 0L
            val limitLen = logLimitSize / 4

            // 开始读写
            writeFile.use { fos ->
                val data = ByteArray(1024)
                var len: Int
                while (readFile.read(data).also { len = it } > -1) {
                    if (totalBytes >= limitLen) {
                        fos.write(data, 0, len)
                    }
                    totalBytes += len
                }
                fos.flush()
            }

            // 删除备份文件
            tempFile.delete()

            // 释放资源
            readFile.close()
            writeFile.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * 获取默认日志存储目录
 */
fun Context.defaultLogDir(rootDir: String): File {
    val cachePath = if (isExternalStorageWriteable()) {
        externalCacheDir ?: cacheDir
    } else {
        cacheDir
    }
    val baseFile = File(cachePath.toString(), rootDir)
    if (!baseFile.exists()) {
        baseFile.mkdirs()
    }
    return baseFile
}

/**
 * 外部文件是否可写
 *
 * @return true: 外设存储设备可写；<br></br>
 */
private fun isExternalStorageWriteable(): Boolean {
    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
}