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
- `cn.noodlecode.phone_agent.device.adb`: 
    - `AdbShell`: 封装了 ADB 的基础操作，支持多设备管理（通过 `deviceId`）。
    - `Input`: (待实现) 计划封装点击、滑动、文本输入等模拟操作。
- `cn.noodlecode.phone_agent.model`: 
    - `ApiClient`: 基于 Ktor 的异步 API 客户端，支持 SSE 流式响应。
    - `Message`: 定义了多模态消息结构和模型配置模型。
    - `ChatCli`: 交互式命令行工具，用于测试多轮对话和模型响应。
- `cn.noodlecode.phone_agent.config`: 
    - `Env`: 环境变量管理，负责从资源文件加载 `model-config.json` 和 `prompts-zh`。
    - `Apps`: 维护 Android 常用应用的包名映射表。
- `cn.noodlecode.phone_agent.Util`: 提供通用的系统命令执行工具函数。

## 当前进展
- [x] 项目基础骨架搭建完成，配置了 Gradle 8.14 环境。
- [x] 实现 `AdbShell` 基础封装，支持执行 shell 命令。
- [x] 实现 `ModelConfig` 和 `Env` 配置加载逻辑，支持从 resources 读取配置。
- [x] 实现 `ApiClient` 和 `ChatCli`，支持与大模型进行流式或非流式对话。
- [x] 建立了初步的应用包名映射表（`ANDROID_APP_PACKAGES`）。

## 待办事项与后续计划
- [ ] **完善输入控制**: 在 `Input.kt` 中实现具体的 `tap`, `swipe`, `keyevent` 等 ADB 操作。
- [ ] **屏幕感知**: 实现截图功能，并将其转换为 Base64 编码发送给 VLM。
- [ ] **意图解析与规划**: 优化 System Prompt，使模型能输出符合规范的操作指令（如 JSON 格式的操作序列）。
- [ ] **异常处理**: 增强对 ADB 连接断开、API 超时等异常情况的处理。
- [ ] **人工接管机制**: 设计在登录、验证码等敏感场景下的用户干预流程。

## 笔记
- `ModelResponse` 中内置了对 `<explain>` 标签的解析，用于提取模型的思考过程（Thinking Process）。
- 运行环境需确保 `adb` 已加入系统 `PATH`。
