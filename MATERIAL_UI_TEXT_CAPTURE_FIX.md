# 浅蓝配色 + MD2 界面 + 文本捕获修复说明

## 概述

本次改动围绕三个需求：
1. 配色改为浅蓝色（原配色为粉/红/橙，过于花哨）
2. 修复文本捕获问题与 Material Design 2（MD2）设计问题
3. 使用 Material 图标

## 1. 浅蓝色配色

### 修改文件
- `app/src/main/res/values/colors.xml` — 全面替换为 Material Light Blue 色板
- `app/src/main/res/drawable/ic_launcher.xml` — 启动图标改浅蓝
- `app/src/main/res/drawable/floating_window_background.xml` — 悬浮窗背景改浅蓝
- `app/src/main/res/drawable/close_button_background.xml` — 关闭按钮改半透明白
- `app/src/main/res/drawable/text_background.xml` — 文本区改半透明白
- `app/src/main/res/layout/activity_splash.xml` — 背景改用深一档的浅蓝保证白字可读

### 新色板
```xml
colorPrimary          #4FC3F7  (Light Blue 300)
colorPrimaryVariant   #0288D1  (Light Blue 700)
colorOnPrimary        #003C58  (深蓝文字，保证对比度)
colorSecondary        #81D4FA  (Light Blue 200)
colorSecondaryVariant #0288D1
colorOnSecondary      #003C58
windowBackground      #E1F5FE  (Light Blue 50)
floatingWindowBackground #0288D1
logText               #B3E5FC
```

## 2. 文本捕获修复（AccessibilityService.java）

### 修复前的问题
- 只按固定 view id（`android:id/edit`、`android:id/text1`）查找文本，
  大多数应用没有这些 id，导致"未找到可捕获的文本"
- 每次 `TYPE_WINDOW_CONTENT_CHANGED` 都全量捕获并以 INFO 级写日志，
  日志区被事件刷屏（50 条上限内全是事件记录）
- 相同文本反复通知，悬浮窗内容被同一段文字刷屏

### 修复内容
1. **递归遍历节点树**：收集所有含文本的节点（保留顺序、去重），不再依赖 view id
2. **节流**：`TYPE_WINDOW_CONTENT_CHANGED` 600ms 内只捕获一次；
   窗口切换、文本变化仍即时捕获
3. **去重通知**：捕获文本与上次相同时不重复通知
4. **日志降噪**：常规事件降为 DEBUG 级别，不再刷屏
5. **忽略自身窗口**：跳过 NekoNeko 包名的窗口，避免捕获到悬浮窗内的文本
6. **安全上限**：节点数 600、深度 24、文本 2000 字符，防止卡顿
7. 事件类型改为 `WINDOW_STATE_CHANGED | VIEW_TEXT_CHANGED | WINDOW_CONTENT_CHANGED`，
   并增加 `FLAG_INCLUDE_NOT_IMPORTANT_VIEWS`

## 3. MD2 设计修复

### 修复前的问题
- 主题为 `Theme.AppCompat.Light.DarkActionBar`（非 Material 主题）
- 悬浮窗是裸 LinearLayout + 形状背景，按钮为原生 Button，无 MD2 风格
- 颜色大量硬编码在布局里
- **`floating_window.xml` 引用了不存在的 `@drawable/log_background`（编译必失败）**

### 修复内容
- `styles.xml`：主题改为 `Theme.MaterialComponents.Light.NoActionBar`（MD2）
- `floating_window.xml`：
  - 根布局改为 `MaterialCardView`（圆角 16dp、高度 8dp、卡片背景色）
  - 捕获按钮、清除按钮改用 `MaterialButton`（自带主题色与涟漪）
  - 关闭按钮改用 `ImageButton` + Material 关闭图标
  - 新增 `log_background.xml`，修复编译错误
- `activity_main.xml`：
  - 按钮改用 `MaterialButton` 并带图标
  - 背景改用 `windowBackground`，标题/说明改用颜色资源
  - 移除硬编码 `#FF5722` 等颜色

## 4. Material 图标

新增 5 个矢量图标（material-icons 官方路径，Apache 2.0）：
- `ic_close.xml` — 关闭悬浮窗
- `ic_content_copy.xml` — 捕获文本
- `ic_delete.xml` — 清除日志
- `ic_accessibility.xml` — 启用无障碍服务
- `ic_picture_in_picture.xml` — 启动悬浮窗

## 修改文件清单

```
app/src/main/java/com/youzix/nekoneko/AccessibilityService.java   (重写)
app/src/main/java/com/youzix/nekoneko/FloatingWindowService.java  (关闭按钮改 ImageButton)
app/src/main/res/values/colors.xml / styles.xml / strings.xml
app/src/main/res/layout/activity_main.xml / floating_window.xml / activity_splash.xml
app/src/main/res/drawable/ic_close.xml / ic_content_copy.xml / ic_delete.xml
app/src/main/res/drawable/ic_accessibility.xml / ic_picture_in_picture.xml
app/src/main/res/drawable/log_background.xml (新增)
app/src/main/res/drawable/close_button_background.xml / floating_window_background.xml
app/src/main/res/drawable/text_background.xml / ic_launcher.xml
CHANGELOG.md
```

## 验证方式

- 本地无法编译（无 Android SDK），已做 XML 良构、资源引用完整性校验
- 建议推送后在 GitHub Actions 验证 `assembleDebug` / `assembleRelease`
- 真机验证：启用无障碍服务 → 打开任意应用输入文字 → 悬浮窗应自动显示捕获文本；
  点击"捕获文本"手动抓取当前窗口文本

---

**修复时间**: 2025-08-29
**修复状态**: ✅ 已完成（待 CI 验证）
