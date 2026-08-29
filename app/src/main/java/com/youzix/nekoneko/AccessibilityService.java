package com.youzix.nekoneko;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "AccessibilityService";
    private static AccessibilityService instance;
    private static TextCaptureListener textCaptureListener;

    public interface TextCaptureListener {
        void onTextCaptured(String text);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        
        int eventType = event.getEventType();
        Log.d(TAG, "Accessibility event: " + eventType);
        
        // 当窗口内容变化时，尝试获取文本
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            
            // 获取当前活动窗口的根节点
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                // 尝试获取所有可编辑文本框的内容
                List<AccessibilityNodeInfo> editTextNodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/edit");
                
                if (editTextNodes != null && !editTextNodes.isEmpty()) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (AccessibilityNodeInfo node : editTextNodes) {
                        if (node.getText() != null) {
                            textBuilder.append(node.getText()).append("\n");
                        }
                    }
                    
                    String capturedText = textBuilder.toString().trim();
                    if (!capturedText.isEmpty()) {
                        Log.d(TAG, "Captured text: " + capturedText);
                        notifyTextCaptured(capturedText);
                    }
                }
                
                rootNode.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Accessibility service connected");
        
        // 配置服务信息
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = AccessibilityEvent.TYPE_ALL_MASK;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            setServiceInfo(info);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "Accessibility service destroyed");
    }

    public static AccessibilityService getInstance() {
        return instance;
    }

    public static void setTextCaptureListener(TextCaptureListener listener) {
        textCaptureListener = listener;
    }

    private void notifyTextCaptured(String text) {
        if (textCaptureListener != null) {
            textCaptureListener.onTextCaptured(text);
        }
        
        // 同时发送广播，通知其他组件
        Intent intent = new Intent("com.youzix.nekoneko.TEXT_CAPTURED");
        intent.putExtra("captured_text", text);
        sendBroadcast(intent);
    }

    // 手动获取当前窗口文本的方法
    public String getCurrentWindowText() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return "";
        
        StringBuilder textBuilder = new StringBuilder();
        
        // 尝试获取所有可编辑文本框的内容
        List<AccessibilityNodeInfo> editTextNodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/edit");
        if (editTextNodes != null) {
            for (AccessibilityNodeInfo node : editTextNodes) {
                if (node.getText() != null) {
                    textBuilder.append(node.getText()).append("\n");
                }
                node.recycle();
            }
        }
        
        // 如果没有找到编辑框，尝试获取所有文本视图的内容
        if (textBuilder.length() == 0) {
            List<AccessibilityNodeInfo> textNodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/text1");
            if (textNodes != null) {
                for (AccessibilityNodeInfo node : textNodes) {
                    if (node.getText() != null) {
                        textBuilder.append(node.getText()).append("\n");
                    }
                    node.recycle();
                }
            }
        }
        
        rootNode.recycle();
        return textBuilder.toString().trim();
    }
}
