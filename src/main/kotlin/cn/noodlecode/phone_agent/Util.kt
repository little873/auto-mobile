package cn.noodlecode.phone_agent

object Util {
    fun runCommand(vararg args: String): String {
        val process = ProcessBuilder(*args)
            .redirectErrorStream(true)
            .start()

        return process.inputStream.use { it.reader().readText() }.also {
            process.waitFor()
        }
    }
}