package com.beviswang.customcontrols.widget


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.beviswang.customcontrols.loge
import org.jetbrains.anko.sp
import kotlin.math.max

/**
 * 有趣味性的选择器控件
 * @author BevisWong
 * @date 2022/3/24
 */
class InterestingSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    def: Int = 0
) : View(context, attrs, def) {
    private val mDefWidth: Int = 100
    private val mDefHeight: Int = 100
    private val mItemArray: ArrayList<String> = arrayListOf()
    private var mColumn: Int = 5
    private var mLine: Int = 0
    private var mHPadding: Int = 10
    private var mVPadding: Int = 10
    private var mRadius: Float = 10f

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mItemRect: RectF = RectF() // 每个区块的方块，包括间距
    private var mItemBlockRect: RectF = RectF() // 可选区域，除去间距
    private var mItemBlockRectList: ArrayList<RectF> = arrayListOf()
    private var mSelectColor: Int = Color.parseColor("#FD2A1A")
    private var mUnselectColor: Int = Color.parseColor("#EEEEEE")

    private var mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTextRect: Rect = Rect()
    private var mTextRectList: ArrayList<Rect> = arrayListOf()
    private var mSelectTextColor: Int = Color.WHITE
    private var mUnselectTextColor: Int = Color.BLACK
    private var mTextSize: Int = 10

    private var mTouchPosList: Array<Boolean> = Array(0) { false }
    private var mSavedTouchPosList: Array<Boolean> = Array(0) { false }
    private var mTouchLine: Int = 0
    private var mTouchColumn: Int = 0

    private var mIsTouching: Boolean = false // 是否正在触摸

    private var mLastTouchPos: Int = -1 // 上次触摸的位置

    init {
        mPaint.color = mUnselectColor
        mTextPaint.color = mUnselectTextColor
        mTextPaint.isFakeBoldText = true

        // TODO 测试数据
        setItems(
            arrayListOf(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"
            )
        )
        setColumn(5)
        setTextSize(15)
    }

    fun setItems(items: ArrayList<String>) {
        mItemArray.clear()
        mItemArray.addAll(items)
        buildBlock()
    }

    fun setColumn(column: Int) {
        mColumn = column
        buildBlock()
    }

    /**
     * 设置标题文字大小
     * @param textSize 单位 sp
     */
    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        mTextPaint.textSize = sp(mTextSize).toFloat()
        buildBlock()
    }

    /** 重置选中状态 */
    fun resetStatus() {
        mSavedTouchPosList = Array(mItemArray.size) { false }
        invalidate()
    }

    private fun buildBlock(w: Int = width) {
        if (mItemArray.isEmpty() || mColumn == 0) {
            mLine = 0
            return
        }
        mLine = mItemArray.size / mColumn
        if ((mLine * mColumn) < mItemArray.size) mLine++
        // 根据行列重新计算每个 Item 区域大小
        mItemRect.right = w / mColumn.toFloat()
        mItemRect.bottom = 2 * (mVPadding - mHPadding) + mItemRect.right
        mItemBlockRect.left = mHPadding.toFloat()
        mItemBlockRect.right = mItemRect.right - (2 * mHPadding)
        mItemBlockRect.top = mVPadding.toFloat()
        mItemBlockRect.bottom = mItemRect.bottom - (2 * mVPadding)

        mItemBlockRectList.clear()
        mTextRectList.clear()
        (0 until mLine).forEach { line ->
            (0 until mColumn).forEach { column ->
                mItemBlockRectList.add(
                    RectF(
                        column * mItemRect.width() + mItemBlockRect.left,
                        line * mItemRect.height() + mItemBlockRect.top,
                        column * mItemRect.width() + mItemBlockRect.right,
                        line * mItemRect.height() + mItemBlockRect.bottom
                    )
                )
                mTextRectList.add(measureTextRect(mItemArray[mItemBlockRectList.lastIndex]))
            }
        }

        mSavedTouchPosList = Array(mItemArray.size) { false }
    }

    private fun measureTextRect(str: String): Rect {
        mTextRect = Rect()
        mTextPaint.getTextBounds(str, 0, str.length, mTextRect)
        return mTextRect
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 针对 wrap_content 的处理，使 wrap_content 生效
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefWidth, mDefHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildBlock(w)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchPos = -1
                mIsTouching = true
                resetSelectItem()
                onTouchPosition(max(0f,event.x), max(0f,event.y))
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mIsTouching = true
                onTouchPosition(max(0f,event.x), max(0f,event.y))
            }
            MotionEvent.ACTION_UP -> {
                mIsTouching = false
                onTouchPosition(max(0f,event.x), max(0f,event.y))
                saveTouchPosition()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsTouching = false
                resetSelectItem()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun onTouchPosition(motionX: Float, motionY: Float) {
        loge("motionX=$motionX   motionY=$motionY")
        mTouchLine = (motionY / mItemRect.height()).toInt()
        mTouchColumn = (motionX / mItemRect.width()).toInt()
        if (mTouchLine == mLine) mTouchLine--
        if (mTouchColumn == mColumn) mTouchColumn--
        touchingPosition(mTouchLine * mColumn + mTouchColumn)
    }

    private fun touchingPosition(pos: Int) {
        if (pos >= mTouchPosList.size) {
            if (!mIsTouching) resetSelectItem()
            return
        }
        if (mLastTouchPos == pos) return // 触摸的位置相同，不做处理
        mTouchPosList[pos] = !mTouchPosList[pos]
        mLastTouchPos = pos
        invalidate()
    }

    private fun saveTouchPosition() {
        mSavedTouchPosList = mTouchPosList.copyOf()
        invalidate()
    }

    private fun resetSelectItem() {
        mTouchPosList = mSavedTouchPosList.copyOf()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        mItemBlockRectList.forEachIndexed { index, block ->
            if (if (mIsTouching) mTouchPosList[index] else mSavedTouchPosList[index]) {
                mPaint.color = mSelectColor
                mTextPaint.color = mSelectTextColor
            } else {
                mPaint.color = mUnselectColor
                mTextPaint.color = mUnselectTextColor
            }
            canvas.drawRoundRect(block, mRadius, mRadius, mPaint)
            canvas.drawText(
                mItemArray[index],
                block.centerX() - (mTextRectList[index].width() / 2f),
                block.centerY() + (mTextRectList[index].height() / 2f),
                mTextPaint
            )
        }
    }
}