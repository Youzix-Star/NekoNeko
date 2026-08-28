# 快速开始指南

## 获取APK文件

### 方法1：从GitHub Actions下载（推荐）

1. 访问仓库：https://github.com/Youzix-Star/NekoNeko
2. 点击"Actions"标签页
3. 找到最新的绿色构建记录，点击进入
4. 在右侧"Artifacts"部分，点击下载：
   - `debug-apk` - Debug版本
   - `release-apk` - Release版本

### 方法2：从Release页面下载

1. 访问：https://github.com/Youzix-Star/NekoNeko/releases
2. 找到最新版本
3. 在"Assets"部分下载APK文件

## 安装APK

### 在Android设备上安装

1. 将APK文件传输到手机（USB、蓝牙、邮件等）
2. 在手机上打开文件管理器
3. 点击APK文件
4. 如果提示"未知来源"，请在设置中允许安装
5. 点击"安装"按钮

### 启用未知来源安装

不同Android版本设置方法不同：

**Android 8.0及以上**:
1. 打开"设置" > "应用和通知"
2. 点击"高级" > "特殊应用访问"
3. 选择"安装未知应用"
4. 选择要允许安装的应用（如文件管理器）
5. 启用"允许来自此来源"

**Android 7.0及以下**:
1. 打开"设置" > "安全"
2. 启用"未知来源"

## 本地开发

### 前提条件

1. 安装JDK 11或更高版本
2. 安装Android Studio（推荐）
3. 安装Git

### 克隆并运行

```bash
# 克隆仓库
git clone https://github.com/Youzix-Star/NekoNeko.git
cd NekoNeko

# 使用构建脚本
./build-local.sh

# 或者手动构建
chmod +x gradlew
./gradlew assembleDebug
```

### 在Android Studio中打开

1. 打开Android Studio
2. 选择"Open an Existing Project"
3. 选择NekoNeko文件夹
4. 等待Gradle同步完成
5. 点击运行按钮

## 常见问题

### Q: 下载的APK无法安装？
A: 请确保：
1. 已启用"未知来源"安装
2. 设备Android版本在5.0以上
3. APK文件完整（未损坏）

### Q: GitHub Actions构建失败？
A: 检查：
1. 网络连接是否正常
2. 查看Actions日志了解具体错误
3. 确保代码没有语法错误

### Q: 如何修改应用名称？
A: 编辑 `app/src/main/res/values/strings.xml` 文件中的 `app_name` 字段。

### Q: 如何修改应用图标？
A: 替换 `app/src/main/res/mipmap-*/` 目录中的图片文件。

## 下一步

查看详细文档：
- [README.md](README.md) - 项目概述
- [APK下载指南](APK_DOWNLOAD_GUIDE.md) - 详细下载说明
- [贡献指南](CONTRIBUTING.md) - 如何参与开发
- [项目总结](PROJECT_SUMMARY.md) - 项目详细信息

## 技术支持

如有问题，请：
1. 查看GitHub Issues: https://github.com/Youzix-Star/NekoNeko/issues
2. 创建新的Issue描述问题
3. 等待维护者回复
