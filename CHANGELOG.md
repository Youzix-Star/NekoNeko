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
- 无

### 修复
- 无

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
