#!/bin/bash

echo "=== 下载 Gradle Wrapper JAR ==="
echo ""

# 创建目录
mkdir -p gradle/wrapper

# 尝试从GitHub下载gradle-wrapper.jar
echo "尝试从GitHub下载gradle-wrapper.jar..."
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v7.5.1/gradle/wrapper/gradle-wrapper.jar"

# 使用curl下载
if curl -sL "$WRAPPER_URL" -o gradle/wrapper/gradle-wrapper.jar; then
    echo "✅ 下载成功"
    echo "文件大小: $(ls -lh gradle/wrapper/gradle-wrapper.jar | awk '{print $5}')"
else
    echo "❌ 下载失败"
    echo ""
    echo "替代方案："
    echo "1. 检查网络连接"
    echo "2. 手动下载：https://services.gradle.org/distributions/gradle-7.5.1-bin.zip"
    echo "3. 从zip文件中提取 gradle/wrapper/gradle-wrapper.jar"
fi

echo ""
echo "=== 完成 ==="
