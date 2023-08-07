package com.jeka8833.tntserverwebapi.git

data class PlayerCustomizationPreferences(var capePriority: Int = 1, var heartAnimation: HeartAnimation? = null) {
    fun setUseTNTCape(isTNTCape: Boolean) {
        capePriority = if (isTNTCape) 2 else 1
    }

    fun setTextAnimation(textAnimation: Array<String>) {
        if (heartAnimation == null) {
            heartAnimation = HeartAnimation(textAnimation)
        } else {
            heartAnimation!!.textAnimation = textAnimation
        }
    }

    fun setDelayAnimation(timeDelay: Int) {
        if (heartAnimation == null) {
            heartAnimation = HeartAnimation(null, timeDelay)
        } else {
            heartAnimation!!.timeShift = timeDelay
        }
    }
}

data class HeartAnimation(var textAnimation: Array<String>? = null, var timeShift: Int = Int.MAX_VALUE)