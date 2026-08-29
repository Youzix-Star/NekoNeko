# 变更日志

本项目遵循[语义化版本控制](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- 项目基础结构
- GitHub Actions自动构建工作流
- 简单的欢迎界面
- Debug和Release版本支持
- 本地构建脚本
- 项目文档（README、贡献指南、APK下载指南）

### 变更
- 配色从粉/红/橙改为浅蓝色系（Material Light Blue）
- 主题升级为 Material Design 2（Theme.MaterialComponents）
- 悬浮窗改用 MaterialCardView + MaterialButton，MD2 风格化
- 按钮全部使用 Material 图标（关闭/复制/删除/无障碍/画中画）

### 修复
- 文本捕获不再依赖固定 view id，改为递归遍历节点树，兼容更多应用
- 捕获文本无变化时不再重复通知，避免刷屏
- 窗口内容变化事件加入节流，避免频繁全量捕获
- 常规无障碍事件降为 DEBUG 日志，不再刷屏日志区
- 修复缺失的 log_background drawable（此前会导致编译失败）
- 忽略 NekoNeko 自身窗口，避免捕获到悬浮窗内文本
- 修复悬浮窗不显示（Material 组件在 overlay 窗口的兼容问题，改用普通 View 方案）

## [1.5.0] - 2026-08-29

### 变更
- 配色整体调浅为"白蓝"系（主色 Light Blue 200，悬浮窗改白蓝底 + 深蓝文字）
- 默认模型改为 deepseek-v4-flash

### 新增
- AI 配置支持通过 API 获取模型列表（GET /models）
- AI 配置支持预设：保存 / 加载 / 删除自定义预设（SharedPreferences JSON 持久化）
- 内置"微软式翻译"预设（微软翻译腔风格提示词），与"微软式中文"一并提供
- 加载预设采用合并语义：预设非空字段覆盖当前值，空字段沿用当前值（避免覆盖 API Key）

## [1.4.0] - 2026-08-29

### 新增
- AI 配置功能：API 地址 / API Key / 模型 / 提示词（SharedPreferences 持久化）
- 悬浮窗新增"AI 修改"按钮：调用 AI 改写捕获的文本，并用结果替换输入框内容
- 默认提示词为"微软式中文"风格；支持 {text} 占位符自定义提示词
- 支持 OpenAI 兼容接口（默认 DeepSeek：api.deepseek.com / deepseek-chat）
- 主界面新增"AI 配置"入口；新增 auto_fix / settings Material 图标

## [1.3.0] - 2026-08-29

### 新增
- 悬浮窗新增"替换"按钮：将当前输入框内容整体替换为 test（ACTION_SET_TEXT）
- 悬浮窗新增"增加"按钮：在当前输入框文本末尾追加 test（ACTION_SET_TEXT）
- 新增 swap_horiz / add Material 图标

## [1.2.0] - 2026-08-29

### 变更
- 文本捕获改为纯手动模式：只有点击悬浮窗"捕获文本"按钮时才捕获
- 只捕获当前正在输入（有焦点/光标指示）的输入框文本，不再收集屏幕其他文本
- 移除自动捕获、事件监听与广播通知机制

## [1.1.0] - 2026-08-29

### 修复
- 修复悬浮窗不显示（MaterialCardView/MaterialButton 在 overlay 窗口的渲染兼容问题）
- 悬浮窗创建失败时弹出可见错误提示，不再静默失败

## [1.0.0] - 2024-01-01

### 新增
- 初始版本发布
- 基础Android应用框架
- GitHub Actions CI/CD配置
- 项目文档

### 技术栈
- Android SDK 33
- Java 11
- Gradle 7.5.1
- AndroidX AppCompat 1.6.1
- Material Design 1.9.0
- ConstraintLayout 2.1.4

### 项目结构
```
NekoNeko/
├── .github/workflows/    # GitHub Actions配置
├── app/                  # Android应用模块
├── docs/                 # 项目文档
├── build.gradle          # 项目构建配置
├── gradlew              # Gradle Wrapper
└── README.md            # 项目说明
```

### 构建特性
- 自动化CI/CD流水线
- 多环境构建支持（Debug/Release）
- 构建产物自动上传
- 标签触发发布版本创建

### 文档
- 详细的README说明
- APK下载和安装指南
- 贡献指南
- 变更日志

## 版本说明

### 版本号格式
- **主版本号**: 不兼容的API修改
- **次版本号**: 向下兼容的功能性新增
- **修订号**: 向下兼容的问题修正

### 发布周期
- 主版本: 按需发布
- 次版本: 每月发布
- 修订号: 按需发布

### 发布流程
1. 更新CHANGELOG.md
2. 更新版本号（build.gradle）
3. 创建Git标签
4. 推送到GitHub
5. GitHub Actions自动构建并发布
