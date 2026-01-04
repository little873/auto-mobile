package cn.noodlecode.phone_agent.model

class Client(private val modelConfig: ModelConfig) {

    val apiClient = ApiClient(modelConfig)

    suspend fun request(
        messageList: List<Message>,
    ): ModelResponse {
        val request = ChatCompletionRequest(
            model = modelConfig.modelName,
            messages = messageList,
            temperature = modelConfig.temperature,
            maxTokens = modelConfig.maxToken,
            stream = true
        )
        var fullAssistantResponse = ""
        fullAssistantResponse = StringBuilder().apply {
            apiClient.createChatCompletionStream(request).collect { chunk ->
                print(chunk) // 逐字输出
                append(chunk)
                // 刷新控制台输出（确保立即显示）
                System.out.flush()
            }
        }.toString()
        println() // 流结束后换行
        return ModelResponse(fullAssistantResponse)
    }
}