package com.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 文本替换规则引擎：正则匹配 + 替换模板，持久化到 SharedPreferences。
 *
 * 规则按顺序依次应用：第 1 条的输出作为第 2 条的输入，以此类推。
 * 替换模板中 $1、$2 … 引用正则捕获组；$0 引用整个匹配。
 */
public class RuleManager {

    private static final String PREFS = "text_rules";
    private static final String KEY_RULES = "rules";

    /** 单条规则。 */
    public static class Rule {
        public String name;
        public String pattern;       // 正则表达式
        public String replacement;   // 替换模板（支持 $1 等）
        public boolean enabled;

        public Rule() {
            this.enabled = true;
        }

        public Rule(String name, String pattern, String replacement, boolean enabled) {
            this.name = name;
            this.pattern = pattern;
            this.replacement = replacement;
            this.enabled = enabled;
        }
    }

    // ---------- 持久化 ----------

    /** 加载全部规则。 */
    public static List<Rule> load(Context context) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_RULES, "[]");
        List<Rule> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Rule r = new Rule();
                r.name = o.optString("name", "");
                r.pattern = o.optString("pattern", "");
                r.replacement = o.optString("replacement", "");
                r.enabled = o.optBoolean("enabled", true);
                list.add(r);
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    /** 保存全部规则。 */
    public static void save(Context context, List<Rule> rules) {
        JSONArray arr = new JSONArray();
        try {
            for (Rule r : rules) {
                JSONObject o = new JSONObject();
                o.put("name", r.name == null ? "" : r.name);
                o.put("pattern", r.pattern == null ? "" : r.pattern);
                o.put("replacement", r.replacement == null ? "" : r.replacement);
                o.put("enabled", r.enabled);
                arr.put(o);
            }
        } catch (Exception ignored) {
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_RULES, arr.toString())
                .apply();
    }

    // ---------- 规则引擎 ----------

    /**
     * 对 text 依次应用所有启用的规则，返回最终文本。
     * 如果某条规则的正则语法错误，跳过该条并在 Logger 中记录警告。
     */
    public static String applyRules(String text, List<Rule> rules) {
        if (text == null || text.isEmpty() || rules == null) {
            return text;
        }
        String result = text;
        for (Rule r : rules) {
            if (!r.enabled) {
                continue;
            }
            if (r.pattern == null || r.pattern.isEmpty()) {
                continue;
            }
            try {
                Pattern p = Pattern.compile(r.pattern);
                Matcher m = p.matcher(result);
                if (m.find()) {
                    m.reset();
                    String newResult = m.replaceAll(r.replacement);
                    Logger.d("规则「" + r.name + "」已应用: "
                            + truncate(result, 40) + " → " + truncate(newResult, 40));
                    result = newResult;
                }
            } catch (PatternSyntaxException e) {
                Logger.w("规则「" + r.name + "」正则语法错误: " + e.getMessage());
            }
        }
        return result;
    }

    /** 验证正则表达式是否合法。返回 true 表示合法。 */
    public static boolean isValidPattern(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
