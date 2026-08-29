package com.youzix.nekoneko;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 无障碍服务：手动捕获 / 修改"正在输入"的文本框。
 *
 * 行为说明（按需求）：
 * 1. 不做任何自动捕获——只有用户点击悬浮窗按钮时才操作。
 * 2. 只针对当前获得输入焦点（正在编辑、有光标/输入指示）的输入框：
 *    - 捕获：读取其文本
 *    - 替换：整体改为指定文本（ACTION_SET_TEXT）
 *    - 增加：在末尾追加指定文本（ACTION_SET_TEXT）
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "AccessibilityService";
    private static final int MAX_DEPTH = 24;

    private static AccessibilityService instance;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 按需求不做自动捕获：仅支持手动点击按钮时调用
    }

    @Override
    public void onInterrupt() {
        Logger.w("无障碍服务被中断");
    }

    /**
     * 捕获当前正在输入的输入框文本。
     *
     * @return 焦点输入框内的文本；未检测到正在输入的输入框时返回空字符串
     */
    public String getCurrentWindowText() {
        Logger.d("手动捕获：查找当前聚焦的输入框");

        AccessibilityNodeInfo input = getFocusedInputNode();
        if (input == null) {
            Logger.w("未检测到正在输入的文本框");
            return "";
        }

        String text = extractInputText(input);
        input.recycle();

        if (text.isEmpty()) {
            Logger.w("未检测到正在输入的文本框");
        } else {
            Logger.i("成功捕获输入框文本: " + text);
        }
        return text;
    }

    /**
     * 将当前输入框内容整体替换为 newText。
     *
     * @return true 表示操作成功；false 表示未找到输入框或应用不支持该操作
     */
    public boolean replaceInputText(String newText) {
        Logger.d("手动替换输入框内容");

        AccessibilityNodeInfo input = getFocusedInputNode();
        if (input == null) {
            Logger.w("未检测到正在输入的文本框");
            return false;
        }

        boolean ok = setNodeText(input, newText);
        input.recycle();
        Logger.i("替换结果: " + ok);
        return ok;
    }

    /**
     * 在当前输入框文本末尾追加 suffix。
     *
     * @return true 表示操作成功；false 表示未找到输入框或应用不支持该操作
     */
    public boolean appendInputText(String suffix) {
        Logger.d("手动在输入框末尾追加文本");

        AccessibilityNodeInfo input = getFocusedInputNode();
        if (input == null) {
            Logger.w("未检测到正在输入的文本框");
            return false;
        }

        CharSequence current = input.getText();
        String newText = (current == null ? "" : current.toString()) + suffix;
        boolean ok = setNodeText(input, newText);
        input.recycle();
        Logger.i("追加结果: " + ok);
        return ok;
    }

    /**
     * 获取当前窗口中获得输入焦点的可编辑节点；无则返回 null。
     * 返回的节点由调用方负责 recycle（返回节点不会与内部临时节点重复）。
     */
    private AccessibilityNodeInfo getFocusedInputNode() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Logger.w("无法获取根节点");
            return null;
        }

        // 忽略 NekoNeko 自身窗口
        CharSequence pkg = rootNode.getPackageName();
        if (pkg != null && getPackageName().equals(pkg.toString())) {
            Logger.d("当前窗口是 NekoNeko 自身，跳过");
            rootNode.recycle();
            return null;
        }

        AccessibilityNodeInfo input = findFocusedInputNode(rootNode);
        if (input != rootNode) {
            rootNode.recycle();
        }
        return input;
    }

    /** 在指定根节点下查找获得输入焦点的可编辑节点。 */
    private AccessibilityNodeInfo findFocusedInputNode(AccessibilityNodeInfo root) {
        // 方式一：系统 API 直接获取拥有输入焦点的节点
        AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focused != null) {
            if (focused.isEditable()) {
                return focused;
            }
            // 焦点落在容器上：在其子树中查找可编辑节点
            AccessibilityNodeInfo editable = findEditableDescendant(focused);
            focused.recycle();
            if (editable != null) {
                return editable;
            }
        }

        // 方式二：部分应用焦点信息拿不到，遍历节点树查找处于聚焦状态的可编辑节点
        return findFocusedEditableRecursive(root, 0);
    }

    /** 查找子树中第一个可编辑节点（返回的节点不会被重复回收）。 */
    private AccessibilityNodeInfo findEditableDescendant(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        if (node.isEditable()) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo found = findEditableDescendant(child);
            if (found != null) {
                if (child != null && found != child) {
                    child.recycle();
                }
                return found;
            }
            if (child != null) {
                child.recycle();
            }
        }
        return null;
    }

    /** 遍历查找第一个"聚焦且可编辑"的节点（返回的节点不会被重复回收）。 */
    private AccessibilityNodeInfo findFocusedEditableRecursive(AccessibilityNodeInfo node, int depth) {
        if (node == null || depth > MAX_DEPTH) {
            return null;
        }
        if ((node.isFocused() || node.isAccessibilityFocused()) && node.isEditable()) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo found = findFocusedEditableRecursive(child, depth + 1);
            if (found != null) {
                if (child != null && found != child) {
                    child.recycle();
                }
                return found;
            }
            if (child != null) {
                child.recycle();
            }
        }
        return null;
    }

    /** 通过 ACTION_SET_TEXT 将节点文本整体替换。 */
    private boolean setNodeText(AccessibilityNodeInfo node, String text) {
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
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

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Logger.i("无障碍服务已连接");

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            // 手动操作只需要能读取当前窗口，无需依赖事件驱动
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
