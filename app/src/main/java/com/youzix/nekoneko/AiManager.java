package com.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * AI 配置与调用（OpenAI 兼容 chat/completions 接口）。
 * 默认配置为 DeepSeek，提示词默认为"微软式中文"风格。
 */
public class AiManager {

    private static final String PREFS = "ai_config";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_MODEL = "model";
    private static final String KEY_PROMPT = "prompt";

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    public static final String DEFAULT_MODEL = "deepseek-chat";
    public static final String DEFAULT_PROMPT =
            "你是一位专业的中文编辑。请将用户提供的文本改写为“微软式中文”风格：" +
            "使用正式、书面、简洁的简体中文；" +
            "多使用“请”“请确保”“请注意”等礼貌指令式表达；" +
            "避免口语化、网络用语和不必要的英文混排；" +
            "保持原意不变，只优化表达方式。" +
            "直接输出改写后的文本，不要输出任何解释、前缀或多余内容。";

    public static class Config {
        public String baseUrl = DEFAULT_BASE_URL;
        public String apiKey = "";
        public String model = DEFAULT_MODEL;
        public String prompt = DEFAULT_PROMPT;
    }

    public interface Callback {
        void onSuccess(String modifiedText);
        void onError(String message);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static Config load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Config c = new Config();
        c.baseUrl = sp.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        c.apiKey = sp.getString(KEY_API_KEY, "");
        c.model = sp.getString(KEY_MODEL, DEFAULT_MODEL);
        c.prompt = sp.getString(KEY_PROMPT, DEFAULT_PROMPT);
        return c;
    }

    public static void save(Context context, Config c) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_BASE_URL, c.baseUrl)
                .putString(KEY_API_KEY, c.apiKey)
                .putString(KEY_MODEL, c.model)
                .putString(KEY_PROMPT, c.prompt)
                .apply();
    }

    /**
     * 异步调用 AI 修改文本，结果通过回调返回（主线程）。
     * 提示词中含 {text} 时：{text} 替换为捕获文本，整体作为用户消息发送；
     * 否则提示词作为系统指令、捕获文本作为用户消息发送。
     */
    public static void modifyText(Config cfg, String text, Callback callback) {
        final String systemPrompt;
        final String userContent;
        if (cfg.prompt != null && cfg.prompt.contains("{text}")) {
            systemPrompt = "你是一个文本改写助手。只输出改写后的文本，不要任何解释。";
            userContent = cfg.prompt.replace("{text}", text);
        } else {
            systemPrompt = (cfg.prompt == null || cfg.prompt.trim().isEmpty())
                    ? DEFAULT_PROMPT : cfg.prompt.trim();
            userContent = text;
        }

        Thread thread = new Thread(() -> {
            try {
                String result = requestChatCompletion(cfg, systemPrompt, userContent);
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                MAIN.post(() -> callback.onError(msg));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static String requestChatCompletion(Config cfg, String systemPrompt, String userContent)
            throws Exception {
        String base = (cfg.baseUrl == null || cfg.baseUrl.trim().isEmpty())
                ? DEFAULT_BASE_URL : cfg.baseUrl.trim();
        base = base.replaceAll("/+$", "");
        URL url = new URL(base + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Authorization", "Bearer " + cfg.apiKey.trim());
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("model", cfg.model == null ? DEFAULT_MODEL : cfg.model.trim());
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userContent));
            body.put("messages", messages);
            body.put("temperature", 0.3);

            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String resp = readAll(is);
            if (code >= 400) {
                throw new Exception("HTTP " + code + ": " + truncate(resp, 200));
            }
            JSONObject root = new JSONObject(resp);
            String content = root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            if (content == null || content.trim().isEmpty()) {
                throw new Exception("AI 返回内容为空");
            }
            return content.trim();
        } finally {
            conn.disconnect();
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
