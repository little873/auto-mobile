import cn.noodlecode.phone_agent.PhoneAgent
import cn.noodlecode.phone_agent.cli.ChatCli
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val agent = PhoneAgent()
    agent.run("打开 Pokemon Trading Card Game,里面有个社群的页签，有一个分送的功能，请选择第一个朋友，随便分送一张对方没有的牌，分送成功后，有个向上滑动的动作，需要快速向上滑才可以")//    ChatCli().start()}