# 如何获取APK文件

## 方法1：通过GitHub Actions自动构建（推荐）

### 步骤：
1. 访问GitHub仓库：https://github.com/Youzix-Star/NekoNeko
2. 点击"Actions"标签页
3. 查看最新的构建状态
4. 构建成功后，点击最新的构建记录
5. 在右侧"Artifacts"部分，下载以下文件：
   - `debug-apk`: Debug版本APK
   - `release-apk`: Release版本APK

### 自动触发构建：
- 每次推送到`main`或`master`分支时自动构建
- 每次创建Pull Request时自动构建

## 方法2：创建发布版本（Release）

### 步骤：
1. 在本地创建标签：
   ```bash
   git tag v1.0
   git push origin v1.0
   ```
2. GitHub Actions会自动创建Release
3. 访问仓库的"Releases"页面下载APK

## 方法3：本地构建（需要Android开发环境）

### 前提条件：
- 安装JDK 11或更高版本
- 安装Android SDK（通常通过Android Studio）
- 设置`ANDROID_HOME`环境变量

### 构建步骤：
1. 克隆仓库：
   ```bash
   git clone https://github.com/Youzix-Star/NekoNeko.git
   cd NekoNeko
   ```

2. 运行构建脚本：
   ```bash
   ./build-local.sh
   ```

3. 或者手动构建：
   ```bash
   chmod +x gradlew
   ./gradlew assembleDebug
   ```

4. APK文件位置：
   - Debug版本：`app/build/outputs/apk/debug/app-debug.apk`
   - Release版本：`app/build/outputs/apk/release/app-release.apk`

## APK文件说明

### Debug版本 vs Release版本：

| 特性 | Debug版本 | Release版本 |
|------|----------|------------|
| 签名 | 使用调试密钥 | 使用发布密钥 |
| 优化 | 未优化 | 已优化 |
| 调试信息 | 包含 | 不包含 |
| 用途 | 开发测试 | 正式发布 |

### 安装APK：
1. 将APK文件传输到Android设备
2. 在设备上启用"未知来源"安装
3. 点击APK文件进行安装

## 常见问题

### Q: 构建失败怎么办？
A: 检查Actions日志，通常是由于：
- Java版本不兼容
- Android SDK版本问题
- 网络连接问题（下载依赖）

### Q: 如何修改应用名称或图标？
A: 修改以下文件：
- 应用名称：`app/src/main/res/values/strings.xml`
- 图标：替换`app/src/main/res/mipmap-*/`中的图片文件

### Q: 如何添加更多功能？
A: 可以修改：
- `app/src/main/java/com/youzix/nekoneko/MainActivity.java` - 主界面
- `app/src/main/res/layout/activity_main.xml` - 布局文件
- `app/build.gradle` - 添加依赖库

## 技术支持

如有问题，请：
1. 查看GitHub Issues：https://github.com/Youzix-Star/NekoNeko/issues
2. 创建新的Issue描述问题
3. 等待维护者回复

## 更新日志

### v1.0 (当前版本)
- 基础应用框架
- GitHub Actions自动构建
- Debug和Release版本支持
- 本地构建脚本
