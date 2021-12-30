package com.coffee.klog

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kangfa.klog.KLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.btn_hello).setOnClickListener {
            for(i in 0..100){
                KLog.i("测试数据 哈哈哈哈哈哈 序号 $i")
            }
            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show()
        }
    }
}