# auto-mobile - 手机端智能助理框架

auto-mobile 是一个基于 Kotlin 开发的手机端智能助理框架，灵感源于 Open-AutoGLM。它能够以多模态方式理解手机屏幕内容，并通过自动化操作帮助用户完成任务。用户只需用自然语言描述需求，auto-mobile 即可自动解析意图、理解当前界面、规划下一步动作并完成整个流程。

## ✨ 核心特性

- **多模态屏幕理解**：结合视觉语言模型分析手机屏幕截图
- **自动化操作**：通过 ADB 执行点击、滑动、输入、启动应用等操作
- **智能任务规划**：根据用户自然语言指令自动规划操作流程
- **安全机制**：内置敏感操作确认和人工接管机制
- **远程控制**：支持 WiFi 或网络连接设备的远程 ADB 调试
- **多应用支持**：预置 100+ 常见 Android 应用包名映射
- **流式响应**：支持流式和非流式两种模型响应模式
- **配置灵活**：支持自定义模型配置和提示词模板
- **日期感知**：自动在提示词中注入当前日期信息

## 🚀 快速开始

### 前置要求

1. **Android 设备**：已开启开发者选项和 USB 调试
2. **ADB 工具**：Android SDK Platform-Tools (建议版本 34+)
3. **Java 环境**：JDK 21 或更高版本
4. **AI 模型 API**：支持视觉语言模型的 API 服务（如 OpenAI GPT-4V）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/little873/auto-mobile.git
   cd auto-mobile
   ```

2. **配置模型 API**
   在 `src/main/resources/` 目录下创建 `model-config.json` 文件：
   ```json
   {
     "baseUrl": "https://api.openai.com/v1",
     "apiKey": "sk-your-api-key-here",
     "modelName": "gpt-4-vision-preview",
     "maxToken": 3000,
     "temperature": 0.0,
     "frequencyPenalty": 0.2,
     "topP": 0.85
   }
   ```

3. **构建项目**
   ```bash
   ./gradlew build
   ```

4. **连接设备**
   ```bash
   adb devices  # 确认设备已连接
   ```

5. **运行示例**
   ```bash
   ./gradlew run
   ```

## 📁 项目结构

```
auto-mobile/
├── src/main/kotlin/cn/noodlecode/phone_agent/
│   ├── PhoneAgent.kt              # 主智能体类，协调任务执行
│   ├── device/                    # 设备控制模块
│   │   ├── DeviceControl.kt       # 设备控制接口
│   │   └── adb/                  # ADB 实现
│   │       ├── AndroidControl.kt  # Android 设备控制实现
│   │       ├── AdbShell.kt       # ADB Shell 基础类
│   │       ├── Screenshot.kt     # 截图处理类
│   │       └── Input.kt          # 输入操作类
│   ├── model/                    # 模型交互模块
│   │   ├── Client.kt             # 模型客户端
│   │   ├── ApiClient.kt          # API 客户端（支持流式/非流式）
│   │   ├── Message.kt            # 消息和模型相关数据类
│   │   ├── ChatCli.kt            # 聊天客户端（交互式对话）
│   │   └── ModelResponse.kt      # 模型响应处理
│   ├── config/                   # 配置管理
│   │   ├── Env.kt                # 环境配置加载
│   │   └── Apps.kt               # 应用包名映射
│   └── Util.kt                   # 工具函数
├── src/main/resources/
│   ├── model-config.json         # 模型配置
│   └── prompts-zh               # 中文提示词模板
├── src/main/kotlin/main.kt       # 主程序入口
├── build.gradle                  # 构建配置
├── gradle.properties            # Gradle 属性（kotlin.code.style=official）
├── settings.gradle              # 项目设置
└── gradlew                      # Gradle 包装器
```

## 🔧 配置说明

### 1. 模型配置 (`model-config.json`)

创建 `src/main/resources/model-config.json` 文件，配置格式如下：

```json
{
  "baseUrl": "https://api.openai.com/v1",
  "apiKey": "sk-your-api-key-here",
  "modelName": "gpt-4-vision-preview",
  "maxToken": 3000,
  "temperature": 0.0,
  "frequencyPenalty": 0.2,
  "topP": 0.85
}
```

**参数说明：**
- `baseUrl`: 模型 API 的基础 URL
- `apiKey`: API 密钥
- `modelName`: 模型名称（需支持视觉输入）
- `maxToken`: 最大生成 token 数（默认 3000）
- `temperature`: 生成温度（0.0-2.0，默认 0.0）
- `frequencyPenalty`: 频率惩罚（-2.0 到 2.0，默认 0.2）
- `topP`: 核采样概率（0.0-1.0，默认 0.85）

### 2. 提示词模板 (`prompts-zh`)

系统使用结构化提示词指导 AI 模型执行操作，包含：
- **操作指令格式规范**：必须输出 `<explain>{think}</explain><answer>{action}</answer>` 格式
- **18 条任务执行规则**：涵盖应用启动、页面导航、错误处理等最佳实践
- **16 种可执行操作**：完整的操作类型列表和 JSON 格式说明
- **日期注入**：自动添加当前日期信息（如"今天的日期是:2024年01月15日 星期一"）

### 3. 应用包名映射

项目预置了 100+ 常见 Android 应用的包名映射，支持快速启动。完整列表见 `src/main/kotlin/cn/noodlecode/phone_agent/config/Apps.kt`，包括：
- 社交应用：微信、QQ、微博、抖音、小红书等
- 电商购物：淘宝、京东、拼多多、美团、饿了么等
- 生活服务：高德地图、携程、铁路12306、滴滴出行等
- 娱乐游戏：bilibili、腾讯视频、Pokemon Trading Card Game、星穹铁道等
- 工具应用：Chrome、Gmail、Google Maps、文件管理器等

## 💻 使用方法

### 基本使用

```kotlin
import cn.noodlecode.phone_agent.PhoneAgent
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 创建智能体（使用默认配置）
    val agent = PhoneAgent()
    
    // 执行任务（示例来自 main.kt）
    agent.run("打开 Pokemon Trading Card Game,里面有个社群的页签，有一个分送的功能，请选择第一个朋友，随便分送一张对方没有的牌，分送成功后，有个向上滑动的动作，需要快速向上滑才可以")
    
    // 或使用特定设备ID
    val agentWithDevice = PhoneAgent(deviceId = "emulator-5554")
    agentWithDevice.run("打开微信，找到张三，发送'晚上一起吃饭吗？'")
}
```

### 交互式对话模式

```kotlin
import cn.noodlecode.phone_agent.model.ChatCli
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 启动交互式对话（支持流式响应）
    ChatCli().start(useStream = true)
    
    // 或使用非流式模式
    // ChatCli().start(useStream = false)
}
```

在对话模式中，输入 `quit` 或 `exit` 退出。

### 支持的指令示例

```kotlin
// 1. 应用启动和导航
agent.run("打开微信")
agent.run("返回上一页")
agent.run("回到桌面")

// 2. 社交应用操作
agent.run("在小红书搜索'咖啡厅推荐'，收藏前3个图文笔记")
agent.run("在抖音关注'科技博主'")

// 3. 购物操作
agent.run("在淘宝搜索'无线耳机'，按价格从低到高排序，购买第一个")
agent.run("清空美团购物车，然后购买一份披萨和可乐")

// 4. 游戏操作
agent.run("在星穹铁道中开启自动战斗，完成日常任务")

// 5. 复杂任务
agent.run("找到宠物友好的餐厅，预订今晚7点两人位")
agent.run("在12306上查询明天北京到上海的高铁，选择最早的一班")
```

## 🤖 工作原理

### 执行流程
1. **屏幕捕获**：通过 ADB `screencap -p` 命令获取当前屏幕截图，保存为临时 PNG 文件
2. **图片处理**：转换为 JPEG 格式并压缩（质量 0.7），Base64 编码
3. **应用识别**：解析 `dumpsys window` 输出识别当前应用
4. **多模态分析**：将截图、应用信息和用户指令发送给视觉语言模型
5. **动作规划**：模型返回结构化操作指令（JSON 格式），包含思考过程 `<explain>` 和动作 `<answer>`
6. **指令执行**：通过 ADB 执行相应操作（tap、swipe、input 等）
7. **状态验证**：检查操作结果，必要时重试或调整
8. **循环迭代**：重复 1-7 步直到任务完成或达到最大步数（默认 100 步）

### 消息格式
系统使用标准的多模态消息格式与模型交互：
```kotlin
Message(
    role = "user",
    content = listOf(
        TextContent("用户指令和当前应用信息"),
        ImageUrlContent(ImageUrl("data:image/jpeg;base64,..."))
    )
)
```

### 坐标系统
- 使用归一化坐标系统 (0-999)
- 左上角为原点 (0,0)，右下角为 (999,999)
- 系统自动转换为实际屏幕坐标：
  ```kotlin
  actualX = (normalizedX / 1000f * screenshot.width).toInt()
  actualY = (normalizedY / 1000f * screenshot.height).toInt()
  ```

## 📋 支持的操作类型

系统支持 16 种操作类型，完整列表如下：

| 类型 | 描述 | JSON 格式示例 | 使用场景 |
|------|------|---------------|----------|
| `launch` | 启动应用 | `{"type": "launch", "app": "微信"}` | 打开指定应用 |
| `back` | 返回上一级 | `{"type": "back"}` | 返回、关闭弹窗 |
| `home` | 返回桌面 | `{"type": "home"}` | 退出当前任务 |
| `tap` | 点击屏幕 | `{"type": "tap", "x": 500, "y": 300}` | 点击按钮、选项 |
| `longPress` | 长按屏幕 | `{"type": "longPress", "x": 500, "y": 300}` | 触发上下文菜单 |
| `doubleTap` | 双击屏幕 | `{"type": "doubleTap", "x": 500, "y": 300}` | 缩放、激活功能 |
| `input` | 文本输入 | `{"type": "input", "text": "搜索内容"}` | 在输入框输入文本 |
| `inputName` | 输入人名 | `{"type": "inputName", "text": "张三"}` | 输入姓名信息 |
| `swipe` | 滑动/滚动 | `{"type": "swipe", "startX": 500, "startY": 800, "endX": 500, "endY": 200}` | 滚动列表、切换页面 |
| `wait` | 等待加载 | `{"type": "wait", "duration": 3}` | 页面加载、网络延迟 |
| `note` | 记录信息 | `{"type": "note", "message": "关键信息"}` | 记录页面内容 |
| `callAPI` | 调用分析API | `{"type": "callAPI", "instruction": "分析页面内容"}` | 请求总结分析 |
| `interact` | 请求用户选择 | `{"type": "interact"}` | 多个选项需要决策 |
| `takeOver` | 请求用户接管 | `{"type": "takeOver", "message": "需要登录验证"}` | 登录验证等复杂环节 |
| `finish` | 结束任务 | `{"type": "finish", "message": "任务完成"}` | 任务准确完成 |

**敏感操作**：涉及支付、隐私等操作时，`tap` 操作必须附带 `message` 字段说明。

## ⚙️ 技术细节

### 截图处理优化
- **格式转换**：PNG → JPEG 减少传输体积
- **压缩优化**：质量 0.7，平衡清晰度和文件大小
- **背景处理**：透明通道转换为白色背景
- **自动清理**：删除临时文件，避免存储泄漏
- **大小监控**：输出压缩后文件大小供调试

### ADB 命令封装
- **设备支持**：支持单设备和多设备连接（通过 `deviceId` 参数）
- **错误处理**：统一的异常捕获和日志记录
- **延迟控制**：操作后自动等待指定时间（默认 1000ms）
- **命令安全**：使用 `ProcessBuilder` 安全执行系统命令

### 模型交互特性
- **双模式支持**：流式（实时显示）和非流式（完整响应）
- **SSE 处理**：自动解析 Server-Sent Events 数据流
- **Token 优化**：自动从历史消息中移除图片内容减少消耗
- **错误恢复**：完善的异常处理和重试机制
- **响应解析**：自动提取思考内容和动作指令

### 配置管理
- **懒加载**：使用 `by lazy` 延迟加载配置资源
- **资源定位**：通过类加载器获取资源文件
- **日期感知**：自动注入当前日期和星期信息
- **环境隔离**：配置与代码分离，便于部署

## 🛠️ 开发指南

### 环境配置
```bash
# 使用 Gradle Wrapper（项目已包含）
./gradlew build

# 运行测试
./gradlew test

# 清理构建
./gradlew clean

# 运行主程序
./gradlew run
```

### 依赖管理
项目使用以下主要依赖：
- **Kotlin**: 2.2.21（协程支持）
- **Ktor**: 3.3.3（HTTP 客户端和 SSE）
- **kotlinx.serialization**: JSON 序列化
- **OpenAI Java**: 4.13.0（API 客户端）

### 代码规范
- **官方风格**：遵循 Kotlin 官方编码规范（`kotlin.code.style=official`）
- **不可变性**：优先使用 `val`，仅在必要时使用 `var`
- **错误处理**：关键路径使用 `try-catch`，可预见失败返回 `Result` 或 `null`
- **协程使用**：充分利用 Kotlin 协程进行异步操作
- **扩展函数**：使用扩展函数简化工具类
- **单例对象**：工具类使用 `object` 声明

### 添加新功能
1. **添加新应用支持**
   ```kotlin
   // 在 Apps.kt 的 ANDROID_APP_PACKAGES 中添加
   "应用显示名" to "应用包名"
   ```

2. **添加新操作类型**
   ```kotlin
   // 1. 在 prompts-zh 中添加操作说明
   // 2. 在 PhoneAgent.actionExecute() 中添加处理逻辑
   when (type) {
       "newAction" -> {
           // 处理新操作
           ActionResult(success = true, finish = false)
       }
   }
   ```

3. **扩展设备控制**
   ```kotlin
   // 在 DeviceControl 接口中添加新方法
   // 在 AndroidControl 中实现
   ```

### 调试技巧
```kotlin
// 1. 检查设备连接
println("当前应用: ${control.getCurrentApp()}")

// 2. 验证截图质量
val screenshot = control.getScreenshot()
if (screenshot != null) {
    println("截图尺寸: ${screenshot.width}x${screenshot.height}")
    println("Base64 长度: ${screenshot.base64Data.length}")
}

// 3. 调试模型响应
val response = client.request(messageList)
println("思考内容: ${response.thinking()}")
println("动作指令: ${response.action()}")

// 4. 监控执行步骤
println("第 $stepCount 次执行任务")
```

## ⚠️ 注意事项

### 使用限制
1. **设备要求**：
   - Android 5.0+ 设备
   - 已开启 USB 调试
   - 屏幕解锁状态

2. **网络要求**：
   - 稳定网络连接访问 AI 模型 API
   - 建议在 WiFi 环境下使用
   - API 调用可能有速率限制

3. **性能考虑**：
   - 单次循环通常需要 5-15 秒（截图+模型调用+执行）
   - 复杂任务可能需要多次迭代
   - 默认最大步数 100，防止无限循环

4. **兼容性问题**：
   - 不同设备型号可能需调整点击坐标
   - 某些定制 ROM 可能修改了标准交互
   - 应用更新可能改变界面布局

### 最佳实践
1. **指令明确**：提供清晰、具体的任务描述
2. **分步执行**：复杂任务分解为多个简单指令
3. **监控执行**：观察执行过程，必要时人工干预
4. **错误处理**：遇到失败时检查日志，调整指令重试
5. **资源管理**：长时间运行注意内存和存储使用

## 📄 许可证

[待补充 - 请添加 LICENSE 文件并在此说明协议类型]

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！请参考项目文档：
- `AGENTS.md` - AI 代理开发指南和项目概述
- `MEMO.md` - 详细架构说明和技术笔记
- `TODO.md` - 待完成任务和功能规划

### 贡献流程
1. Fork 项目仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 代码审查标准
- 遵循项目代码风格规范
- 包含必要的测试用例
- 更新相关文档
- 通过现有测试套件

## 📞 支持与反馈

- **GitHub Issues**: [https://github.com/little873/auto-mobile/issues](https://github.com/little873/auto-mobile/issues)
- **项目主页**: [https://github.com/little873/auto-mobile](https://github.com/little873/auto-mobile)

### 问题反馈模板
```markdown
## 问题描述
[清晰描述遇到的问题]

## 复现步骤
1. [步骤1]
2. [步骤2]
3. [步骤3]

## 预期行为
[期望的正常行为]

## 实际行为
[实际观察到的行为]

## 环境信息
- 设备型号: [如 Samsung Galaxy S23]
- Android 版本: [如 Android 14]
- auto-mobile 版本: [如 1.0-SNAPSHOT]
- 网络环境: [如 WiFi/4G]

## 日志输出
[相关日志或错误信息]

## 截图
[如有，添加相关截图]
```

## 🚀 未来规划

基于项目当前状态，可能的改进方向包括：
- 更多应用支持扩展
- 本地模型集成优化
- 操作录制和回放功能
- 性能监控和优化
- 跨平台支持扩展

---

**让手机自动化变得更智能、更简单！**

*auto-mobile - 您的手机智能助理，理解您的意图，执行您的指令。*
