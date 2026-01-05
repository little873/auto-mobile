package cn.noodlecode.phone_agent.device.adb

import cn.noodlecode.phone_agent.config.ANDROID_APP_PACKAGES
import cn.noodlecode.phone_agent.device.DeviceControl
import cn.noodlecode.phone_agent.device.ScreenshotInfo
import java.lang.Thread.sleep
import java.util.Base64

class AndroidControl(deviceId: String? = null) : AdbShell(deviceId), DeviceControl {

    private val screenshot = Screenshot(deviceId)

    override fun getCurrentApp(): String {
        val result = runShellCommand("dumpsys", "window")
        result.split("\n").forEach { line ->
            if ("mCurrentFocus" in line || "mFocusedApp" in line) {
                for ((appName, packageName) in ANDROID_APP_PACKAGES) {
                    if (packageName in line) {
                        return appName
                    }
                }
            }
        }
        return "System Home"
    }

    override fun launch(packageName: String): Boolean {
        // 使用 monkey 命令启动应用的主 Activity
        val result = runShellCommand("monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
        return result.contains("Events injected: 1")
    }

    override fun tap(x: Int, y: Int, delay: Long): Boolean {
        println("调用了 tap 函数: $x $y")
        val result = runShellCommand("input", "tap", x.toString(), y.toString())
        sleep(delay)
        return result.isBlank()
    }

    override fun doubleTap(x: Int, y: Int, delay: Long): Boolean {
        val tap1 = tap(x, y, 100)
        val tap2 = tap(x, y, delay)
        return tap1 && tap2
    }

    override fun longPress(x: Int, y: Int, duration: Long, delay: Long): Boolean {
        val result = runShellCommand(
            "input",
            "swipe",
            x.toString(),
            y.toString(),
            x.toString(),
            y.toString(),
            duration.toString()
        )
        sleep(delay)
        return result.isBlank()
    }

    override fun swipe(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long,
        delay: Long,
    ): Boolean {
        val result =
            runShellCommand(
                "input",
                "swipe",
                startX.toString(),
                startY.toString(),
                endX.toString(),
                endY.toString(),
                duration.toString()
            )
        sleep(delay)
        return result.isBlank()
    }

    override fun back(delay: Long): Boolean {
        val result = runShellCommand("input", "keyevent", "4")
        sleep(delay)
        return result.isBlank()
    }

    override fun home(delay: Long): Boolean {
        val result = runShellCommand("input", "keyevent", "KEYCODE_HOME")
        sleep(delay)
        return result.isBlank()
    }

    override fun inputText(text: String): Boolean {
        val encodeText = Base64.getEncoder().encodeToString(text.toByteArray())
        val result = runShellCommand("input", "am", "broadcast", "-a", "ADB_INPUT_B64", "--es", "msg", encodeText)
        return result.isBlank()
    }

    override fun clearText(): Boolean {
        val result = runShellCommand("am", "broadcast", "-a", "ADB_CLEAR_TEXT")
        return result.isBlank()
    }

    override fun getScreenshot(): ScreenshotInfo? = screenshot.getScreenshot()

}
