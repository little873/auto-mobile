import cn.noodlecode.phone_agent.PhoneAgent
import cn.noodlecode.phone_agent.config.Env
import cn.noodlecode.phone_agent.model.ApiClient
import cn.noodlecode.phone_agent.model.ChatCli
import cn.noodlecode.phone_agent.model.ChatCompletionRequest
import cn.noodlecode.phone_agent.model.Message
import cn.noodlecode.phone_agent.model.TextContent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val agent = PhoneAgent()
    agent.run("打开 PTCG 游戏")
}