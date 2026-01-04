package cn.noodlecode.phone_agent.model

import cn.noodlecode.phone_agent.config.Env

class ChatCli(private val modelConfig: ModelConfig? = Env.modelConfig, private val prompts: String? = Env.prompts) {

    val conversation = mutableListOf<Message>()

    suspend fun start(useStream: Boolean = true) {
        if (modelConfig == null) return
        if (prompts?.isNotBlank() == true) {
            conversation.add(
                Message(role = "system", content = listOf(TextContent(Env.prompts)))
            )
        }
        val client = ApiClient(modelConfig)
        println("🚀 多轮对话已启动（输入 'quit' 或 'exit' 退出）")
        println("📌 模式: ${if (useStream) "Stream（流式）" else "Non-Stream（完整）"}")
        println("──────────────────────────────────────")
        while (true) {
            print("👤 You: ")
            val userInput = readlnOrNull()?.trim() ?: break

            if (userInput.equals("quit", ignoreCase = true) ||
                userInput.equals("exit", ignoreCase = true)
            ) {
                println("👋 再见！")
                break
            }

            if (userInput.isEmpty()) continue

            // 添加用户消息
            conversation.add(
                Message(role = "user", content = listOf(TextContent(userInput)))
            )

            val request = ChatCompletionRequest(
                model = modelConfig.modelName,
                messages = conversation,
                temperature = modelConfig.temperature,
                maxTokens = modelConfig.maxToken,
                stream = useStream // 实际传给 API 的值（ApiClient 内部会 copy(stream=...)）
            )

            var fullAssistantResponse = ""

            try {
                if (useStream) {
                    // ========== Stream 模式 ==========
                    print("🤖 Assistant: ")
                    fullAssistantResponse = StringBuilder().apply {
                        client.createChatCompletionStream(request).collect { chunk ->
                            print(chunk) // 逐字输出
                            append(chunk)
                            // 刷新控制台输出（确保立即显示）
                            System.out.flush()
                        }
                    }.toString()
                    println() // 流结束后换行
                } else {
                    // ========== Non-Stream 模式 ==========
                    val response = client.createChatCompletion(request)
                    val assistantMessage = response.getOrNull()?.choices?.firstOrNull()?.message
                    fullAssistantResponse = assistantMessage?.content ?: "（无回复内容）"
                    println("🤖 Assistant: $fullAssistantResponse")
                }

                // 将完整回复加入历史（用于下一轮上下文）
                conversation.add(
                    Message(role = "assistant", content = listOf(TextContent(fullAssistantResponse)))
                )

            } catch (e: Exception) {
                println("\n❌ 请求失败: ${e.message}")
                // 不将失败回复加入历史
            }

            println("──────────────────────────────────────")
        }
    }
}