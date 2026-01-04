package cn.noodlecode.phone_agent.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 请求相关
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false,
    val temperature: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
)

@Serializable
data class Message(
    val role: String, // "user", "system", "assistant"
    val content: List<Content>,
)

@Serializable
sealed interface Content

@Serializable
@SerialName("text")
data class TextContent(val text: String) : Content

@Serializable
@SerialName("image_url")
data class ImageUrlContent(@SerialName("image_url") val imageUrl: ImageUrl) : Content

@Serializable
data class ImageUrl(val url: String)

// 非 stream 响应
@Serializable
data class ChatCompletionResponse(
    val id: String,
    @SerialName("object") val obj: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null,
)

@Serializable
data class Choice(
    val index: Int,
    val message: MessageResponse,
    @SerialName("finish_reason") val finishReason: String?,
)

@Serializable
data class MessageResponse(
    val role: String,
    val content: String,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int,
)

@Serializable
data class ModelConfig(
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val maxToken: Int = 3000,
    val temperature: Double = 0.0,
    val frequencyPenalty: Double = 0.2,
    val topP: Double = 0.85,
)

data class ModelResponse(
    val data: String,
) {
    fun thinking(): String {
        val thinkRegex = Regex("""<explain>([\s\S]*?)</explain>""")
        val thinkMatch = thinkRegex.find(data)
        return thinkMatch?.groupValues?.get(1) ?: ""
    }

    fun action(): String {
        // 解析 answer 内容
        val answerRegex = Regex("""<answer>([\s\S]*?)</answer>""")
        val answerMatch = answerRegex.find(data)
        return answerMatch?.groupValues?.get(1) ?: ""
    }
}

data class ActionResult(
    val success: Boolean,
    val finish: Boolean
)
