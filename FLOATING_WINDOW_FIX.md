# 悬浮窗背景修复说明

## 问题描述

GitHub Actions构建失败，错误信息：
```
ERROR: /home/runner/work/NekoNeko/NekoNeko/app/src/main/res/drawable/floating_window_background.xml:13: AAPT: error: attribute android:dx not found.
ERROR: /home/runner/work/NekoNeko/NekoNeko/app/src/main/res/drawable/floating_window_background.xml:13: AAPT: error: attribute android:dy not found.
ERROR: /home/runner/work/NekoNeko/NekoNeko/app/src/main/res/drawable/floating_window_background.xml:13: AAPT: error: '4' is incompatible with attribute radius (attr) dimension.
```

## 问题原因

在`floating_window_background.xml`中使用了`<shadow>`标签，但Android的`<shape>` drawable不支持阴影属性。

## 解决方案

### 1. 移除不支持的`<shadow>`标签
从`floating_window_background.xml`中移除了`<shadow>`子标签。

### 2. 使用`elevation`属性实现阴影
在`floating_window.xml`布局中添加了`android:elevation="4dp"`属性。

## 修复内容

### 修改的文件
1. `app/src/main/res/drawable/floating_window_background.xml`
   - 移除了`<shadow>`标签

2. `app/src/main/res/layout/floating_window.xml`
   - 添加了`android:elevation="4dp"`属性

### 配置变化
```xml
<!-- floating_window_background.xml -->
<!-- 修复前 -->
<shape ...>
    <solid android:color="#E91E63" />
    <corners android:radius="12dp" />
    <stroke ... />
    <shadow
        android:color="#40000000"
        android:dx="2"
        android:dy="2"
        android:radius="4" />
</shape>

<!-- 修复后 -->
<shape ...>
    <solid android:color="#E91E63" />
    <corners android:radius="12dp" />
    <stroke ... />
</shape>
```

```xml
<!-- floating_window.xml -->
<!-- 添加的属性 -->
android:elevation="4dp"
```

## 验证修复

修复后，GitHub Actions应该能够正常构建：
1. 推送代码到main分支
2. 检查GitHub Actions标签页
3. 验证构建状态为绿色（成功）
4. 检查构建日志中没有drawable错误

## 技术说明

### Android Drawable阴影限制
- `<shape>` drawable不支持阴影属性
- 阴影需要通过`elevation`属性或代码实现
- `elevation`属性需要Android 5.0+支持

### 阴影实现方法
1. **XML属性**：使用`android:elevation`属性（推荐）
2. **代码实现**：使用`setElevation()`方法
3. **自定义View**：重写`onDraw()`方法绘制阴影

## 相关链接

- [Android Drawable文档](https://developer.android.com/guide/topics/resources/drawable-resource)
- [Material Design阴影](https://material.io/design/environment/elevation.html)
- [View.elevation属性](https://developer.android.com/reference/android/view/View#setElevation(float))

---

**修复时间**: 2024年1月1日
**修复者**: MiMo-v2.5
**影响范围**: 悬浮窗背景drawable
**修复状态**: ✅ 已完成
