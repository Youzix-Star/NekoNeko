# 悬浮窗功能说明

## 功能概述

NekoNeko应用现在支持悬浮窗功能，采用Material Design 2风格设计。

## 主要特性

### 1. 悬浮窗界面
- **Material Design 2风格**：使用粉色主题和圆角设计
- **可拖动**：用户可以拖动悬浮窗到屏幕任意位置
- **关闭按钮**：红色圆形关闭按钮，点击可关闭悬浮窗
- **操作按钮**：白色背景的交互按钮

### 2. 权限处理
- **Android 6.0+支持**：自动请求悬浮窗权限
- **权限检查**：启动前检查权限状态
- **用户引导**：权限被拒绝时显示提示

### 3. 服务架构
- **后台服务**：使用Android Service实现
- **独立生命周期**：悬浮窗服务独立于主Activity
- **资源管理**：正确管理窗口资源，避免内存泄漏

## 技术实现

### 权限配置
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<service
    android:name=".FloatingWindowService"
    android:enabled="true"
    android:exported="false" />
```

### 悬浮窗参数
```java
WindowManager.LayoutParams params = new WindowManager.LayoutParams(
    WindowManager.LayoutParams.WRAP_CONTENT,
    WindowManager.LayoutParams.WRAP_CONTENT,
    layoutFlag,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
    PixelFormat.TRANSLUCENT
);
```

### 触摸事件处理
- **ACTION_DOWN**：记录初始位置
- **ACTION_MOVE**：更新悬浮窗位置
- **拖动平滑**：使用相对位移计算

## 使用说明

### 启动悬浮窗
1. 打开NekoNeko应用
2. 点击"启动悬浮窗"按钮
3. 如果是首次使用，系统会请求悬浮窗权限
4. 授权后悬浮窗将显示在屏幕上

### 操作悬浮窗
- **拖动**：按住悬浮窗任意位置拖动
- **关闭**：点击右上角的×按钮
- **交互**：点击"点击我"按钮

### 权限管理
- **Android 6.0以下**：自动获得权限
- **Android 6.0+**：需要用户手动授权
- **权限设置**：可在系统设置中管理

## 设计规范

### Material Design 2元素
- **颜色**：使用Material Design粉色主题
- **圆角**：12dp圆角设计
- **阴影**：4dp阴影效果
- **间距**：16dp内边距

### 颜色方案
- **主色调**：#E91E63（粉色）
- **主色调变体**：#C2185B
- **关闭按钮**：#F44336（红色）
- **文本颜色**：白色
- **按钮背景**：白色

## 文件结构

```
app/src/main/
├── java/com/youzix/nekoneko/
│   ├── MainActivity.java          # 主界面
│   └── FloatingWindowService.java # 悬浮窗服务
├── res/
│   ├── layout/
│   │   ├── activity_main.xml      # 主界面布局
│   │   └── floating_window.xml    # 悬浮窗布局
│   ├── drawable/
│   │   ├── floating_window_background.xml
│   │   ├── close_button_background.xml
│   │   └── button_background.xml
│   └── values/
│       └── strings.xml            # 字符串资源
└── AndroidManifest.xml            # 应用配置
```

## 测试建议

### 功能测试
1. **启动测试**：验证悬浮窗能正常启动
2. **拖动测试**：验证悬浮窗可以拖动
3. **关闭测试**：验证关闭按钮能正常工作
4. **权限测试**：验证权限请求流程

### 兼容性测试
1. **Android版本**：测试Android 5.0到最新版本
2. **屏幕尺寸**：测试不同屏幕尺寸
3. **分辨率**：测试不同分辨率设备

### 性能测试
1. **内存使用**：监控悬浮窗服务的内存使用
2. **电池消耗**：测试悬浮窗对电池的影响
3. **响应速度**：测试拖动和点击的响应速度

## 已知问题

### 权限问题
- **Android 11+**：需要在应用设置中手动开启悬浮窗权限
- **厂商定制**：某些厂商可能需要额外的权限设置

### 兼容性问题
- **旧版Android**：某些旧版Android可能不支持某些Material Design特性
- **自定义ROM**：某些自定义ROM可能有兼容性问题

## 未来改进

### 功能增强
1. **多悬浮窗**：支持多个悬浮窗同时显示
2. **自定义主题**：允许用户自定义悬浮窗主题
3. **快捷操作**：添加更多快捷操作按钮

### 性能优化
1. **动画优化**：优化拖动动画性能
2. **内存优化**：减少内存使用
3. **电池优化**：减少电池消耗

## 相关文档

- [Android悬浮窗文档](https://developer.android.com/guide/topics/resources/drawable-vector)
- [Material Design 2指南](https://material.io/design/color/the-color-system.html)
- [Android Service文档](https://developer.android.com/guide/components/services)

---

**功能完成时间**: 2024年1月1日
**开发者**: MiMo-v2.5
**状态**: ✅ 已完成
**测试状态**: 待测试
