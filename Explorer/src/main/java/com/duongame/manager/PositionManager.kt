package com.duongame.manager

import com.duongame.manager.PositionManager
import java.util.HashMap

/**
 * Created by namjungsoo on 2016-11-20.
 */
object PositionManager {
    private val positionMap = HashMap<String, Int>()
    private val topMap = HashMap<String, Int>()

    fun setPosition(path: String, position: Int) {
        positionMap[path] = position
    }

    fun getPosition(path: String): Int {
        return if (positionMap.containsKey(path)) {
            positionMap[path]!!
        } else 0
    }

    fun setTop(path: String, top: Int) {
        topMap[path] = top
    }

    fun getTop(path: String): Int {
        return if (topMap.containsKey(path)) {
            topMap[path]!!
        } else 0
    }
}