package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Token 用量统计：每次 AI 调用后记录，按时间查询。
 * 数据存储在 SharedPreferences 的 JSON 数组中，每条记录带时间戳。
 * 自动清理 30 天前的记录。
 */
public class TokenStats {

    private static final String PREFS = "token_stats";
    private static final String KEY_RECORDS = "records";

    /** 单次用量记录。 */
    public static class Record {
        public long timestamp;      // 毫秒
        public int promptTokens;
        public int completionTokens;
        public int totalTokens;
        public boolean cached;      // 是否缓存命中（部分 API 支持 cached_tokens）

        public Record(long timestamp, int prompt, int completion, int total, boolean cached) {
            this.timestamp = timestamp;
            this.promptTokens = prompt;
            this.completionTokens = completion;
            this.totalTokens = total;
            this.cached = cached;
        }
    }

    /** 一段时间内的统计汇总。 */
    public static class Stats {
        public int totalCalls;
        public int totalPromptTokens;
        public int totalCompletionTokens;
        public int totalTokens;
        public int cachedCalls;
    }

    /** 记录一次 API 调用。 */
    public static void record(Context context, int promptTokens, int completionTokens,
                              int totalTokens, boolean cached) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_RECORDS, "[]");
        List<Record> records = parseRecords(raw);

        records.add(new Record(System.currentTimeMillis(), promptTokens, completionTokens,
                totalTokens, cached));

        // 清理 30 天前的记录
        long cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        List<Record> cleaned = new ArrayList<>();
        for (Record r : records) {
            if (r.timestamp >= cutoff) {
                cleaned.add(r);
            }
        }

        sp.edit().putString(KEY_RECORDS, serialize(cleaned)).apply();
    }

    /** 查询指定时间范围内的统计。 */
    public static Stats query(Context context, long fromTimestamp) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_RECORDS, "[]");
        List<Record> records = parseRecords(raw);

        Stats s = new Stats();
        for (Record r : records) {
            if (r.timestamp >= fromTimestamp) {
                s.totalCalls++;
                s.totalPromptTokens += r.promptTokens;
                s.totalCompletionTokens += r.completionTokens;
                s.totalTokens += r.totalTokens;
                if (r.cached) s.cachedCalls++;
            }
        }
        return s;
    }

    // ---------- 序列化 ----------

    private static String serialize(List<Record> records) {
        JSONArray arr = new JSONArray();
        try {
            for (Record r : records) {
                JSONObject o = new JSONObject();
                o.put("ts", r.timestamp);
                o.put("p", r.promptTokens);
                o.put("c", r.completionTokens);
                o.put("t", r.totalTokens);
                o.put("cached", r.cached);
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
                        o.getLong("ts"),
                        o.optInt("p", 0),
                        o.optInt("c", 0),
                        o.optInt("t", 0),
                        o.optBoolean("cached", false)
                ));
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
