# MEMO.md

## 项目背景
`auto-mobile` 是一个基于 Kotlin 开发的 Android 自动化智能助理框架，灵感源于 Open-AutoGLM。它结合了 ADB 设备控制技术和视觉语言模型（VLM），旨在通过自然语言指令实现手机端的自动化任务处理。

## 技术架构
- **核心语言**: Kotlin (遵循官方编码规范)
- **构建工具**: Gradle 8.14
- **设备控制**: 通过 `ProcessBuilder` 调用系统 `adb` 指令，封装在 `AdbShell` 中。
- **模型交互**: 支持 OpenAI 兼容格式的 API 接口，使用 Ktor Client 处理多模态输入（文本+图像）。
- **配置管理**: 使用 `kotlinx.serialization` 处理 JSON 配置，支持动态加载提示词和应用包名映射。

## 模块说明
- `cn.noodlecode.phone_agent`:
    - `PhoneAgent`: 核心自动化代理类。负责循环执行任务：获取截图 -> 调用 VLM 规划 -> 解析 Action -> 执行设备操作 -> 判断是否结束。
- `cn.noodlecode.phone_agent.device.adb`:
    - `AdbShell`: 封装了 ADB 的基础操作，支持多设备管理（通过 `deviceId`）。
    - `AndroidControl`: 实现了 `DeviceControl` 接口，提供了 tap, swipe, input, screenshot 等具体操作。
    - `Screenshot`: 负责通过 `screencap` 获取屏幕截图并转换为 Base64。
    - `Input`: (待实现) 输入控制相关类。
- `cn.noodlecode.phone_agent.model`:
    - `ModelGateway`: 模型交互的统一网关，负责封装请求逻辑并调用 `ApiClient`。
    - `ApiClient`: 基于 Ktor 的底层 HTTP 客户端，处理 SSE 流式响应和原始 API 请求。
    - `Message`: 定义了多模态消息结构。**注意**：当前正在向支持 OpenAI Function Calling 演进，`content` 字段计划改为 `JsonElement` 以兼容 `tool` 消息。
- `cn.noodlecode.phone_agent.cli`:
    - `ChatCli`: 交互式命令行工具，用于测试模型的多轮对话能力，计划集成 Function Calling。
- `cn.noodlecode.phone_agent.config`:
    - `Env`: 环境变量管理，负责从资源文件加载 `model-config.json` 和 `prompts-zh`。
    - `Apps`: 维护 Android 常用应用的包名映射表。
- `cn.noodlecode.phone_agent.Util`: 提供通用的系统命令执行工具函数。

## 当前进展
- [x] 项目基础骨架搭建完成，配置了 Gradle 8.14 环境。
- [x] 实现 `AdbShell` 基础封装，支持执行 shell 命令。
- [x] 实现 `ModelConfig` 和 `Env` 配置加载逻辑，支持从 resources 读取配置。
- [x] 实现 `AndroidControl`，完成了点击、滑动、输入文本、截图等核心设备控制功能。
- [x] 实现 `ModelGateway` 和 `ApiClient`，支持与大模型进行流式或非流式对话。
- [x] 实现 `PhoneAgent` 核心逻辑，包括截图获取、多模态请求发送、Action 解析（`<answer>`）及执行循环。
- [x] 实现 `ChatCli`，支持基础的多轮对话交互。
- [x] 建立了初步的应用包名映射表（`ANDROID_APP_PACKAGES`）。

## 待办事项与后续计划
- [ ] **重构 `Message` 数据模型**: 将 `content` 字段类型从 `List<Content>` 修改为 `JsonElement`，以解决 OpenAI API 中 `role="tool"` 要求 `content` 为字符串，而 `user`/`assistant` 支持数组的类型冲突问题。
- [ ] **实现 OpenAI Function Calling**:
    - 在 `Message.kt` 中增加 `Tool`, `ToolCall` 等相关数据类。
    - 在 `ChatCli` 中实现工具定义、调用检测、执行及结果回传的完整闭环。
- [ ] **完善输入控制**: `Input.kt` 目前为空，考虑是否需要整合进 `AndroidControl` 或作为辅助类。
- [ ] **屏幕感知**: 优化截图功能，确保 Base64 编码效率。
- [ ] **意图解析与规划**: 优化 System Prompt，使模型能输出符合规范的操作指令（如 JSON 格式的操作序列）。
- [ ] **异常处理**: 增强对 ADB 连接断开、API 超时等异常情况的处理。
- [ ] **人工接管机制**: 设计在登录、验证码等敏感场景下的用户干预流程。

## 笔记
- `ModelResponse` 中内置了对 `<explain>` 和 `<answer>` 标签的解析，用于提取模型的思考过程和操作指令（主要用于 `PhoneAgent`）。
- 运行环境需确保 `adb` 已加入系统 `PATH`。
- **Function Calling 兼容性**: OpenAI API 规定 `role="tool"` 的消息 `content` 必须是字符串，而多模态消息通常是数组。为了在同一个 `Message` 类中兼容两者，建议使用 `kotlinx.serialization.json.JsonElement` 作为 `content` 的类型。
