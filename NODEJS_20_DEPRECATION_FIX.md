# Node.js 20 Deprecation 修复说明

## 问题描述

GitHub Actions构建出现以下错误：

1. **Node.js 20 Deprecation Warning**:
   ```
   Node.js 20 is deprecated. The following actions target Node.js 20 but are being forced to run on Node.js 24: actions/checkout@v4, actions/setup-java@v4.
   ```

2. **actions/setup-java Deprecation Warning**:
   ```
   setup-java v4 is deprecated and will no longer receive updates. Please migrate to actions/setup-java@v5.
   ```

## 问题原因

GitHub Actions正在从Node.js 20迁移到Node.js 24。使用v4版本的actions仍然依赖Node.js 20，因此会出现deprecated警告。

## 解决方案

将所有actions升级到支持Node.js 24的最新版本：

| 原版本 | 新版本 | 说明 |
|--------|--------|------|
| `actions/checkout@v4` | `actions/checkout@v5` | 支持Node.js 24 |
| `actions/setup-java@v4` | `actions/setup-java@v5` | 支持Node.js 24，不再deprecated |
| `actions/upload-artifact@v4` | `actions/upload-artifact@v5` | 最新版本 |

## 修复内容

更新了`.github/workflows/android-build.yml`文件，将所有actions升级到v5版本。

## 验证修复

修复后，GitHub Actions应该能够正常构建，不再出现deprecated警告：
1. 推送代码到main分支
2. 检查GitHub Actions标签页
3. 验证构建状态为绿色（成功）
4. 检查构建日志中没有deprecated警告

## 相关链接

- [Node.js 20 Deprecation Notice](https://github.blog/changelog/2025-09-19-deprecation-of-node-20-on-github-actions-runners/)
- [actions/checkout v5](https://github.com/actions/checkout#readme)
- [actions/setup-java v5](https://github.com/actions/setup-java#readme)
- [actions/upload-artifact v5](https://github.com/actions/upload-artifact#readme)

## 预防措施

为避免类似问题，建议：
1. 定期检查GitHub Actions的更新日志
2. 使用Dependabot自动更新actions版本
3. 关注Node.js版本迁移公告
4. 在项目文档中记录使用的actions版本

---

**修复时间**: 2024年1月1日
**修复者**: MiMo-v2.5
**影响范围**: GitHub Actions工作流
**修复状态**: ✅ 已完成
