package cn.noodlecode.phone_agent

import cn.noodlecode.phone_agent.config.Env
import cn.noodlecode.phone_agent.config.getPackageName
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
        val jsonObject = try {
            Json.parseToJsonElement(action).jsonObject
        } catch (e: Exception) {
            println("解析 Action JSON 失败: $action")
            return ActionResult(success = false, finish = false)
        }

        val type: String? = jsonObject["type"]?.jsonPrimitive?.content

        return when (type) {
            "launch" -> {
                val appName = jsonObject["app"]?.jsonPrimitive?.content ?: ""
                val packageName = getPackageName(appName)
                if (packageName != null) {
                    ActionResult(control.launch(packageName), false)
                } else {
                    println("未找到应用包名: $appName")
                    ActionResult(false, false)
                }
            }

            "back" -> ActionResult(control.back(), false)

            "home" -> ActionResult(control.home(), false)

            "tap" -> {
                val x = ((jsonObject["x"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.width).toInt()
                val y = ((jsonObject["y"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.height).toInt()
                ActionResult(control.tap(x, y), false)
            }

            "longPress" -> {
                val x = ((jsonObject["x"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.width).toInt()
                val y = ((jsonObject["y"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.height).toInt()
                ActionResult(control.longPress(x, y), false)
            }

            "doubleTap" -> {
                val x = ((jsonObject["x"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.width).toInt()
                val y = ((jsonObject["y"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.height).toInt()
                ActionResult(control.doubleTap(x, y), false)
            }

            "input", "inputName" -> {
                val text = jsonObject["text"]?.jsonPrimitive?.content ?: ""
                control.clearText()
                ActionResult(control.inputText(text), false)
            }

            "swipe" -> {
                val startX = ((jsonObject["startX"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.width).toInt()
                val startY = ((jsonObject["startY"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.height).toInt()
                val endX = ((jsonObject["endX"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.width).toInt()
                val endY = ((jsonObject["endY"]?.jsonPrimitive?.int ?: 0) / 1000f * screenshot.height).toInt()
                ActionResult(control.swipe(startX, startY, endX, endY), false)
            }

            "wait" -> {
                val duration = jsonObject["duration"]?.jsonPrimitive?.int ?: 1
                Thread.sleep(duration * 1000L)
                ActionResult(true, false)
            }

            "note" -> {
                val message = jsonObject["message"]?.toString() ?: ""
                println("📝 记录信息: $message")
                ActionResult(true, false)
            }

            "callAPI" -> {
                val instruction = jsonObject["instruction"]?.jsonPrimitive?.content ?: ""
                println("🤖 调用分析 API: $instruction")
                ActionResult(true, false)
            }

            "interact", "takeOver" -> {
                val message = jsonObject["message"]?.jsonPrimitive?.content ?: ""
                println("⚠️ 需要人工干预 ($type): $message")
                println("请在手机上完成操作后，在此处输入 'ok' 继续...")
                readlnOrNull()
                ActionResult(true, false)
            }

            "finish" -> {
                val message = jsonObject["message"]?.jsonPrimitive?.content ?: ""
                println("✅ 任务结束: $message")
                ActionResult(success = true, finish = true)
            }

            else -> {
                println("未知操作类型: $type")
                ActionResult(false, false)
            }
        }
    }

}
