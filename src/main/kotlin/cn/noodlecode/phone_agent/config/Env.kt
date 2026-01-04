package cn.noodlecode.phone_agent.config

import cn.noodlecode.phone_agent.model.ModelConfig
import kotlinx.serialization.json.Json
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val weekdayNames = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

object Env {
    val modelConfig: ModelConfig by lazy {
        val jsonString = getResource("model-config.json")?.readText() ?: ""
        Json.decodeFromString<ModelConfig>(jsonString)
    }

    val prompts: String by lazy {
        val today = LocalDate.now()
        val weekday = weekdayNames[today.dayOfWeek.value - 1]
        val prompt = getResource("prompts-zh")?.readText()
        "今天的日期是:${today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) + " " + weekday}\n$prompt"
    }

    fun getResource(name: String): URL? {
        return this::class.java.classLoader.getResource(name)
    }
}