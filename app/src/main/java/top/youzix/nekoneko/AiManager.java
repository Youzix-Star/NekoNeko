package top.youzix.nekoneko;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * AI 配置、预设与调用（OpenAI 兼容 chat/completions 接口）。
 * 默认配置为 DeepSeek（deepseek-v4-flash），提示词默认为"微软式中文"风格。
 */
public class AiManager {

    private static final String PREFS = "ai_config";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_MODEL = "model";
    private static final String KEY_PROMPT = "prompt";
    private static final String KEY_PRESETS = "presets";

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    public static final String DEFAULT_MODEL = "deepseek-v4-flash";
    public static final String DEFAULT_PROMPT =
            "你是一位专业的中文编辑。请将用户提供的文本改写为“微软式中文”风格：" +
            "使用正式、书面、简洁的简体中文；" +
            "多使用“请”“请确保”“请注意”等礼貌指令式表达；" +
            "避免口语化、网络用语和不必要的英文混排；" +
            "保持原意不变，只优化表达方式。" +
            "直接输出改写后的文本，不要输出任何解释、前缀或多余内容。";

    // 内置预设
    public static final String PRESET_MS_TRANSLATE = "微软式翻译";
    public static final String PRESET_MS_CHINESE = "微软式中文";
    public static final String BUILTIN_TRANSLATE_PROMPT =
            "你是一位幽默的文本改写专家。请把用户提供的文本改写为“微软式翻译腔”风格：" +
            "使用正式、书面、略带生硬直译腔调的简体中文；" +
            "多使用“请”“请确保”“请勿”“请联系您的管理员”“为了继续”“我们对此感到抱歉，但也不是非常抱歉”等典型微软系统提示式表达；" +
            "语气礼貌、机械，像系统更新提示或错误对话框；" +
            "保持原意大体不变，允许适度夸张以增强幽默效果。" +
            "参考风格示例：“我们正在为您的设备准备一些重要的更新。请勿关闭您的计算机。”、“您没有权限执行此操作。请联系您的管理员为了请求这个权限。”" +
            "直接输出改写后的文本，不要任何解释。";

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

    public interface ListCallback {
        void onSuccess(List<String> models);
        void onError(String message);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static UsageRecord _lastUsage = null;

    /** 上次调用的用量记录。 */
    public static class UsageRecord {
        public String model;
        public int promptTokens;
        public int completionTokens;
        public int totalTokens;
        public int cachedTokens;
    }

    /** 获取上次调用的用量记录，查询后清空。 */
    public static UsageRecord consumeLastUsage() {
        UsageRecord u = _lastUsage;
        _lastUsage = null;
        return u;
    }

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
                .putString(KEY_BASE_URL, c.baseUrl == null ? "" : c.baseUrl)
                .putString(KEY_API_KEY, c.apiKey == null ? "" : c.apiKey)
                .putString(KEY_MODEL, c.model == null ? "" : c.model)
                .putString(KEY_PROMPT, c.prompt == null ? "" : c.prompt)
                .apply();
    }

    // ---------- 预设 ----------

    /** 内置预设：仅提示词非空，其余字段留空表示沿用当前配置。 */
    public static Config builtinPreset(String name) {
        Config c = new Config();
        c.baseUrl = "";
        c.apiKey = "";
        c.model = "";
        if (PRESET_MS_TRANSLATE.equals(name)) {
            c.prompt = BUILTIN_TRANSLATE_PROMPT;
        } else if (PRESET_MS_CHINESE.equals(name)) {
            c.prompt = DEFAULT_PROMPT;
        } else {
            c.prompt = DEFAULT_PROMPT;
        }
        return c;
    }

    public static void savePreset(Context context, String name, Config c) {
        JSONObject presets = getPresetsJson(context);
        JSONObject o = new JSONObject();
        try {
            o.put(KEY_BASE_URL, c.baseUrl == null ? "" : c.baseUrl);
            o.put(KEY_API_KEY, c.apiKey == null ? "" : c.apiKey);
            o.put(KEY_MODEL, c.model == null ? "" : c.model);
            o.put(KEY_PROMPT, c.prompt == null ? "" : c.prompt);
            presets.put(name, o);
            putPresetsJson(context, presets);
        } catch (Exception ignored) {
        }
    }

    public static boolean deletePreset(Context context, String name) {
        JSONObject presets = getPresetsJson(context);
        if (presets.has(name)) {
            presets.remove(name);
            putPresetsJson(context, presets);
            return true;
        }
        return false;
    }

    /** 用户自定义预设名（不含内置）。 */
    public static List<String> getUserPresetNames(Context context) {
        List<String> names = new ArrayList<>();
        JSONObject presets = getPresetsJson(context);
        Iterator<String> it = presets.keys();
        while (it.hasNext()) {
            names.add(it.next());
        }
        Collections.sort(names);
        return names;
    }

    /** 全部可加载预设名（内置 + 用户自定义）。 */
    public static List<String> getAllPresetNames(Context context) {
        List<String> names = new ArrayList<>();
        names.add(PRESET_MS_TRANSLATE);
        names.add(PRESET_MS_CHINESE);
        names.addAll(getUserPresetNames(context));
        return names;
    }

    /** 读取预设；内置预设或用户预设均可。 */
    public static Config loadPreset(Context context, String name) {
        if (PRESET_MS_TRANSLATE.equals(name) || PRESET_MS_CHINESE.equals(name)) {
            return builtinPreset(name);
        }
        JSONObject presets = getPresetsJson(context);
        JSONObject o = presets.optJSONObject(name);
        Config c = new Config();
        if (o != null) {
            c.baseUrl = o.optString(KEY_BASE_URL, "");
            c.apiKey = o.optString(KEY_API_KEY, "");
            c.model = o.optString(KEY_MODEL, "");
            c.prompt = o.optString(KEY_PROMPT, DEFAULT_PROMPT);
        }
        return c;
    }

    private static JSONObject getPresetsJson(Context context) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PRESETS, "{}");
        try {
            return new JSONObject(raw);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static void putPresetsJson(Context context, JSONObject obj) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PRESETS, obj.toString())
                .apply();
    }

    // ---------- AI 调用 ----------

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

        final String modelName = cfg.model;
        Thread thread = new Thread(() -> {
            try {
                _lastUsage = null;
                String result = requestChatCompletion(cfg, systemPrompt, userContent);
                // 补充 model 名到 usage
                if (_lastUsage != null && _lastUsage.model == null) {
                    _lastUsage.model = modelName;
                }
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                MAIN.post(() -> callback.onError(msg));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 异步获取模型列表（GET {base}/models），结果通过回调返回（主线程）。
     */
    public static void listModels(Config cfg, ListCallback callback) {
        Thread thread = new Thread(() -> {
            try {
                String base = (cfg.baseUrl == null || cfg.baseUrl.trim().isEmpty())
                        ? DEFAULT_BASE_URL : cfg.baseUrl.trim();
                base = base.replaceAll("/+$", "");
                URL url = new URL(base + "/models");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setRequestProperty("Authorization", "Bearer " + (cfg.apiKey == null ? "" : cfg.apiKey.trim()));

                    int code = conn.getResponseCode();
                    InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
                    String resp = readAll(is);
                    if (code >= 400) {
                        throw new Exception("HTTP " + code + ": " + truncate(resp, 200));
                    }
                    JSONObject root = new JSONObject(resp);
                    JSONArray data = root.getJSONArray("data");
                    List<String> models = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        models.add(data.getJSONObject(i).getString("id"));
                    }
                    if (models.isEmpty()) {
                        throw new Exception("模型列表为空");
                    }
                    MAIN.post(() -> callback.onSuccess(models));
                } finally {
                    conn.disconnect();
                }
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
            conn.setRequestProperty("Authorization", "Bearer " + (cfg.apiKey == null ? "" : cfg.apiKey.trim()));
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("model", cfg.model == null || cfg.model.trim().isEmpty()
                    ? DEFAULT_MODEL : cfg.model.trim());
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

            // 提取 token 用量并记录
            if (root.has("usage")) {
                JSONObject usage = root.getJSONObject("usage");
                int prompt = usage.optInt("prompt_tokens", 0);
                int completion = usage.optInt("completion_tokens", 0);
                int total = usage.optInt("total_tokens", 0);
                int cached = usage.optInt("prompt_tokens_cached", 0);
                // 通过 ThreadLocal 传递给调用方
                _lastUsage = new int[]{prompt, completion, total, cached};
            } else {
                _lastUsage = null;
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
