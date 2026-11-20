
<div align="center">

# 骂完就删 · Say & Gone

> 一个只负责让情绪消失的极简本地 Android 应用 —— 写下、释放、看着它消散，然后干净地结束。

**关键词**：`情绪宣泄` `本地优先` `不保存·不上传·不分析` `仪式感` `MVVM` `Canvas 动画`

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-7F52FF?logo=kotlin) ![Android](https://img.shields.io/badge/API-26%2B-3DDC84?logo=android) ![Gradle](https://img.shields.io/badge/Gradle-8.2-02303A?logo=gradle) ![UI](https://img.shields.io/badge/UI-View%20%2B%20Canvas-orange)

作者：[linkpain](https://github.com/linkpain) · 仓库：[linkpain/Say-Gone](https://github.com/linkpain/Say-Gone)

</div>

> **骂完就删**（工程名 `SayAndGone`，包名 `com.sayandgone`）把「骂两句」从一次性输入升级为一次完整的情绪仪式。它不教育、不分析、不评价，也不保存任何数据：所有文字只存在于内存中，随着动画结束而彻底消失。它的目标不是让你发泄得更多，而是让一次发泄真正结束。

---

## 📖 目录

- [🎯 项目简介](#-项目简介)
- [✨ 核心特性](#-核心特性)
- [🚀 快速开始](#-快速开始)
- [📋 构建环境](#-构建环境)
- [📁 项目结构](#-项目结构)
- [🏗 架构概览](#-架构概览)
- [🎨 交互设计](#-交互设计)
- [📄 许可](#-许可)
- [🙏 致谢](#-致谢)

---

## 🎯 项目简介

《骂两句》是一款面向高压年轻人的**本地情绪宣泄工具**。根据仓库中的三份 PRD，产品的核心原则是：

- **不记录、不分析、不传播**：无网络、无数据库、无文件、无日志，内容仅存在于当前页面与内存变量中；
- **不指向具体的人**：情绪对象是抽象存在（一种情绪、一件事情、一个念头），文案提示「给这股情绪起个名字（可选）」；
- **不强化攻击叙事**：统一叙事方向是「向外释放 / 排出 / 清空」，而非针对某个对象的攻击；
- **仪式感优先**：V2 的核心关键词是「慢一点、确定一点、干净一点」，所有动画只为让用户相信「事情真的结束了」。

应用从 V1 的单页「骂完即删」，演进到 V2 的销毁仪式，再到当前实现中「点击式情绪释放」的完整闭环：引导页 → 情绪具象化与书写 → 连续点击释放 → 「爽」字坠落碰撞 → 情绪体粒子消散 → 撤销窗口 → 结束语收尾。

---

## ✨ 核心特性

| 环节 | 实现要点 | 涉及类 / 资源 |
| --- | --- | --- |
| 🟢 引导页 | 首次启动全屏无交互引导，文案「你写下的任何内容 / 都不会被保存」分两阶段淡入淡出（0ms / 1000ms / 3000ms），约 3.5 秒后自动进入主页；通过 `SharedPreferences`（`sayandgone_prefs` / `onboarding_shown`）保证只出现一次 | `OnboardingActivity.kt`、`activity_onboarding.xml` |
| 🔵 情绪选择 | 「给这股情绪起个名字（可选）」输入后，情绪名会实时绘制进火柴人情绪体的头部；释放强度随点击数分 5 级变色：灰 `#6B7280` → 金 `#FFD700` → 蓝 `#3B82F6` → 橙 `#F59E0B` → 红 `#EF4444` | `EmotionView.kt`、`MainViewModel.ReleaseIntensity` |
| 🔴 攻击（文字弹幕） | 点击屏幕时，从屏幕边缘随机位置向中心发射筹字：`AttackView` 以 `AccelerateInterpolator` 投射用户输入（按换行随机取一行）及从 9 条 `profanityLibrary` 中随机的短语，命中后触发撞击动画 | `AttackView.kt`、`MainActivity.profanityLibrary` |
| 🟡 满足 | 点击「完成释放」后，巨型「爽」字从顶部坠落（800ms），碰撞情绪体时触发 **300ms 震动**（`VIBRATE` 权限）、屏幕抖动、火花粒子，并驱动情绪体进入消散 | `SatisfactionView.kt`、`AndroidManifest.xml` |
| 🟣 结束语 | 销毁后按**输入长度**（>150 字走 `longText`）与**时间段**（`Calendar.HOUR_OF_DAY in 8..20` 判为白天）从 `EndingPhrases` 四个文案池随机取一条；淡显 1 秒后主按钮变为「关闭」 | `EndingPhrases.kt`、`MainActivity.finalizeDestruction()` |

补充特性：输入时输入框「呼吸」缩放（1→1.02→1，2 秒循环）与背景亮度 ±5% 缓慢起伏，停止输入 1 秒后回归静态；连击阈值触发气泡嘲弄文案（如「就这就这」「我是不会被打败的」「要爆炸啦」），累计 201 次点击自动结束释放。

---

## 🚀 快速开始

```bash
# 1. 克隆仓库
git clone https://github.com/linkpain/Say-Gone.git
cd Say-Gone

# 2. 用 Android Studio（Hedgehog 2023.1.1 或更高）打开根目录，等待 Gradle 同步

# 3. 连接真机或启动 API 26+ 模拟器，点击 Run，或命令行构建 Debug 包
./gradlew assembleDebug

# 4. 安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

> 说明：首次启动会播放一次性引导动画；发布包使用仓库内置的 `app/release.keystore` 签名配置。安装后请输入任意文字体验完整流程，退出 App 后数据即不存在。

---

## 📋 构建环境

| 项目 | 版本 / 配置 |
| --- | --- |
| 构建插件 | Android Gradle Plugin `8.2.0`，Kotlin Android Plugin `1.9.20` |
| Gradle Wrapper | `gradle-8.2-bin.zip` |
| JDK | Java 17（`sourceCompatibility` / `targetCompatibility` / `jvmTarget` 均为 `17`） |
| SDK | `compileSdk = 34`，`minSdk = 26`，`targetSdk = 34` |
| 应用标识 | `applicationId` / `namespace` = `com.sayandgone`，`versionCode = 1`，`versionName = "1.0"` |
| UI 技术 | **未使用 Jetpack Compose**：采用传统 View 体系 + XML 布局（`activity_main.xml`、`activity_onboarding.xml`）+ 自定义 Canvas 视图（`EmotionView`、`AttackView`、`SatisfactionView`）；启用 `viewBinding = true` |
| 主要依赖 | `core-ktx 1.12.0`、`appcompat 1.6.1`、`material 1.11.0`、`constraintlayout 2.1.4`、`lifecycle-viewmodel-ktx 2.7.0`、`lifecycle-runtime-ktx 2.7.0` |
| 其他 | `android.useAndroidX=true`、`android.enableJetifier=true`；Release 开启 `isMinifyEnabled` 并配置签名 |

---

## 📁 项目结构

```text
Say-Gone/
├── app/
│   ├── build.gradle.kts                 # 模块级构建配置（SDK / 依赖 / 签名）
│   ├── release.keystore                 # 发布签名 keystore
│   └── src/main/
│       ├── AndroidManifest.xml          # VIBRATE 权限；引导页为 Launcher
│       ├── java/com/sayandgone/
│       │   ├── MainActivity.kt          # 主流程：输入 → 释放 → 消散 → 结束语
│       │   ├── OnboardingActivity.kt    # 首次启动引导
│       │   ├── data/EndingPhrases.kt    # 结束语文案池与选取规则
│       │   ├── view/
│       │   │   ├── EmotionView.kt       # 情绪体绘制、脉动、800 粒子消散
│       │   │   ├── AttackView.kt        # 文字弹幕投射与命中动画
│       │   │   └── SatisfactionView.kt  # 「爽」字坠落、震动、碰撞反馈
│       │   └── viewmodel/MainViewModel.kt # 内存状态与释放强度分级
│       └── res/
│           ├── layout/                  # 主界面 / 引导页布局
│           ├── values/                  # strings / colors / themes
│           └── drawable|mipmap-anydpi-v26/ # 图标与气泡背景
├── build.gradle.kts / settings.gradle.kts / gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── 《骂两句》prd_v_2_｜仪式感强化版.md
├── 《骂两句》prd_与实现说明.md
└── 情绪释放交互版设计总结.md
```

---

## 🏗 架构概览

项目采用轻量级 **MVVM**：

- **Model / 数据层**：`EndingPhrases` 是规则型文案数据源；`MainViewModel` 内部以 `MutableStateFlow` 持有 `inputText`、`emotionName`、`isReleasing`、`clickCount`、`releaseIntensity`、`isDestroyed`、`canUndo`、`endingPhrase` 等状态。
- **View / 界面层**：`MainActivity` 通过 `ViewModelProvider` 获取 ViewModel，用 `lifecycleScope` 收集状态流驱动 UI；三个自定义 `View` 专注动画表现，通过回调（`onClickListener`、`onCollisionListener`、`onScreenShakeListener`）与 Activity 通信。
- **状态流转**：`saveText()` 保存现场 → `startReleasing()` 进入释放（点击数清零、强度 LEVEL_1）→ `incrementClick()` 累加点击并按阈值（≤20/≤50/≤100/≤150/>150）升级强度 → `destroy()` 标记可撤销 → `undo()` 恢复现场或 `finalize()` 清空现场并选定结束语。

所有数据均为**内存级**：无持久化、无网络请求，契合 PRD「App 关闭即清空」的隐私承诺。

---

## 🎨 交互设计

完整流程（融合三份 PRD 与当前实现）：

1. **建立信任**：首次启动的引导页明确声明「你写下的任何内容都不会被保存」。
2. **情绪具象化（可选）**：为情绪命名，名字被绘制进中央火柴人情绪体的头部，帮助心理聚焦而非制造对立。
3. **情绪书写**：多行输入框占屏约 70%，提示「想骂就骂，这里不会记住任何东西 / 内容只存在于当前页面」；输入时伴随呼吸与背景微光。
4. **开始释放**：点击「开始释放」进入释放态，主按钮隐藏，露出「点得越快，释放得越充分」与点击计数；连续点击屏幕触发文字弹幕、情绪体脉动与阶段性气泡文案。
5. **完成释放**：点击「完成释放」（或点击数 ≥201 自动触发），「爽」字坠落撞击情绪体，伴随震动与屏幕抖动。
6. **消散与余波**：情绪体炸裂为 800 个粒子并二次裂解（2.5 秒）；页面淡出后显示「已经消失了」。
7. **防误触撤销**：销毁后进入撤销窗口（**代码实现为 2 秒 CountDownTimer**，PRD V2 建议 10 秒），提示「如果你还没准备好，它还在」；期间点击主按钮可撤销恢复。
8. **结束语收尾**：按输入长度与时段选句，淡显后主按钮变为「关闭」，点击退出，完成一次「写出来 → 动起来 → 看着它消失 → 确认结束」的闭环。

> 仪式感设计要点：所有动画都服务于「让用户相信内容已被清除」——呼吸与背景微光增强投入感，粒子消散提供视觉确认，震动与抖动强化「命中/完成」的体感，而规则型结束语（仅基于长度与时间，不做语义分析）给出温和的心理句号。

---

## 📄 许可

仓库当前未包含 `LICENSE` 文件。源代码版权归作者 [linkpain](https://github.com/linkpain) 所有，具体使用与再分发条款请以仓库实际声明为准；在未获得明确授权前，请仅作学习与参考用途。

---

## 🙏 致谢

- 感谢 [linkpain](https://github.com/linkpain) 开源本项目，并在三份 PRD 中清晰沉淀了「不记录、不分析、不传播」的产品原则与「仪式感强化」的设计思考。
- 感谢 Android / Kotlin 开源生态提供的 AppCompat、Material Components 与 Lifecycle 等基础组件。
- 感谢所有在高压生活中寻找一个安全出口的人——愿每一次情绪，都能被好好地结束。
