package cn.noodlecode.phone_agent.model

import kotlinx.coroutines.flow.Flow

class ModelGateway(private val modelConfig: ModelConfig) {

    val apiClient = ApiClient(modelConfig)

    suspend fun request(
        messageList: List<Message>,
    ): ModelResponse {
        val request = ChatCompletionRequest(
            model = modelConfig.modelName,
            messages = messageList,
            temperature = modelConfig.temperature,
            maxTokens = modelConfig.maxToken,
            stream = false
        )
        val response = apiClient.createChatCompletion(request)
        if (response.isSuccess) {
            val chatResponse = response.getOrNull()
            val content = chatResponse?.choices?.firstOrNull()?.message?.content ?: ""
            return ModelResponse(content)
        } else {
            throw Exception("请求失败: ${response.exceptionOrNull()?.message}")
        }
    }

    fun requestStream(
        messageList: List<Message>,
    ): Flow<String> {
        val request = ChatCompletionRequest(
            model = modelConfig.modelName,
            messages = messageList,
            temperature = modelConfig.temperature,
            maxTokens = modelConfig.maxToken,
            stream = true
        )
        return apiClient.createChatCompletionStream(request)
    }
}
