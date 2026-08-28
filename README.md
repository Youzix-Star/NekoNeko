# NekoNeko Android 应用

这是一个简单的Android应用，使用GitHub Actions进行自动构建。

## 功能特点

- 简单的欢迎界面
- 自动化APK构建
- 支持Debug和Release版本
- 自动上传构建产物

## 项目结构

```
NekoNeko/
├── .github/workflows/    # GitHub Actions工作流
├── app/                  # Android应用模块
│   ├── src/main/         # 主源代码
│   └── build.gradle      # 应用构建配置
├── build.gradle          # 项目构建配置
├── gradlew              # Gradle Wrapper脚本
└── settings.gradle      # 项目设置
```

## 如何使用

### 1. 克隆项目
```bash
git clone https://github.com/Youzix-Star/NekoNeko.git
cd NekoNeko
```

### 2. 本地构建
```bash
./gradlew assembleDebug    # 构建Debug版本
./gradlew assembleRelease  # 构建Release版本
```

### 3. 自动构建
每次推送到`main`或`master`分支时，GitHub Actions会自动：
1. 检出代码
2. 设置Java环境
3. 构建Debug和Release APK
4. 上传构建产物

### 4. 创建发布版本
当推送标签时（如`v1.0`），会自动创建GitHub Release并上传APK文件。

## 构建产物

构建完成后，APK文件会上传到：
- **Debug版本**: `app/build/outputs/apk/debug/`
- **Release版本**: `app/build/outputs/apk/release/`

## 开发说明

- 包名: `com.youzix.nekoneko`
- 最低SDK版本: 21 (Android 5.0)
- 目标SDK版本: 33 (Android 13)
- 使用AndroidX和Material Design组件

## 许可证

本项目使用MIT许可证，详见[LICENSE](LICENSE)文件。

## 联系方式

如有问题或建议，请通过GitHub Issues联系。
