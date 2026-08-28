#!/bin/bash

echo "=== NekoNeko 本地构建脚本 ==="
echo ""

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK 11或更高版本"
    exit 1
fi

echo "Java版本信息:"
java -version
echo ""

# 检查Android SDK（可选）
if [ -n "$ANDROID_HOME" ]; then
    echo "Android SDK路径: $ANDROID_HOME"
else
    echo "警告: 未设置ANDROID_HOME环境变量，可能无法构建Android应用"
fi

echo ""

# 运行Gradle构建
echo "开始构建..."
if [ -f "./gradlew" ]; then
    chmod +x gradlew
    ./gradlew assembleDebug
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ 构建成功！"
        echo "Debug APK文件位于: app/build/outputs/apk/debug/"
        ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "无法列出APK文件"
    else
        echo ""
        echo "❌ 构建失败"
        exit 1
    fi
else
    echo "错误: 未找到gradlew脚本"
    exit 1
fi
