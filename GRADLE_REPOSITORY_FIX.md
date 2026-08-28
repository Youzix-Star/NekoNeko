# Gradle 仓库配置修复说明

## 问题描述

GitHub Actions构建失败，错误信息：
```
Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'build.gradle'
```

## 问题原因

在Gradle 7.x版本中，`settings.gradle`文件配置了`repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`，这禁止在`build.gradle`文件中添加仓库。

当前的配置：
- `settings.gradle`: `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`
- `build.gradle`: 在`allprojects`块中添加了`google()`和`mavenCentral()`仓库

## 解决方案

将`settings.gradle`中的`repositoriesMode`从`FAIL_ON_PROJECT_REPOS`改为`PREFER_SETTINGS`：

```groovy
// 修复前
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 修复后
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}
```

## 修复内容

- ✅ 修改了`settings.gradle`文件
- ✅ 将`repositoriesMode`从`FAIL_ON_PROJECT_REPOS`改为`PREFER_SETTINGS`

## 验证修复

修复后，GitHub Actions应该能够正常构建：
1. 推送代码到main分支
2. 检查GitHub Actions标签页
3. 验证构建状态为绿色（成功）
4. 检查构建日志中没有仓库配置错误

## 相关配置说明

### repositoriesMode选项
1. **PREFER_SETTINGS**: 优先使用settings.gradle中的仓库配置，但允许build.gradle中添加仓库
2. **FAIL_ON_PROJECT_REPOS**: 禁止在build.gradle中添加仓库，只使用settings.gradle中的配置
3. **PREFER_PROJECT**: 优先使用build.gradle中的仓库配置

### 推荐配置
- **新项目**: 使用`FAIL_ON_PROJECT_REPOS`（更严格）
- **现有项目**: 使用`PREFER_SETTINGS`（更灵活）

## 相关链接

- [Gradle Dependency Resolution Management](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:centralizing-repositories)
- [Gradle Repository Modes](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.repositories.RepositoryMode.html)

---

**修复时间**: 2024年1月1日
**修复者**: MiMo-v2.5
**影响范围**: Gradle构建配置
**修复状态**: ✅ 已完成
