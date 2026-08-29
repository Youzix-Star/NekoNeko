package com.youzix.nekoneko;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 无障碍服务：手动捕获"正在输入"的文本框内容。
 *
 * 行为说明（按需求）：
 * 1. 不做任何自动捕获——只有用户点击悬浮窗的"捕获文本"按钮时才捕获。
 * 2. 只捕获当前获得输入焦点（正在编辑、有光标/输入指示）的输入框内的文本，
 *    不收集屏幕上其他任何文本。
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "AccessibilityService";
    private static final int MAX_DEPTH = 24;

    private static AccessibilityService instance;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 按需求不做自动捕获：仅支持手动点击"捕获"按钮时调用 getCurrentWindowText()
    }

    @Override
    public void onInterrupt() {
        Logger.w("无障碍服务被中断");
    }

    /**
     * 手动获取当前正在输入的文本框内容（点击"捕获文本"按钮时调用）。
     *
     * @return 焦点输入框内的文本；未检测到正在输入的输入框时返回空字符串
     */
    public String getCurrentWindowText() {
        Logger.d("手动捕获：查找当前聚焦的输入框");

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Logger.w("无法获取根节点");
            return "";
        }

        // 忽略 NekoNeko 自身窗口，避免捕获到悬浮窗内的文本
        CharSequence packageName = rootNode.getPackageName();
        if (packageName != null && getPackageName().equals(packageName.toString())) {
            Logger.d("当前窗口是 NekoNeko 自身，跳过");
            rootNode.recycle();
            return "";
        }

        String text = "";

        // 方式一：系统 API 直接获取拥有输入焦点的节点
        AccessibilityNodeInfo focused = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focused != null) {
            text = extractInputText(focused);
            focused.recycle();
        }

        // 方式二：部分应用焦点信息拿不到，遍历节点树查找处于聚焦状态的输入框
        if (text.isEmpty()) {
            text = findFocusedInputText(rootNode);
        }

        rootNode.recycle();

        if (text.isEmpty()) {
            Logger.w("未检测到正在输入的文本框");
        } else {
            Logger.i("成功捕获输入框文本: " + text);
        }
        return text;
    }

    /** 取节点文本；节点本身无文本时在其子树中查找第一个含文本的节点。 */
    private String extractInputText(AccessibilityNodeInfo node) {
        if (node == null) {
            return "";
        }
        CharSequence t = node.getText();
        if (t != null && t.toString().trim().length() > 0) {
            return t.toString().trim();
        }
        return findFirstText(node, 0);
    }

    private String findFirstText(AccessibilityNodeInfo node, int depth) {
        if (node == null || depth > MAX_DEPTH) {
            return "";
        }
        CharSequence t = node.getText();
        if (t != null && t.toString().trim().length() > 0) {
            return t.toString().trim();
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            String text = findFirstText(child, depth + 1);
            if (child != null) {
                child.recycle();
            }
            if (!text.isEmpty()) {
                return text;
            }
        }
        return "";
    }

    /** 遍历节点树，返回第一个处于聚焦状态的输入框/文本节点内容。 */
    private String findFocusedInputText(AccessibilityNodeInfo node) {
        return findFocusedInputTextRecursive(node, 0);
    }

    private String findFocusedInputTextRecursive(AccessibilityNodeInfo node, int depth) {
        if (node == null || depth > MAX_DEPTH) {
            return "";
        }
        if (node.isFocused() || node.isAccessibilityFocused()) {
            String text = extractInputText(node);
            if (!text.isEmpty()) {
                return text;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            String text = findFocusedInputTextRecursive(child, depth + 1);
            if (child != null) {
                child.recycle();
            }
            if (!text.isEmpty()) {
                return text;
            }
        }
        return "";
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Logger.i("无障碍服务已连接");

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            // 手动捕获只需要能读取当前窗口，无需依赖事件驱动
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.notificationTimeout = 100;
            info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                         AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            setServiceInfo(info);
            Logger.i("无障碍服务配置完成");
        } else {
            Logger.e("无法获取无障碍服务信息");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Logger.w("无障碍服务已销毁");
    }

    public static AccessibilityService getInstance() {
        return instance;
    }
}
