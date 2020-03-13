package com.luren.lifegame

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var left1 = 2
    var right1 = 3
    var left2 = 3
    var right2 = 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        world_start.setOnClickListener {
            life_game.startWorldTime()
        }
        world_step.setOnClickListener {
            life_game.worldTicket()
        }
        world_pause.setOnClickListener {
            life_game.pauseWorldTime()
        }
        world_reset.setOnClickListener {
            life_game.resetWorld()
        }
        bSeekBar1.setOnSeekBarChangeListener { i, i2 ->
            left1 = i
            right1 = i2
            tv_left1.text = "" + i
            tv_right1.text = "" + i2
            Log.i("game","i:$i ,i2$i2")
        }
        bSeekBar2.setOnSeekBarChangeListener { i, i2 ->
            left2 = i
            right2 = i2
            tv_left2.text = "" + i
            tv_right2.text = "" + i2
        }
        world_sure.setOnClickListener {
            val widthInput = world_width.text.toString().toIntOrNull()
            val heightInput = world_height.text.toString().toIntOrNull()
            val speedInput = world_speed.text.toString().toIntOrNull()
            val rate = world_rate.text.toString().toIntOrNull()
            widthInput?.also { life_game.worldWidth = it }
            heightInput?.also { life_game.worldHeight = it }
            speedInput?.also { life_game.worldTimeSpeed = it }
            rate?.also { life_game.randomLifeRate = it }
            life_game.alive = Array(right1 - left1 + 1, { it + left1 })
            life_game.newLife = Array(right2 - left2 + 1, { it + left2 })
//            life_game.resetWorld()
        }
    }
}