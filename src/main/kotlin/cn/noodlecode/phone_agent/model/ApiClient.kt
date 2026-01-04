package cn.noodlecode.phone_agent.model

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApiClient(private val modelConfig: ModelConfig) {

    private val jsonHandler = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonHandler)
        }
        install(SSE)
    }

    /**
     * 非 stream 模式：完整响应
     */
    suspend fun createChatCompletion(
        request: ChatCompletionRequest,
    ): Result<ChatCompletionResponse> {
        return try {
            val httpResponse = client.post("${modelConfig.baseUrl}/chat/completions") {
                header("Authorization", "Bearer ${modelConfig.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(request.copy(stream = false))
            }
            if (httpResponse.status == HttpStatusCode.OK) {
                Result.success(httpResponse.body())
            } else {
                val errorBody = httpResponse.bodyAsText()
                println("请求chat/completions失败: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            println("请求chat/completions异常: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Stream 模式：返回文本内容流（逐字/逐片段）
     *
     * @return Flow<String> - 每个 emission 是一个文本片段（可能为空或 "[DONE]"）
     */
    fun createChatCompletionStream(
        request: ChatCompletionRequest,
    ): Flow<String> = flow {
        try {
            val httpResponse = client.post("${modelConfig.baseUrl}/chat/completions") {
                header("Authorization", "Bearer ${modelConfig.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(request.copy(stream = true))
            }
            if (httpResponse.status != HttpStatusCode.OK) {
                val errorBody = httpResponse.bodyAsText()
                println("请求chat/completions失败: $errorBody")
                throw Exception("请求chat/completions失败: $errorBody")
            }
            val channel: ByteReadChannel = httpResponse.body()
            val reader = channel.toInputStream().bufferedReader(Charsets.UTF_8)
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line!!
                if (currentLine.startsWith("data:")) {
                    val data = currentLine.removePrefix("data:").trim()

                    if (data == "[DONE]") {
                        break // 流结束
                    }
                    if (data.isNotEmpty()) {
                        try {
                            // 解析 JSON chunk
                            val chunk = Json.decodeFromString<JsonObject>(data)
                            val delta = chunk["choices"]
                                ?.jsonArray?.getOrNull(0)
                                ?.jsonObject?.get("delta")
                                ?.jsonObject

                            val text = delta?.get("content")?.jsonPrimitive?.content
                            if (!text.isNullOrBlank()) {
                                emit(text) // 发送文本片段
                            }
                        } catch (parseEx: Exception) {
                            println("⚠️ Chunk 解析失败: $data | Error: ${parseEx.message}")
                        }
                    }
                }
            }
            reader.close()

        } catch (e: Exception) {
            println("请求chat/completions异常: ${e.message}")
        }
    }

}