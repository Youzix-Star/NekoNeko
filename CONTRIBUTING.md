# 贡献指南

感谢您对NekoNeko项目的关注！我们欢迎任何形式的贡献。

## 如何贡献

### 1. 报告问题
如果您发现bug或有功能建议，请：
1. 检查现有的GitHub Issues，避免重复
2. 创建新的Issue，包含详细描述
3. 提供重现步骤（如果是bug）
4. 添加相关标签（bug、enhancement等）

### 2. 提交代码
1. Fork项目仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 进行修改并测试
4. 提交更改：`git commit -m "Add some feature"`
5. 推送到您的Fork：`git push origin feature/your-feature`
6. 创建Pull Request

### 3. 代码规范
- 遵循Android开发最佳实践
- 使用有意义的变量和方法名
- 添加必要的注释
- 保持代码简洁易读

### 4. 测试要求
- 确保代码能在本地构建
- 验证GitHub Actions构建通过
- 测试在不同Android版本上的兼容性

## 开发环境设置

### 前提条件
- JDK 11或更高版本
- Android Studio（推荐）
- Git

### 步骤
1. 克隆项目：
   ```bash
   git clone https://github.com/Your-username/NekoNeko.git
   cd NekoNeko
   ```

2. 在Android Studio中打开项目

3. 等待Gradle同步完成

4. 运行应用进行测试

## 提交规范

### Commit消息格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 类型（type）
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构代码
- `test`: 添加测试
- `chore`: 构建过程或辅助工具的变动

### 示例
```
feat(ui): 添加启动画面

- 添加启动画面布局文件
- 更新strings.xml资源
- 添加相关字符串资源

Closes #12
```

## 功能建议

如果您有功能建议，请：
1. 在Issues中创建新Issue
2. 详细描述功能需求
3. 说明使用场景
4. 提供设计草图（如果适用）

## 文档贡献

我们欢迎文档改进，包括：
- 修复拼写错误
- 改进说明文档
- 添加使用示例
- 翻译文档

## 许可证

通过贡献代码，您同意您的贡献将在 GNU AGPL v3 许可证下发布。

## 联系方式

如有问题，请通过GitHub Issues联系维护者。
