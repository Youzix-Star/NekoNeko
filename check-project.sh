#!/bin/bash

echo "=== NekoNeko 项目状态检查 ==="
echo ""

# 检查Git状态
echo "1. Git状态检查:"
echo "   分支: $(git branch --show-current)"
echo "   最新提交: $(git log --oneline -1)"
echo "   远程仓库: $(git remote get-url origin)"
echo ""

# 检查项目文件
echo "2. 项目文件检查:"
echo "   总文件数: $(find . -type f -not -path './.git/*' | wc -l)"
echo "   Java源文件: $(find . -name "*.java" | wc -l)"
echo "   XML资源文件: $(find . -name "*.xml" | wc -l)"
echo "   Gradle文件: $(find . -name "*.gradle" | wc -l)"
echo "   文档文件: $(find . -name "*.md" | wc -l)"
echo ""

# 检查GitHub Actions
echo "3. GitHub Actions检查:"
if [ -f ".github/workflows/android-build.yml" ]; then
    echo "   ✅ 工作流文件存在"
else
    echo "   ❌ 工作流文件不存在"
fi
echo ""

# 检查构建脚本
echo "4. 构建脚本检查:"
if [ -f "build-local.sh" ]; then
    echo "   ✅ 本地构建脚本存在"
    if [ -x "build-local.sh" ]; then
        echo "   ✅ 构建脚本可执行"
    else
        echo "   ⚠️  构建脚本不可执行"
    fi
else
    echo "   ❌ 本地构建脚本不存在"
fi
echo ""

# 检查文档
echo "5. 文档检查:"
for doc in README.md APK_DOWNLOAD_GUIDE.md CONTRIBUTING.md CHANGELOG.md PROJECT_SUMMARY.md QUICK_START.md; do
    if [ -f "$doc" ]; then
        echo "   ✅ $doc 存在"
    else
        echo "   ❌ $doc 不存在"
    fi
done
echo ""

# 检查Android项目结构
echo "6. Android项目结构检查:"
if [ -f "app/build.gradle" ]; then
    echo "   ✅ 应用构建文件存在"
else
    echo "   ❌ 应用构建文件不存在"
fi

if [ -f "app/src/main/AndroidManifest.xml" ]; then
    echo "   ✅ AndroidManifest.xml存在"
else
    echo "   ❌ AndroidManifest.xml不存在"
fi

if [ -f "app/src/main/java/com/youzix/nekoneko/MainActivity.java" ]; then
    echo "   ✅ MainActivity.java存在"
else
    echo "   ❌ MainActivity.java不存在"
fi
echo ""

echo "=== 检查完成 ==="
echo ""
echo "项目已准备好进行构建和部署！"
echo "访问 https://github.com/Youzix-Star/NekoNeko 查看仓库"
