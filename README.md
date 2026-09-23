# 布布密码生成器 (Password Generator)

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language" />
  <img src="https://img.shields.io/badge/Min%20SDK-28-orange.svg" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-34-brightgreen.svg" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Build-GitHub%20Actions-blueviolet.svg" alt="CI/CD" />
</p>

一款轻量、安全、便捷的 Android 随机密码生成应用，支持灵活的密码规则组合、彩色字符高亮显示、历史记录管理与一键复制功能。

---

## 🌟 核心特性

- 🎚️ **密码长度自由调节**：支持通过滑块在 `4 ~ 50` 位之间快速调节密码长度（默认 15 位）。
- 🔠 **丰富的字符规则组合**：
  - 小写字母 (`a-z`)
  - 大写字母 (`A-Z`)
  - 数字 (`0-9`)
  - 特殊符号 (`!@-$()=[]`)
  - **智能生成算法**：选中的每个字符类别至少出现一次，生成后充分随机打乱，保证密码强度与合规性。
- 🎨 **彩色字符分类高亮**：
  - 数字采用**红色**标注
  - 字母采用**黑色**标注
  - 特殊符号采用**蓝色**标注
  - 搭配等宽字体呈现，便于直观核对各类字符。
- 📋 **一键剪贴板复制**：生成密码后一键复制到系统剪贴板，提供即时 Toast 提示。
- 🕒 **本地历史记录管理**：
  - 自动保留最近生成的 20 条密码，采用本地安全持久化（SharedPreferences）。
  - 历史记录列表支持点击任一项快速复制。

---

## 🛠️ 技术架构

- **开发语言**：Kotlin
- **架构范式**：Android 原生架构，响应式事件监听
- **UI 风格**：Material Design 卡片化设计（CardView, Button, RecyclerView, SeekBar）
- **核心能力**：
  - `SpannableString` + `ForegroundColorSpan` 字符着色与富文本呈现
  - `SharedPreferences` 历史密码本地持久化
  - `ClipboardManager` 系统级剪贴板集成
- **构建环境**：Gradle 8.11.1 + Android Gradle Plugin 8.10.0 (Kotlin DSL)
- **兼容范围**：
  - 最低支持：Android 9.0 (API 28)
  - 目标版本：Android 14 (API 34)

---

## 🚀 CI / CD 自动化流水线

项目已集成基于 **GitHub Actions** 的全自动化 Release 打包发布流水线（[.github/workflows/build-release.yml](.github/workflows/build-release.yml)）：

- **触发机制**：
  - Push 到 `main` 分支自动触发
  - 支持通过 GitHub Actions 控制台手动触发（`workflow_dispatch`）
- **流水线步骤**：
  1. **代码检出与环境就绪**：配置 Zulu JDK 21 与 Gradle Cache。
  2. **编译构建**：执行 `./gradlew :app:assembleRelease --no-daemon` 编译 Release APK。
  3. **自动化安全签名**：
     - 检测仓库 Secret 中的 `SIGNING_KEY`、`ALIAS`、`KEY_STORE_PASSWORD`、`KEY_PASSWORD`。
     - 自动调用 Android SDK Build-Tools 的 `zipalign` 对齐和 `apksigner` 签名及验证。
  4. **规范化动态重命名**：
     - 解析 `app/build.gradle.kts` 中的 `versionName`。
     - 重命名为 `passwordgen-v{version}-{date}-{commit}.apk`。
  5. **产物分发与即时推送**：
     - 自动上传至 GitHub Actions Artifacts。
     - 若配置了 Telegram 机器人相关 Secret（`TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`），构建产物将第一时间自动推送到指定 Telegram 群组或私聊。

- **依赖版本巡检流水线**（[.github/workflows/dependency-updates.yml](.github/workflows/dependency-updates.yml)）：
  - 集成 `gradle-versions-plugin` (Ben Manes)。
  - **触发机制**：纯手动触发（`workflow_dispatch`），在 GitHub Actions 控制台点击 **Run workflow** 即可启动。
  - **巡检流程**：自动执行 `./gradlew dependencyUpdates`，自动过滤掉非稳定预览版本（Alpha/Beta/RC），将版本更新报告发布至 Job Summary 并保存为构建工件 Artifact，支持将报告同步推送到 Telegram。

---

## 💻 本地构建与开发

### 环境要求
- [Android Studio](https://developer.android.com/studio) (推荐 2024.2+ 或更新版本)
- JDK 17 或 JDK 21
- Android SDK Platform 34

### 编译运行

1. 克隆代码仓库：
   ```bash
   git clone https://github.com/Marukon/passwordgen.git
   cd passwordgen
   ```

2. 导入项目：
   打开 Android Studio，选择 **Open** 并选中 `passwordgen` 根目录，等待 Gradle Sync 完成。

3. 命令行构建 Release 包：
   ```bash
   # Windows PowerShell
   .\gradlew.bat assembleRelease

   # Linux / macOS
   ./gradlew assembleRelease
   ```
   构建成功后 APK 产物位于：`app/build/outputs/apk/release/`

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。
