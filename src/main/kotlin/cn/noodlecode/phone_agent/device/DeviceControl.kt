package cn.noodlecode.phone_agent.device

interface DeviceControl {
    fun getCurrentApp(): String

    fun launch(packageName: String): Boolean

    fun tap(x: Int, y: Int, delay: Long = 1000): Boolean

    fun doubleTap(x: Int, y: Int, delay: Long = 1000): Boolean

    fun longPress(x: Int, y: Int, duration: Long = 3000, delay: Long = 1000): Boolean

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 200, delay: Long = 1000): Boolean

    fun back(delay: Long = 1000): Boolean

    fun home(delay: Long = 1000): Boolean

    fun inputText(text: String): Boolean

    fun clearText(): Boolean

    fun getScreenshot(): ScreenshotInfo?

}

data class ScreenshotInfo(
    val base64Data: String,
    val width: Int,
    val height: Int,
)
