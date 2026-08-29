# 变更日志

本项目遵循[语义化版本控制](https://semver.org/lang/zh-CN/)。

## [未发布]

### 变更（UI 全面重构，对齐 legado-with-MD3 的 MD3 设计语言）
- 主题升级为 DayNight（Theme.Material3.DayNight），补齐完整 MD3 颜色 token
  （surfaceContainer 全系列、fixed 系列、dim/bright、inverse），
  新增 values-night 深色 GR 色板，深色模式随系统自动切换
- 莫奈动态取色按系统深浅色叠加官方 Light/Dark overlay（新增 ThemeUtils）
- 输入框全部由 OutlinedBox（边框式）改为 FilledBox（填充式，无边框），
  API Key 支持明文切换（password_toggle）
- 首页/关于页去掉 MaterialCardView"卡片框"与分隔线，改为扁平 MD3 设置列表
  （分组标题 + 图标 Tonal 圆形底 + 标题/副标题/箭头行）
- 悬浮窗按钮统一为 MaterialButton 官方样式层级（Filled/Tonal/Outlined/Text），
  移除手动 backgroundTint 覆盖与分隔线
- 对话框改为 MaterialAlertDialogBuilder + 28dp 大圆角 + surfaceContainerLow 背景
- 引导页标题放大为 HeadlineMedium，隐私页图标与其他页统一（Tonal 圆形底）
- 删除废弃布局 activity_ai_config.xml / activity_splash.xml

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

## [1.15.0] - 2026-08-29

### 变更（UI 完全重构为阅读软件式 M3 设置列表）
- 首页改为设置列表式：大标题页头 + 圆角卡片 + 图标/标题/副标题/箭头行
  （无障碍服务 / 悬浮窗 / AI 配置），并显示实时状态（无障碍已启用、悬浮窗运行中）
- 悬浮窗行支持一键启动/停止（含权限引导）
- AI 配置页按分区卡片重构：服务配置 / 提示词 / 预设
- 关于页改为设置列表式信息卡片（功能特性 / 技术栈 / GitHub 仓库）
- 引导页第 4 步改为莫奈说明

### 变更（只保留莫奈取色）
- 移除全部手动主题色板（GR/Lemon/WH/Koharu/Sora/Elink）与选择器；
  Android 12+ 一律运行时应用官方 ThemeOverlay.Material3.DynamicColors（莫奈），
  Android 11 及以下回退默认配色

## [1.14.0] - 2026-08-29

### 变更（全面对齐 legado-with-MD3 的配色与主题架构）
- 默认主题改用参考仓库的 GR 色板（其默认配色），并搬入 Lemon / WH / Koharu / Sora / Elink
  五个完整 light 色板作为可选主题（色值直接取自其 ColorScheme 源码）
- 主题系统重构：移除 values-v31 静态动态主题，全部改为运行时控制——
  DEFAULT=GR（静态主题即 GR）、莫奈=Android 12+ 运行时叠加官方
  ThemeOverlay.Material3.DynamicColors（与 Compose dynamicLightColorScheme 同源）、
  其余=色板 overlay；Activity 与悬浮窗同步生效
- 主题选择器更新：默认(GR) / 莫奈 / 柠檬 / 白灰 / 小春 / 苍穹 / 墨水 七个选项
- 状态栏颜色与页面背景统一（colorSurface），API 23+ 使用浅色状态栏

## [1.13.0] - 2026-08-29

### 新增
- 首次启动引导流程（仿照 legado-with-MD3 的 WelcomeActivity 结构）：
  隐私与许可 → 无障碍服务 → 悬浮窗权限 → 主题色选择，带进度条
- 首页应用图标改为主题色圆形底；关于页新增当前主题色圆点预览

### 变更
- 主题系统对齐参考实现的 Monet 方案：Android 12+ 显式应用官方
  ThemeOverlay.Material3.DynamicColors（与 Compose dynamicLightColorScheme 同源的系统动态色板）

## [1.12.0] - 2026-08-29

### 修复
- 首页首次进入空白：修复 Fragment 切换的早退守卫 bug（currentTag 初始值与守卫冲突，
  导致首页 Fragment 从未被添加），并补全配置变更后的 Fragment 恢复
- 去除悬浮窗各区域多余的描边（floating window / 文本区 / 日志区），按需使用边框

### 变更
- 悬浮窗最小化/恢复改为缩放动画（顶部轴心，180ms）
- 首页"AI 配置"按钮由 Outlined 改为 Tonal（减少边框使用）
- 关于页新增"壁纸色"诊断（WallpaperColors 是否可用），判断莫奈不生效的原因

### 新增
- 手动主题色兜底：关于页提供 6 个主题色（莫奈/蓝/绿/紫/粉/橙），
  莫奈不可用时可手动选择，立即应用到整个应用与悬浮窗（跨 Android 版本可用）

## [1.11.0] - 2026-08-29

### 修复
- 许可证标注错误：仓库实际为 GNU AGPL v3（此前 README / 关于页 / CONTRIBUTING 误写为 MIT），已全部更正

### 变更
- 莫奈取色增加最可靠路径：MainActivity.onCreate 显式调用
  DynamicColors.applyToActivityIfAvailable（官方 API），与 values-v31 动态色主题双保险

### 优化
- 悬浮窗最小化/恢复增加淡入淡出动画；主要按钮统一最小高度

## [1.10.0] - 2026-08-29

### 修复
- 悬浮窗膨胀过程改用 ContextThemeWrapper 固定为基础 M3 主题（AppTheme.Overlay），
  Android 12+ 再手动叠加官方 ThemeOverlay.Material3.DynamicColors.Light，
  彻底规避动态主题在 Service overlay 窗口中的兼容问题
- 悬浮窗失败提示包含异常类与根因
- 关于页诊断增强：Android 版本 + 动态取色可用性（DynamicColors.isDynamicColorAvailable）
  + 当前主题主色，用于确认莫奈是否生效

### 其他
- 清理仓库根目录 27 份无用 AI 生成的修复/总结文档（保留 README、CHANGELOG、LICENSE、
  CONTRIBUTING、QUICK_START、APK_DOWNLOAD_GUIDE）

## [1.9.0] - 2026-08-29

### 变更
- 升级至 Material Design 3 Expressive：material 库 1.9.0 → 1.12.0（官方 expressive 组件/形状 token），
  compileSdk 34、AGP 8.3.2；卡片采用 28dp 大圆角 expressive 形状
- 莫奈取色按官方文档改用 Theme.Material3.DynamicColors.Light.NoActionBar（1.12.0 提供），
  悬浮窗手动应用官方公开样式 ThemeOverlay.Material3.DynamicColors.Light

### 修复
- 悬浮窗创建崩溃：overlay 窗口改用纯平台 View（LinearLayout/Button/ImageButton + shape 背景），
  彻底规避 Material 组件在 Service overlay 中的通货膨胀问题（该方案 v1.1-v1.6 期间从未失败）
- 悬浮窗创建失败提示现在包含根因信息，便于定位
- 关于页新增设备诊断：Android 版本 + 当前主题主色（用于验证莫奈是否生效）

## [1.8.0] - 2026-08-29

### 变更
- 重构为单 Activity + Fragment 结构，新增 Material 3 底部导航栏（首页 / AI 配置 / 关于）
- 新增"关于"页面：应用信息、版本号、功能特性、技术栈、GitHub 链接、许可证
- 莫奈取色改为官方方案：Android 12+ 直接使用 Theme.Material3.DynamicColors.Light
  （移除手动 DynamicColors 调用与自定义动态 overlay）
- 悬浮窗按 M3 规范重设计：24dp 圆角卡片、应用图标头像、分隔线、
  "捕获内容"标签、Filled/Tonal/Icon/Text 按钮层级

## [1.7.0] - 2026-08-29

### 变更
- 全面重构为 Material Design 3（官方 material 库）：
  - 主题改为 Theme.Material3.Light.NoActionBar，采用完整 M3 颜色角色
    （primary/secondary/tertiary、container 系列、surface 系列、outline）
  - 主界面重构：应用图标 + M3 排版标题，卡片式操作区，
    Filled / Tonal / Outlined 三级按钮
  - AI 配置页重构：OutlinedBox 输入框、TextButton、预设区 Tonal/Outlined 按钮
  - 悬浮窗重构：MaterialCardView 容器、Filled/Tonal/Icon/Text 按钮
- Android 12+ 莫奈动态色同步升级为 M3 颜色角色映射

## [1.6.0] - 2026-08-29

### 新增
- 支持莫奈（Material You）动态取色：Android 12+ 自动跟随壁纸配色，
  低版本回退浅蓝色系；悬浮窗同步应用动态色
- 悬浮窗新增"最小化"按钮：收起主体内容为紧凑标题栏，再点恢复

### 修复
- AI 提示词输入框改为固定高度 + 内部滚动，长文本可正常滚动编辑
- AI 配置界面 windowSoftInputMode=adjustResize，键盘弹出时页面可滚动到输入框

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
