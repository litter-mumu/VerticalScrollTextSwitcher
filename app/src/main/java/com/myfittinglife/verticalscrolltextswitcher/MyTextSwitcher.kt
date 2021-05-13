package com.myfittinglife.verticalscrolltextswitcher

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher
import java.util.*

/**
 * @Author LD
 * @Time 2021/5/12 15:13
 * @Describe 自定义TextSwitcher
 * @Modify
 */
class MyTextSwitcher(private val mContext: Context, attributeSet: AttributeSet? = null) :
    TextSwitcher(mContext, attributeSet), ViewSwitcher.ViewFactory {


    private var textSize: Float
    private var textColor: Int
    //最多显示的行数
    private var maxlines: Int
    private var ellipse: String?
    private var textStyle: Int
    private var animDirection: Int

    private val dataList = mutableListOf<String>()
    private var timer: Timer? = null
    private var num = 0

    //用来判断是否开始滚动，防止重复滚动(timer执行多次schedule造成滚动的文字顺序混乱)
    private var isStart = false

    init {

        //获取属性
        val typedArray: TypedArray =
            mContext.obtainStyledAttributes(attributeSet, R.styleable.MyTextSwitcherStyle)
//        textSize = typedArray.getDimension(R.styleable.MyTextSwitcherStyle_textSize, px2sp(mContext,15))
        textSize = typedArray.getDimension(R.styleable.MyTextSwitcherStyle_textSize, 15f)


        textColor =
            typedArray.getColor(R.styleable.MyTextSwitcherStyle_textColor, Color.RED)

        maxlines = typedArray.getInt(R.styleable.MyTextSwitcherStyle_maxLines, 0)

        ellipse = typedArray.getString(R.styleable.MyTextSwitcherStyle_ellipsize)

        textStyle = typedArray.getInt(R.styleable.MyTextSwitcherStyle_textStyle, 0)

        animDirection =
            typedArray.getInt(R.styleable.MyTextSwitcherStyle_animDirection, 0)
        //默认从下往上
        typedArray.recycle()
        //创建动画
        when (animDirection) {
            0 -> {
                //从下往上的动画
                createBottomToTopAnimation()
            }
            1 -> {
                //从上往下
                createTopToBottomAnimation()
            }
        }
        //注意，这个setFactory()一定要写在获取属性的下方，不然在调用makeView()方法时，获取不到属性。
        setFactory(this)
    }

    override fun makeView(): View {
        val textView = TextView(mContext)

        when (ellipse) {
            "1" -> {
                textView.ellipsize = TextUtils.TruncateAt.START
            }
            "2" -> {
                textView.ellipsize = TextUtils.TruncateAt.MIDDLE
            }
            "3" -> {
                textView.ellipsize = TextUtils.TruncateAt.END
            }
            "4" -> {
                textView.ellipsize = TextUtils.TruncateAt.MARQUEE
            }
        }
        when (textStyle) {
            1 -> {
                textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
            2 -> {
                textView.typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
            }
        }
        if(maxlines>0){
            textView.maxLines = maxlines
        }
        //这里不能直接写textSize，setTextSize()方法默认是按sp值设置进去的。二者的单位不同，所以最后的结果会偏差很大
//        textView.textSize = textSize
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize)

        textView.setTextColor(textColor)
        textView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )
        return textView
    }

    /**
     * 从下到上的动画（实现3）
     */
    private fun createBottomToTopAnimation() {
        createAnimation(
            //入场动画
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_PARENT, 1f,
            Animation.ABSOLUTE, 0f,
            //出场动画
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_PARENT, -1f
        )
    }

    /**
     * 从上到下（实现4）
     */
    private fun createTopToBottomAnimation() {
        createAnimation(
            //入场动画
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_PARENT, -1f,
            Animation.ABSOLUTE, 0f,
            //出场动画
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_PARENT, 1f
        )
    }
    /**
     * 具体创建动画的方法
     */
    private fun createAnimation(
        ina: Int,
        inFromXValue: Float,
        inb: Int,
        inToXValue: Float,
        inc: Int,
        inFromYValue: Float,
        ind: Int,
        inToYValue: Float,

        outa: Int,
        outFromXValue: Float,
        outb: Int,
        outToXValue: Float,
        outc: Int,
        outFromYValue: Float,
        outd: Int,
        outToYValue: Float
    ) {
        //入场动画
        var _inAnimation: Animation = TranslateAnimation(
            ina, inFromXValue,
            inb, inToXValue,
            inc, inFromYValue,
            ind, inToYValue
        )
        _inAnimation.duration = 1000
        _inAnimation.fillAfter = true
        inAnimation = _inAnimation
        //出场动画
        var _outAnimation: Animation = TranslateAnimation(
            outa, outFromXValue,
            outb, outToXValue,
            outc, outFromYValue,
            outd, outToYValue
        )
        _outAnimation.duration = 1000
        _outAnimation.fillAfter = true
        outAnimation = _outAnimation
    }


    /**
     * 点击VerticalTextSwitcher时需要知道其所属的下标位置，用户有可能会进行一些操作
     */
    fun getCurrentPosition(): Int {
        return if (dataList.size > 0) {
            num % dataList.size
        } else {
            -1
        }
    }

    /**
     * 填充数据
     */
    fun setDataList(theData: List<String>) {
        //防止滚动过程中处理数据发生异常
        stopScroll()

        dataList.clear()
        dataList.addAll(theData)
    }

    /**
     * 开始滚动
     * intervalTime：每隔多少秒滚动一次，可以不填写，默认是2秒切换一次，
     * todo 如何防止重复开启滚动
     */
    fun startScroll(intervalTime: Long = 2000) {
        if (timer == null) {
            timer = Timer()
        }
        if(!isStart){
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    num++
                    (mContext as Activity).runOnUiThread {
                        if (dataList.size > 0) {
                            setText(dataList[num % dataList.size])
                        }
                    }
                }

            }, 0, intervalTime)
            isStart = true
        }
    }

    /**
     * 停止滚动
     */
    fun stopScroll() {
        timer?.cancel()
        timer = null
        isStart = false
    }
    private fun px2sp(context: Context, px: Int): Float {
        var fontScale = context.resources.displayMetrics.scaledDensity
        return (px / fontScale + 0.5).toFloat()
    }


}