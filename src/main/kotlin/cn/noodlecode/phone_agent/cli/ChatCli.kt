package cn.noodlecode.phone_agent.cli

import cn.noodlecode.phone_agent.config.Env
import cn.noodlecode.phone_agent.model.Message
import cn.noodlecode.phone_agent.model.ModelConfig
import cn.noodlecode.phone_agent.model.ModelGateway
import cn.noodlecode.phone_agent.model.TextContent
import kotlinx.coroutines.flow.collect

class ChatCli(private val modelConfig: ModelConfig? = Env.modelConfig, private val prompts: String? = null) {

    val conversation = mutableListOf<Message>()

    suspend fun start(useStream: Boolean = true) {
        if (modelConfig == null) return
        if (prompts?.isNotBlank() == true) {
            conversation.add(
                Message(role = "system", content = listOf(TextContent(prompts)))
            )
        }
        val client = ModelGateway(modelConfig)
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

            try {
                if (useStream) {
                    // 流式模式
                    print("🤖 Assistant: ")
                    val fullAssistantResponse = StringBuilder()
                    client.requestStream(conversation).collect { chunk ->
                        print(chunk) // 逐字输出
                        fullAssistantResponse.append(chunk)
                        System.out.flush()
                    }
                    println() // 流结束后换行

                    // 将完整回复加入历史
                    conversation.add(
                        Message(role = "assistant", content = listOf(TextContent(fullAssistantResponse.toString())))
                    )
                } else {
                    // 非流式模式
                    val response = client.request(conversation)
                    val fullAssistantResponse = response.data
                    println("🤖 Assistant: $fullAssistantResponse")

                    // 将完整回复加入历史
                    conversation.add(
                        Message(role = "assistant", content = listOf(TextContent(fullAssistantResponse)))
                    )
                }

            } catch (e: Exception) {
                println("\n❌ 请求失败: ${e.message}")
                // 不将失败回复加入历史
            }

            println("──────────────────────────────────────")
        }
    }
}
