package top.youzix.nekoneko;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 从 GitHub Releases API 检查是否有新版本。
 * 使用不认证的 API 请求（匿名，速率限制 60 次/小时）。
 */
public class UpdateChecker {

    private static final String REPO = "Youzix-Star/NekoNeko";
    private static final String API_URL =
            "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onUpdateAvailable(String latestVersion, String body, String apkUrl);
        void onNoUpdate();
        void onError(String message);
    }

    /** 异步检查更新，回调在主线程。 */
    public static void checkForUpdate(Context context, Callback callback) {
        String currentVersion = getCurrentVersion(context);
        if (currentVersion == null) {
            MAIN.post(() -> onError(callback, "无法获取当前版本"));
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                    int code = conn.getResponseCode();
                    InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
                    String resp = readAll(is);
                    if (code >= 400) {
                        onError(callback, "GitHub API 错误: HTTP " + code);
                        return;
                    }

                    JSONObject release = new JSONObject(resp);
                    String tag = release.optString("tag_name", "");
                    String body = release.optString("body", "");
                    // 去掉 tag 前缀 v
                    String latestVersion = tag.startsWith("v") ? tag.substring(1) : tag;

                    // 查找 APK 下载链接
                    String apkUrl = null;
                    JSONArray assets = release.optJSONArray("assets");
                    if (assets != null) {
                        for (int i = 0; i < assets.length(); i++) {
                            JSONObject asset = assets.getJSONObject(i);
                            String name = asset.optString("name", "");
                            if (name.endsWith(".apk") && !name.contains("debug")) {
                                apkUrl = asset.optString("browser_download_url", null);
                                break;
                            }
                        }
                    }

                    if (isNewer(latestVersion, currentVersion)) {
                        final String v = latestVersion;
                        final String b = body;
                        final String u = apkUrl;
                        MAIN.post(() -> callback.onUpdateAvailable(v, b, u));
                    } else {
                        MAIN.post(() -> callback.onNoUpdate());
                    }
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                onError(callback, msg);
            }
        }).start();
    }

    /** 打开 GitHub Releases 页面供用户手动下载。 */
    public static void openReleasePage(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/" + REPO + "/releases/latest"));
        context.startActivity(intent);
    }

    /** 从 PackageManager 获取当前版本名（如 "1.7.8"）。 */
    private static String getCurrentVersion(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /** 比较两个语义化版本号，返回 latest > current。 */
    private static boolean isNewer(String latest, String current) {
        String[] la = latest.split("\\.");
        String[] cu = current.split("\\.");
        int len = Math.max(la.length, cu.length);
        for (int i = 0; i < len; i++) {
            int l = i < la.length ? parsePart(la[i]) : 0;
            int c = i < cu.length ? parsePart(cu[i]) : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private static int parsePart(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private static void onError(Callback callback, String msg) {
        MAIN.post(() -> callback.onError(msg));
    }
}
