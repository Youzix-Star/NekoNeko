package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Token 用量统计：按模型分别记录，支持按时间范围和模型筛选。
 * 数据存储在 SharedPreferences 的 JSON 数组中。
 * 自动清理 90 天前的记录。
 */
public class TokenStats {

    private static final String PREFS = "token_stats";
    private static final String KEY_RECORDS = "records";

    /** 单次用量记录。 */
    public static class Record {
        public long timestamp;
        public String model;
        public int promptTokens;
        public int completionTokens;
        public int totalTokens;
        public int cachedTokens;

        public Record(long timestamp, String model, int prompt, int completion,
                      int total, int cachedTokens) {
            this.timestamp = timestamp;
            this.model = model == null ? "unknown" : model;
            this.promptTokens = prompt;
            this.completionTokens = completion;
            this.totalTokens = total;
            this.cachedTokens = cachedTokens;
        }
    }

    /** 一段时间内的统计汇总。 */
    public static class Stats {
        public int totalCalls;
        public int totalPromptTokens;
        public int totalCompletionTokens;
        public int totalTokens;
        public int cachedTokens;
        public int cachedCalls;

        public int cacheHitPercent() {
            return totalCalls > 0 ? (int) (cachedCalls * 100f / totalCalls) : 0;
        }
    }

    /** 记录一次 API 调用。 */
    public static void record(Context context, String model, int promptTokens,
                              int completionTokens, int totalTokens, int cachedTokens) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_RECORDS, "[]");
        List<Record> records = parseRecords(raw);

        records.add(new Record(System.currentTimeMillis(), model,
                promptTokens, completionTokens, totalTokens, cachedTokens));

        // 清理 90 天前的记录
        long cutoff = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000;
        List<Record> cleaned = new ArrayList<>();
        for (Record r : records) {
            if (r.timestamp >= cutoff) cleaned.add(r);
        }

        sp.edit().putString(KEY_RECORDS, serialize(cleaned)).apply();
    }

    /** 查询指定时间范围、指定模型的统计。model 为 null 表示全部模型。 */
    public static Stats query(Context context, long fromTimestamp, String model) {
        List<Record> records = loadRecords(context);
        Stats s = new Stats();
        for (Record r : records) {
            if (r.timestamp < fromTimestamp) continue;
            if (model != null && !model.equals(r.model)) continue;
            s.totalCalls++;
            s.totalPromptTokens += r.promptTokens;
            s.totalCompletionTokens += r.completionTokens;
            s.totalTokens += r.totalTokens;
            s.cachedTokens += r.cachedTokens;
            if (r.cachedTokens > 0) s.cachedCalls++;
        }
        return s;
    }

    /** 获取所有出现过的模型名（按使用量降序）。 */
    public static List<String> getModelNames(Context context) {
        List<Record> records = loadRecords(context);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Record r : records) {
            counts.merge(r.model, r.totalTokens, Integer::sum);
        }
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : sorted) {
            result.add(e.getKey());
        }
        return result;
    }

    // ---------- 内部 ----------

    private static List<Record> loadRecords(Context context) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_RECORDS, "[]");
        return parseRecords(raw);
    }

    private static String serialize(List<Record> records) {
        JSONArray arr = new JSONArray();
        try {
            for (Record r : records) {
                JSONObject o = new JSONObject();
                o.put("ts", r.timestamp);
                o.put("m", r.model);
                o.put("p", r.promptTokens);
                o.put("c", r.completionTokens);
                o.put("t", r.totalTokens);
                o.put("cache", r.cachedTokens);
                arr.put(o);
            }
        } catch (Exception ignored) {
        }
        return arr.toString();
    }

    private static List<Record> parseRecords(String json) {
        List<Record> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Record(
                        o.optLong("ts", 0),
                        o.optString("m", "unknown"),
                        o.optInt("p", 0),
                        o.optInt("c", 0),
                        o.optInt("t", 0),
                        o.optInt("cache", 0)
                ));
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
