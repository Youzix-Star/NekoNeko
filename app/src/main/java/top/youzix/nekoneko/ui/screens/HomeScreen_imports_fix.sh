#!/bin/bash
FILE="/data/data/com.termux/files/home/NekoNeko/NekoNeko/app/src/main/java/top/youzix/nekoneko/ui/screens/HomeScreen.kt"

# Replace the wildcard import with specific imports + R import
sed -i 's|import top.youzix.nekoneko.\*|import top.youzix.nekoneko.R\nimport top.youzix.nekoneko.FloatingWindowService\nimport top.youzix.nekoneko.TokenStats\nimport top.youzix.nekoneko.DarkModePrefs|' "$FILE"

# Remove the duplicate subpackage imports
sed -i '/import top.youzix.nekoneko.ui.screens.RingChart/d' "$FILE"
sed -i '/import top.youzix.nekoneko.ui.theme.ComposeThemeManager/d' "$FILE"
sed -i '/import top.youzix.nekoneko.ui.theme.SectionLabel/d' "$FILE"
sed -i '/import top.youzix.nekoneko.ui.theme.CardSection/d' "$FILE"
sed -i '/import top.youzix.nekoneko.ui.theme.FeatureRow/d' "$FILE"
sed -i '/import top.youzix.nekoneko.ui.theme.DividerRow/d' "$FILE"
