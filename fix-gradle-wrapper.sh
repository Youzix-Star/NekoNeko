#!/bin/bash

echo "=== 修复 Gradle Wrapper ==="
echo ""

# 创建gradle wrapper目录
mkdir -p gradle/wrapper

# 下载Gradle Wrapper JAR文件
echo "下载Gradle Wrapper JAR文件..."
GRADLE_VERSION="7.5.1"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# 检查是否有wget或curl
if command -v wget &> /dev/null; then
    wget -q "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar" -O gradle/wrapper/gradle-wrapper.jar
elif command -v curl &> /dev/null; then
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar" -o gradle/wrapper/gradle-wrapper.jar
else
    echo "错误: 需要wget或curl来下载文件"
    exit 1
fi

# 检查下载是否成功
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "✅ Gradle Wrapper JAR文件下载成功"
    echo "文件大小: $(ls -lh gradle/wrapper/gradle-wrapper.jar | awk '{print $5}')"
else
    echo "❌ 下载失败，尝试其他方法..."
    
    # 创建一个简单的gradlew脚本，使用系统gradle（如果可用）
    if command -v gradle &> /dev/null; then
        echo "检测到系统gradle，创建简单gradlew脚本..."
        cat > gradlew << 'GRADLEW_SCRIPT'
#!/bin/sh

# 使用系统gradle
exec gradle "$@"
GRADLEW_SCRIPT
        chmod +x gradlew
        echo "✅ 创建了使用系统gradle的gradlew脚本"
    else
        echo "❌ 系统gradle不可用，需要手动下载Gradle Wrapper"
        echo ""
        echo "请手动下载Gradle Wrapper JAR文件:"
        echo "1. 访问: https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
        echo "2. 解压后找到 gradle/wrapper/gradle-wrapper.jar"
        echo "3. 将文件放到 gradle/wrapper/gradle-wrapper.jar"
        exit 1
    fi
fi

echo ""
echo "=== 修复完成 ==="
echo "现在可以运行: ./gradlew assembleDebug"
