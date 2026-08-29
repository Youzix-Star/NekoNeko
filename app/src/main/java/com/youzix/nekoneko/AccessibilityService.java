package com.youzix.nekoneko;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 无障碍服务：负责捕获屏幕文本。
 *
 * 修复要点：
 * 1. 不再依赖固定的 view id（android:id/edit、android:id/text1），改为递归遍历
 *    整个节点树收集所有有文本的节点，兼容更多应用。
 * 2. 内容变化事件非常频繁，加入节流（throttle），避免反复全量捕获。
 * 3. 文本无变化时不重复通知，避免悬浮窗被同一段文本刷屏。
 * 4. 常规事件只记 DEBUG 日志，不再以 INFO 级别刷屏日志区。
 * 5. 忽略 NekoNeko 自身窗口，避免捕获到悬浮窗里的文本。
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "AccessibilityService";
    private static final int MAX_VISITED_NODES = 600;
    private static final int MAX_DEPTH = 24;
    private static final int MAX_TEXT_LENGTH = 2000;
    private static final long CONTENT_CHANGED_THROTTLE_MS = 600;

    private static AccessibilityService instance;
    private static TextCaptureListener textCaptureListener;

    private final Set<String> collectedTexts = new LinkedHashSet<>();
    private int visitedNodes = 0;
    private String lastCapturedText = "";
    private long lastContentChangedCapture = 0L;

    public interface TextCaptureListener {
        void onTextCaptured(String text);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // 窗口切换（应用切换/页面跳转）时捕获整个窗口文本
                Logger.d("窗口切换，捕获文本");
                captureAndNotify();
                break;

            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                // 输入框内容变化时捕获
                Logger.d("文本变化，捕获文本");
                captureAndNotify();
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                // 该事件非常频繁，做节流处理，避免频繁全量捕获
                long now = System.currentTimeMillis();
                if (now - lastContentChangedCapture >= CONTENT_CHANGED_THROTTLE_MS) {
                    lastContentChangedCapture = now;
                    captureAndNotify();
                }
                break;

            default:
                break;
        }
    }

    private void captureAndNotify() {
        String text = getCurrentWindowText();
        if (!text.isEmpty()) {
            notifyTextCaptured(text);
        }
    }

    /**
     * 手动获取当前窗口文本（点击"捕获文本"按钮时调用）。
     */
    public String getCurrentWindowText() {
        Logger.d("获取当前窗口文本");

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

        collectedTexts.clear();
        visitedNodes = 0;
        collectTexts(rootNode, 0);
        rootNode.recycle();

        StringBuilder sb = new StringBuilder();
        for (String text : collectedTexts) {
            if (sb.length() + text.length() > MAX_TEXT_LENGTH) {
                break;
            }
            sb.append(text).append('\n');
        }

        String result = sb.toString().trim();
        if (result.isEmpty()) {
            Logger.w("未捕获到任何文本");
        } else {
            Logger.d("成功捕获文本: " + result);
        }
        return result;
    }

    /**
     * 递归遍历节点树，收集所有含有文本的节点内容（保留出现顺序并去重）。
     */
    private void collectTexts(AccessibilityNodeInfo node, int depth) {
        if (node == null || depth > MAX_DEPTH || visitedNodes >= MAX_VISITED_NODES) {
            return;
        }
        visitedNodes++;

        CharSequence text = node.getText();
        if (text != null && text.toString().trim().length() > 0) {
            collectedTexts.add(text.toString().trim());
        }

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount && visitedNodes < MAX_VISITED_NODES; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            collectTexts(child, depth + 1);
            if (child != null) {
                child.recycle();
            }
        }
    }

    private void notifyTextCaptured(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        // 文本无变化时不再重复通知，避免刷屏
        if (text.equals(lastCapturedText)) {
            Logger.d("捕获的文本无变化，跳过通知");
            return;
        }
        lastCapturedText = text;

        if (textCaptureListener != null) {
            textCaptureListener.onTextCaptured(text);
        }

        // 同时发送广播，通知其他组件（如悬浮窗服务）
        Intent intent = new Intent("com.youzix.nekoneko.TEXT_CAPTURED");
        intent.putExtra("captured_text", text);
        sendBroadcast(intent);
        Logger.d("已发送文本捕获广播");
    }

    @Override
    public void onInterrupt() {
        Logger.w("无障碍服务被中断");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Logger.i("无障碍服务已连接");

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                              AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
                              AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
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
        lastCapturedText = "";
        Logger.w("无障碍服务已销毁");
    }

    public static AccessibilityService getInstance() {
        return instance;
    }

    public static void setTextCaptureListener(TextCaptureListener listener) {
        textCaptureListener = listener;
    }
}
