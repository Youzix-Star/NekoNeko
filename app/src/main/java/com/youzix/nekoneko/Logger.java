package com.youzix.nekoneko;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Logger {
    private static final String TAG = "NekoNeko";
    private static final int MAX_LOG_ENTRIES = 50;
    private static List<String> logEntries = new ArrayList<>();
    private static LogListener logListener;

    public interface LogListener {
        void onLogAdded(String logEntry);
    }

    public static void setLogListener(LogListener listener) {
        logListener = listener;
    }

    public static void d(String message) {
        Log.d(TAG, message);
        addLogEntry("DEBUG", message);
    }

    public static void i(String message) {
        Log.i(TAG, message);
        addLogEntry("INFO", message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
        addLogEntry("WARN", message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
        addLogEntry("ERROR", message);
    }

    public static void e(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
        addLogEntry("ERROR", message + " - " + throwable.getMessage());
    }

    private static void addLogEntry(String level, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String logEntry = String.format("[%s] %s: %s", timestamp, level, message);
        
        logEntries.add(logEntry);
        
        // 保持日志条目在最大限制内
        if (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.remove(0);
        }
        
        // 通知监听器
        if (logListener != null) {
            logListener.onLogAdded(logEntry);
        }
    }

    public static List<String> getLogEntries() {
        return new ArrayList<>(logEntries);
    }

    public static String getLogEntriesAsString() {
        StringBuilder sb = new StringBuilder();
        for (String entry : logEntries) {
            sb.append(entry).append("\n");
        }
        return sb.toString();
    }

    public static void clearLogs() {
        logEntries.clear();
    }
}
