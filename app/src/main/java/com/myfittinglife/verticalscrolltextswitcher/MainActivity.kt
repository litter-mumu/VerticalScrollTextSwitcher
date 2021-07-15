package com.myfittinglife.verticalscrolltextswitcher

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
@Author LD
@Time 2021/5/12 14:42
@Describe 垂直滚动文字的项目实现(基于TextSwitcher实现)
@Modify
 */
class MainActivity : AppCompatActivity() {

    //存放我们的数据
    val dataList = mutableListOf<String>()
    var timer: Timer? = null
    var num = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        dataList.add("温度传感器报警")
        dataList.add("瓦斯传感器报警")
        dataList.add("二氧化碳传感器报警")
        dataList.add("湿度传感器报警")
        //用来测试较长的文字该怎么显示
        dataList.add("人员超时、人员异常、瓦斯传感器、烟雾传感器、风门、烟雾传感器报警")

        /**
         * XML控件实现
         */
        textSwitcher1.setFactory {
            //设置TextView控件的一些属性
            val textView = TextView(this)
            //文字过长时以省略号的形式显示
            textView.ellipsize = TextUtils.TruncateAt.END
            //设置最多只能显示一行
            textView.maxLines = 1
            textView.textSize = 40f
            textView.setTextColor(Color.BLUE)
            textView.layoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView
        }

        /**
         * 自定义TextSwitcher实现
         */
        //设置数据
        mTextSwitcher.setDataList(dataList)

        /**
         * 开始滚动
         */
        btnStart.setOnClickListener {
            if (timer == null) {
                timer = Timer()
            }
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    num++
                    runOnUiThread {
                        if (dataList.size > 0) {
                            textSwitcher1.setText(dataList[num % dataList.size])
                        }
                    }
                }
            }, 0, 2000)

            mTextSwitcher.startScroll(2000)
        }
        /**
         * 停止滚动
         */
        btnStop.setOnClickListener {
            //XML的TextViewSwitcher的控制
            timer?.cancel()
            timer = null

            //自定义TextSwitcher的控制
            mTextSwitcher.stopScroll()
        }
        mTextSwitcher.setOnClickListener {
            val position = mTextSwitcher.getCurrentPosition()
            Toast.makeText(this,"当前点击的位置为：$position 内容为：${dataList[position]}",Toast.LENGTH_SHORT).show()
        }


    }
}