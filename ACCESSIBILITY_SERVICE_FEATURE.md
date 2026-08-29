# 无障碍服务功能说明

## 功能概述

NekoNeko应用现在支持通过无障碍服务捕获屏幕文本，并在悬浮窗中显示。

## 主要特性

### 1. 无障碍服务
- **实时文本捕获**：自动捕获屏幕上的文本变化
- **手动捕获**：点击按钮手动获取当前窗口文本
- **多文本框支持**：支持多个可编辑文本框的内容捕获

### 2. 悬浮窗集成
- **文本显示**：在悬浮窗中显示捕获的文本
- **实时更新**：捕获的文本实时更新到悬浮窗
- **清晰界面**：Material Design 2风格的文本显示区域

### 3. 权限管理
- **无障碍服务权限**：引导用户启用无障碍服务
- **悬浮窗权限**：处理悬浮窗权限请求
- **用户引导**：清晰的权限说明和引导

## 技术实现

### 无障碍服务配置
```xml
<!-- accessibility_service_config.xml -->
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_service_description"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="true"
    android:settingsActivity="com.youzix.nekoneko.MainActivity" />
```

### 文本捕获实现
```java
// AccessibilityService.java
public String getCurrentWindowText() {
    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
    if (rootNode == null) return "";
    
    StringBuilder textBuilder = new StringBuilder();
    
    // 查找可编辑文本框
    List<AccessibilityNodeInfo> editTextNodes = 
        rootNode.findAccessibilityNodeInfosByViewId("android:id/edit");
    
    if (editTextNodes != null) {
        for (AccessibilityNodeInfo node : editTextNodes) {
            if (node.getText() != null) {
                textBuilder.append(node.getText()).append("\n");
            }
            node.recycle();
        }
    }
    
    rootNode.recycle();
    return textBuilder.toString().trim();
}
```

### 广播通信
```java
// 发送捕获的文本
Intent intent = new Intent("com.youzix.nekoneko.TEXT_CAPTURED");
intent.putExtra("captured_text", text);
sendBroadcast(intent);

// 接收捕获的文本
BroadcastReceiver textCapturedReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String capturedText = intent.getStringExtra("captured_text");
        updateCapturedText(capturedText);
    }
};
```

## 使用说明

### 启用无障碍服务
1. 打开NekoNeko应用
2. 点击"启用无障碍服务"按钮
3. 在系统设置中找到"NekoNeko"服务
4. 启用无障碍服务

### 启动悬浮窗
1. 点击"启动悬浮窗"按钮
2. 如果首次使用，系统会请求悬浮窗权限
3. 授权后悬浮窗将显示在屏幕上

### 捕获文本
1. **自动捕获**：切换应用或输入文本时自动捕获
2. **手动捕获**：点击悬浮窗中的"捕获文本"按钮
3. **查看结果**：捕获的文本会显示在悬浮窗中

## 文件结构

```
app/src/main/
├── java/com/youzix/nekoneko/
│   ├── MainActivity.java          # 主界面
│   ├── FloatingWindowService.java # 悬浮窗服务
│   └── AccessibilityService.java  # 无障碍服务
├── res/
│   ├── layout/
│   │   ├── activity_main.xml      # 主界面布局
│   │   └── floating_window.xml    # 悬浮窗布局
│   ├── drawable/
│   │   ├── floating_window_background.xml
│   │   ├── close_button_background.xml
│   │   ├── button_background.xml
│   │   └── text_background.xml
│   ├── xml/
│   │   └── accessibility_service_config.xml
│   └── values/
│       └── strings.xml            # 字符串资源
└── AndroidManifest.xml            # 应用配置
```

## 测试建议

### 功能测试
1. **无障碍服务测试**：验证服务能正常启用和捕获文本
2. **悬浮窗测试**：验证悬浮窗能正常显示和更新文本
3. **权限测试**：验证权限请求和处理流程

### 兼容性测试
1. **Android版本**：测试Android 7.0到最新版本
2. **不同应用**：测试在不同应用中的文本捕获
3. **不同输入框**：测试各种类型的文本输入框

### 性能测试
1. **响应速度**：测试文本捕获的响应速度
2. **内存使用**：监控无障碍服务的内存使用
3. **电池消耗**：测试对电池消耗的影响

## 已知限制

### 系统限制
- **Android 13+**：某些系统应用可能无法捕获文本
- **安全限制**：密码输入框可能无法捕获文本
- **厂商定制**：某些厂商可能有额外限制

### 技术限制
- **文本框类型**：主要支持可编辑文本框
- **实时性**：自动捕获可能有一定延迟
- **准确性**：某些特殊字符可能无法正确捕获

## 隐私说明

### 数据收集
- **仅本地处理**：所有文本仅在本地处理，不上传到服务器
- **无持久化存储**：捕获的文本不会持久化存储
- **用户控制**：用户可以随时禁用无障碍服务

### 权限用途
- **无障碍服务**：仅用于捕获屏幕文本
- **悬浮窗权限**：仅用于显示捕获的文本
- **无其他权限**：不请求其他不必要的权限

## 未来改进

### 功能增强
1. **更多文本源**：支持通知栏、菜单等文本捕获
2. **文本过滤**：添加文本过滤和搜索功能
3. **多语言支持**：优化多语言文本捕获

### 性能优化
1. **捕获算法**：优化文本捕获算法
2. **内存管理**：优化内存使用
3. **电池优化**：减少电池消耗

## 相关文档

- [Android无障碍服务文档](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
- [无障碍服务配置](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Material Design 2指南](https://material.io/design/color/the-color-system.html)

---

**功能完成时间**: 2024年1月1日
**开发者**: MiMo-v2.5
**状态**: ✅ 已完成
**测试状态**: 待测试
