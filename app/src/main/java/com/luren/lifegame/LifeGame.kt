package com.luren.lifegame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.set
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Created by 拇指 on 2020/3/12 0012.
 * Email:muzhi@uoko.com
 */
class LifeGame : View {
    var worldWidth: Int = 100
    var worldHeight: Int = 100
    private var random = Random.Default
    var worldTimeSpeed = 100
    var worldPause = true
    var worldSizeAndCanvas = 1.0F
    var randomLifeRate = 50
    var newLife = arrayOf(3)
    var alive = arrayOf(2)
    private var drawCache: Bitmap? = null
    private lateinit var world: Array<Array<Boolean>>
    private lateinit var worldBuffer: Array<Array<Boolean>>

    private var worldJob: Job? = null
    private var wallPaint = Paint()
    private var lifePaint = Paint()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        newRandowWorld()

        wallPaint.color = Color.RED
        wallPaint.style = Paint.Style.STROKE
        wallPaint.strokeWidth = 20F

//        lifePaint.color = Color.BLACK
//        lifePaint.style = Paint.Style.FILL

    }


    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * 只wrapcontent
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specSize = MeasureSpec.getSize(widthMeasureSpec)
        var cwidth = specSize - 40
        if (cwidth < worldWidth) {
            cwidth = worldWidth
        }
        worldSizeAndCanvas = (cwidth.toDouble() / worldWidth).toFloat()
        val cheight = (worldSizeAndCanvas * worldHeight).toInt()
        setMeasuredDimension(cwidth + 40, cheight + 40)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0F, 0F, measuredWidth.toFloat(), measuredHeight.toFloat(), wallPaint)
        canvas.translate(20F, 20F)
        if (worldSizeAndCanvas > 1) {
            canvas.scale(worldSizeAndCanvas, worldSizeAndCanvas)
        }
        drawCache?.also {
            canvas.drawBitmap(it, 0F, 0F, lifePaint)
        }

        if (!worldPause) {
            invalidate()
        }
    }

    /**
     * 故意给世界多加两行两列作为边界，都是死的，方便计算，
     * 绘制的时候需要从1行1列开始，最后一行一列也不绘制
     */
    private fun newRandowWorld() {
        drawCache?.recycle()
        drawCache = Bitmap.createBitmap(worldWidth, worldHeight, Bitmap.Config.RGB_565)

        var currentLife = true
        world = Array(worldHeight + 2, { y ->
            Array(worldWidth + 2, { x ->
                if (x == 0 || x == worldWidth + 1 || y == 0 || y == worldHeight + 1) {
                    false
                } else {
                    currentLife = random.nextInt(100) < randomLifeRate
                    drawCache?.set(x - 1, y - 1, if (currentLife) Color.BLACK else Color.WHITE)
                    currentLife
                }
            })
        })
        worldBuffer = Array(worldHeight + 2) { y ->
            Array(worldWidth + 2, { x ->
                false
            })
        }

        postInvalidate()
    }

    /**
     * 世界重启
     */
    fun resetWorld() {
        pauseWorldTime()
        newRandowWorld()
        requestLayout()
        invalidate()
    }

    /**
     * 世界启动
     */
    fun startWorldTime() {
        worldPause = false
        postInvalidate()
        worldJob = GlobalScope.launch {
            while (true) {
                val async1 = async {
                    calculateWorld(0, 0, worldWidth / 2, worldHeight / 2)
                }
                val async2 = async {
                    calculateWorld(worldWidth / 2, 0, worldWidth, worldHeight / 2)
                }
                val async3 = async {
                    calculateWorld(0, worldHeight / 2, worldWidth / 2, worldHeight)
                }
                val async4 = async {
                    calculateWorld(worldWidth / 2, worldHeight / 2, worldWidth, worldHeight)
                }
                async1.await()
                async2.await()
                async3.await()
                async4.await()
                val b = world
                world = worldBuffer
                worldBuffer = b
//                calALl()
                delay(worldTimeSpeed.toLong())
            }
        }
    }

    private fun calALl() = runBlocking {
        val async1 = async {
            calculateWorld(0, 0, worldWidth / 2, worldHeight / 2)
        }
        val async2 = async {
            calculateWorld(worldWidth / 2, 0, worldWidth, worldHeight / 2)
        }
        val async3 = async {
            calculateWorld(0, worldHeight / 2, worldWidth / 2, worldHeight)
        }
        val async4 = async {
            calculateWorld(worldWidth / 2, worldHeight / 2, worldWidth, worldHeight)
        }
        async1.await()
        async2.await()
        async3.await()
        async4.await()
        val b = world
        world = worldBuffer
        worldBuffer = b
    }

    /**
     * 世界暂停
     */
    fun pauseWorldTime() {
        worldPause = true
        worldJob?.cancel()
    }

    /**
     * 计算世界中的生命状态
     * 左闭右开
     */
    private fun calculateWorld(fromX: Int, fromY: Int, endX: Int, endY: Int) {
        var nearLifeCount = 0

        for (y in (fromY + 1) until (endY + 1)) {
            for (x in (fromX + 1) until (endX + 1)) {
                nearLifeCount = 0
                nearLifeCount += if (world[y - 1][x - 1]) 1 else 0
                nearLifeCount += if (world[y][x - 1]) 1 else 0
                nearLifeCount += if (world[y + 1][x - 1]) 1 else 0

                nearLifeCount += if (world[y - 1][x]) 1 else 0
                nearLifeCount += if (world[y + 1][x]) 1 else 0

                nearLifeCount += if (world[y - 1][x + 1]) 1 else 0
                nearLifeCount += if (world[y][x + 1]) 1 else 0
                nearLifeCount += if (world[y + 1][x + 1]) 1 else 0
                when {
                    nearLifeCount in newLife -> {
                        worldBuffer[y][x] = true
                        drawCache?.set(x - 1, y - 1, Color.BLACK)
                    }
                    nearLifeCount in alive -> {
                        worldBuffer[y][x] = world[y][x]
                        if (world[y][x]) {
                            drawCache?.set(x - 1, y - 1, Color.BLACK)
                        } else {
                            drawCache?.set(x - 1, y - 1, Color.WHITE)
                        }
                    }
                    else -> {
                        worldBuffer[y][x] = false
                        drawCache?.set(x - 1, y - 1, Color.WHITE)
                    }
                }
            }
        }
    }

    /**
     * 世界单步运行
     */
    fun worldTicket() {
//        calculateWorld(0, 0, worldWidth, worldHeight)
        calALl()
        postInvalidate()
    }
}