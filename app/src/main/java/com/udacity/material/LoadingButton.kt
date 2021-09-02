package com.udacity.material

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.properties.Delegates
import android.animation.Animator

import android.animation.AnimatorListenerAdapter
import androidx.core.content.ContextCompat


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface CompleteDownload {
        fun onCompleteDownload() {}
    }

    private var completeDownload: CompleteDownload? = null

    private var widthSize = 0
    private var heightSize = 0

    var isBlocked = false

    private var color: Int = 0
    private var colorDownload: Int = 0
    private var text = ""

    //Pay
    private val paintPay = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angleProgress = 0

    //Button
    private var downloadProgress = 0
    private var downloadAlpha = 100
    private val paintButton = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintButtonStatusDownload = Paint(Paint.ANTI_ALIAS_FLAG)

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Unknown) { _, _, newStatus ->
        changeButton(newStatus)
    }

    //Text
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setStyle(attrs)
        changeButton(buttonState)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        onDrawButton(canvas)
        onDrawPay(canvas)
    }

    private fun setStyle(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.DownloadingButton) {
            color = getColor(R.styleable.DownloadingButton_btn_color, 0)
            colorDownload = getColor(R.styleable.DownloadingButton_btn_downloading_color, 0)
        }
    }

    private fun onDrawPay(canvas: Canvas) {
        paintPay.style = Paint.Style.FILL
        paintPay.color = ContextCompat.getColor(context, R.color.colorAccent)

        canvas.drawArc(
            (widthSize - 150f),
            (heightSize / 2) - 50f,
            (widthSize - 50f),
            (heightSize / 2) + 50f,
            270F,
            angleProgress.toFloat(),
            true,
            paintPay
        )
    }

    private fun onDrawButton(canvas: Canvas) {
        paintButton.style = Paint.Style.FILL
        paintButton.color = color

        canvas.drawRect(
            0F,
            height.toFloat(),
            width.toFloat(),
            0F,
            paintButton
        )

        onDrawButtonStatusDownload(canvas)
        onDrawButtonText(canvas)
    }

    private fun onDrawButtonStatusDownload(canvas: Canvas) {
        paintButtonStatusDownload.style = Paint.Style.FILL
        paintButtonStatusDownload.color = colorDownload
        paintButtonStatusDownload.alpha = downloadAlpha

        canvas.drawRect(
            0F,
            height.toFloat(),
            downloadProgress.toFloat(),
            0F,
            paintButtonStatusDownload
        )
    }

    private fun onDrawButtonText(canvas: Canvas) {
        paintText.textAlign = Paint.Align.CENTER
        paintText.textSize = 80F
        paintText.color = Color.WHITE

        canvas.drawText(
            text,
            (widthSize / 2).toFloat(),
            (height / 2).toFloat() + 30,
            paintText
        )
    }

    private fun buttonClickedAnimation() {
        val valueAnimator = ValueAnimator.ofInt(0, width)
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.duration = 5000
        valueAnimator.addUpdateListener {
            downloadProgress = valueAnimator.animatedValue as Int
            angleProgress = ((valueAnimator.animatedValue as Int) * 360) / width
            invalidate()
        }

        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {

                buttonState = ButtonState.Completed

                val valueAnimator2 = ValueAnimator.ofInt(100, 0)
                valueAnimator2.interpolator = AccelerateDecelerateInterpolator()
                valueAnimator2.duration = 1000
                valueAnimator2.addUpdateListener {
                    downloadAlpha = valueAnimator2.animatedValue as Int
                    invalidate()
                }
                valueAnimator2.repeatCount = ValueAnimator.REVERSE
                valueAnimator2.repeatMode = ValueAnimator.RESTART
                valueAnimator2.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        downloadProgress = 0
                        downloadAlpha = 100

                        angleProgress = 0
                        invalidate()

                        buttonState = ButtonState.Unknown
                        completeDownload?.onCompleteDownload()
                    }
                })
                valueAnimator2.start()
            }
        })
        valueAnimator.start()
        this.buttonState = ButtonState.Loading
    }

    private fun changeButton(buttonState: ButtonState) {
        when (buttonState) {
            ButtonState.Unknown -> {
                isBlocked = false
                text = resources.getString(R.string.select_one)
                invalidate()
            }
            ButtonState.Download -> {
                text = resources.getString(R.string.download)
                invalidate()
            }
            ButtonState.Loading -> {
                text = resources.getString(R.string.we_are_loading)
            }
            ButtonState.Completed -> {
                text = resources.getString(R.string.completed)
            }
            ButtonState.Clicked -> {
                isBlocked = true
                buttonClickedAnimation()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val min: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(min, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun setCompleteDownloadListener(completeDownload: CompleteDownload) {
        this.completeDownload = completeDownload
    }
}