package cn.noodlecode.phone_agent.device.adb

import cn.noodlecode.phone_agent.Util

open class AdbShell(val deviceId: String? = null) {

    fun runShellCommand(vararg args: String): String {
        val adb = when (deviceId) {
            null -> arrayOf("adb", "shell")
            else -> arrayOf("adb", "-s", deviceId, "shell")
        }
        return Util.runCommand(*adb + args)
    }

    fun runCommand(vararg args: String): String {
        val adb = when (deviceId) {
            null -> arrayOf("adb")
            else -> arrayOf("adb", "-s", deviceId)
        }
        return Util.runCommand(*adb + args)
    }
}