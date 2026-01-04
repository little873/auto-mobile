package cn.noodlecode.phone_agent

import cn.noodlecode.phone_agent.config.Env
import cn.noodlecode.phone_agent.device.DeviceControl
import cn.noodlecode.phone_agent.device.ScreenshotInfo
import cn.noodlecode.phone_agent.device.adb.AndroidControl
import cn.noodlecode.phone_agent.model.ActionResult
import cn.noodlecode.phone_agent.model.Client
import cn.noodlecode.phone_agent.model.ImageUrl
import cn.noodlecode.phone_agent.model.ImageUrlContent
import cn.noodlecode.phone_agent.model.Message
import cn.noodlecode.phone_agent.model.ModelConfig
import cn.noodlecode.phone_agent.model.TextContent
import com.openai.models.chat.completions.ChatCompletionContentPart
import com.openai.models.chat.completions.ChatCompletionContentPartImage
import com.openai.models.chat.completions.ChatCompletionContentPartText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class PhoneAgent(
    modelConfig: ModelConfig = Env.modelConfig,
    private val systemPrompt: String = Env.prompts,
    deviceId: String? = null,
) {
    private val control: DeviceControl = AndroidControl(deviceId)
    private val client = Client(modelConfig)

    private var stepCount = 0
    private var messageList = mutableListOf<Message>()

    suspend fun run(userPrompt: String) {
        messageList.clear()
        messageList.add(
            Message("system", listOf(TextContent(systemPrompt)))
        )
        stepCount = 0
        while (stepCount < 100) {
            val isFinish = executeStep(userPrompt)
            if (isFinish) {
                println("完成了")
                return
            }
        }
        println("达到最大次数")
    }

    suspend fun executeStep(userPrompt: String): Boolean {
        stepCount++
        println()
        println("第 $stepCount 次执行任务")
        val screenshot = control.getScreenshot() ?: throw Exception("获取截图失败,流程终止")
        val currentApp = control.getCurrentApp()

        var userMessage = "$userPrompt\n\n"
        // 添加描述
        if (stepCount != 1) {
            userMessage = "** Screen Info **\n\n"
        }
        messageList.add(
            Message(
                "user", listOf(
                    // 添加描述
                    TextContent("$userMessage${Json.encodeToString(mapOf("current_app" to currentApp))}"),
                    // 添加截图
                    ImageUrlContent(ImageUrl("data:image/jpeg;base64,${screenshot.base64Data}"))
                )
            )
        )

        println("\n" + "=".repeat(50))
        println("💭 思考中:")
        println("-".repeat(50))
        val response = client.request(messageList)
        // ✅ 成功后：从 messageList 的每条消息中移除 ImageUrlContent
        messageList = messageList.map { message ->
            message.copy(
                content = message.content.filterIsInstance<TextContent>() // 只保留 TextContent
            )
        }.toMutableList()

        println("\n" + "=".repeat(50))
        println("🎯 执行操作:")
        val action = response.action()
        println(action)
        val actionResult = actionExecute(action, screenshot)

        messageList.add(Message("assistant", listOf(TextContent(response.data))))
        if (!actionResult.success) {
            messageList.add(Message("user", listOf(TextContent("answer 执行失败"))))
        }

        return actionResult.finish
    }

    fun actionExecute(action: String, screenshot: ScreenshotInfo): ActionResult {
        val jsonObject = Json.parseToJsonElement(action).jsonObject
        val type: String? = jsonObject["type"]?.jsonPrimitive?.content
        when (type) {
            "tap" -> {
                val x = ((jsonObject["x"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.width).toInt()
                val y = ((jsonObject["y"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.height).toInt()
                return ActionResult(control.tap(x, y), false)
            }

            "finish" -> ActionResult(success = true, finish = true)
        }
        return ActionResult(success = false, finish = false)
    }

}
