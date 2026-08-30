package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 文本替换规则引擎：支持纯文本替换和正则替换两种模式，持久化到 SharedPreferences。
 *
 * 规则按顺序依次应用：第 1 条的输出作为第 2 条的输入，以此类推。
 * useRegex=false 时：简单字符串全局替换（查找→替换）。
 * useRegex=true 时：正则匹配 + 替换模板（$1、$2 引用捕获组）。
 *
 * 支持导入"查找=替换"纯文本格式（一行一条，默认非正则）。
 */
public class RuleManager {

    private static final String PREFS = "text_rules";
    private static final String KEY_RULES = "rules";
    private static final String KEY_PRESETS = "rule_presets";

    /** 单条规则。 */
    public static class Rule {
        public String name;
        public String pattern;       // 查找内容（纯文本或正则）
        public String replacement;   // 替换内容
        public boolean enabled;
        public boolean useRegex;     // false=纯文本替换, true=正则替换

        public Rule() {
            this.enabled = true;
            this.useRegex = false;
        }

        public Rule(String name, String pattern, String replacement, boolean enabled, boolean useRegex) {
            this.name = name;
            this.pattern = pattern;
            this.replacement = replacement;
            this.enabled = enabled;
            this.useRegex = useRegex;
        }
    }

    // ---------- 持久化 ----------

    private static JSONObject ruleToJson(Rule r) throws Exception {
        JSONObject o = new JSONObject();
        o.put("name", r.name == null ? "" : r.name);
        o.put("pattern", r.pattern == null ? "" : r.pattern);
        o.put("replacement", r.replacement == null ? "" : r.replacement);
        o.put("enabled", r.enabled);
        o.put("useRegex", r.useRegex);
        return o;
    }

    private static Rule jsonToRule(JSONObject o) {
        Rule r = new Rule();
        r.name = o.optString("name", "");
        r.pattern = o.optString("pattern", "");
        r.replacement = o.optString("replacement", "");
        r.enabled = o.optBoolean("enabled", true);
        r.useRegex = o.optBoolean("useRegex", false);
        return r;
    }

    /** 加载全部规则。 */
    public static List<Rule> load(Context context) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_RULES, "[]");
        List<Rule> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                list.add(jsonToRule(arr.getJSONObject(i)));
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
                arr.put(ruleToJson(r));
            }
        } catch (Exception ignored) {
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_RULES, arr.toString())
                .apply();
    }

    // ---------- 规则预设 ----------

    public static void savePreset(Context context, String name, List<Rule> rules) {
        JSONObject presets = getPresetsJson(context);
        JSONArray arr = new JSONArray();
        try {
            for (Rule r : rules) {
                arr.put(ruleToJson(r));
            }
            presets.put(name, arr);
            putPresetsJson(context, presets);
        } catch (Exception ignored) {
        }
    }

    public static List<Rule> loadPreset(Context context, String name) {
        JSONObject presets = getPresetsJson(context);
        JSONArray arr = presets.optJSONArray(name);
        List<Rule> list = new ArrayList<>();
        if (arr == null) return list;
        try {
            for (int i = 0; i < arr.length(); i++) {
                list.add(jsonToRule(arr.getJSONObject(i)));
            }
        } catch (Exception ignored) {
        }
        return list;
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

    public static List<String> getPresetNames(Context context) {
        List<String> names = new ArrayList<>();
        JSONObject presets = getPresetsJson(context);
        Iterator<String> it = presets.keys();
        while (it.hasNext()) {
            names.add(it.next());
        }
        Collections.sort(names);
        return names;
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

    // ---------- 导入纯文本格式 ----------

    /**
     * 解析"查找=替换"纯文本格式，每行一条规则。
     * 支持以下格式：
     *   查找=替换                          （纯文本，启用）
     *   # [正则] 查找=替换                  （正则，启用）
     *   # [已禁用] 查找=替换                （纯文本，禁用）
     *   # [已禁用] [正则] 查找=替换          （正则，禁用）
     * 空行跳过。不含 = 的行整行作为查找，替换为空。
     */
    public static List<Rule> importFromText(String text) {
        List<Rule> rules = new ArrayList<>();
        if (text == null) return rules;
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.replace("\r", "");
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            boolean useRegex = false;
            boolean enabled = true;

            // 解析标记前缀
            if (trimmed.startsWith("#")) {
                String content = trimmed.substring(1).trim();
                if (content.startsWith("[已禁用]") || content.startsWith("[禁用]")) {
                    enabled = false;
                    content = content.substring(content.indexOf(']') + 1).trim();
                }
                if (content.startsWith("[正则]") || content.startsWith("[regex]")) {
                    useRegex = true;
                    content = content.substring(content.indexOf(']') + 1).trim();
                }
                trimmed = content;
            }

            int eq = trimmed.indexOf('=');
            String find, replace;
            if (eq >= 0) {
                find = trimmed.substring(0, eq);
                replace = trimmed.substring(eq + 1);
            } else {
                find = trimmed;
                replace = "";
            }
            rules.add(new Rule(find, find, replace, enabled, useRegex));
        }
        return rules;
    }

    /**
     * 将规则列表导出为"查找=替换"纯文本格式。
     * 仅导出非正则的规则；正则规则以 # 注释标记。
     */
    public static String exportToText(List<Rule> rules) {
        StringBuilder sb = new StringBuilder();
        for (Rule r : rules) {
            if (!r.enabled) {
                sb.append("# [已禁用] ");
            }
            if (r.useRegex) {
                sb.append("# [正则] ");
            }
            sb.append(r.pattern).append("=").append(r.replacement).append("\n");
        }
        return sb.toString();
    }

    // ---------- 规则引擎 ----------

    /**
     * 对 text 依次应用所有启用的规则，返回最终文本。
     */
    public static String applyRules(String text, List<Rule> rules) {
        if (text == null || text.isEmpty() || rules == null) {
            return text;
        }
        String result = text;
        for (Rule r : rules) {
            if (!r.enabled || r.pattern == null || r.pattern.isEmpty()) {
                continue;
            }
            if (r.useRegex) {
                // 正则模式
                try {
                    Pattern p = Pattern.compile(r.pattern);
                    Matcher m = p.matcher(result);
                    if (m.find()) {
                        m.reset();
                        String newResult = m.replaceAll(r.replacement);
                        Logger.d("规则「" + r.name + "」(正则) 已应用: "
                                + truncate(result, 40) + " → " + truncate(newResult, 40));
                        result = newResult;
                    }
                } catch (PatternSyntaxException e) {
                    Logger.w("规则「" + r.name + "」正则语法错误: " + e.getMessage());
                }
            } else {
                // 纯文本模式：全局替换
                String newResult = result.replace(r.pattern, r.replacement);
                if (!newResult.equals(result)) {
                    Logger.d("规则「" + r.name + "」(文本) 已应用: "
                            + truncate(result, 40) + " → " + truncate(newResult, 40));
                    result = newResult;
                }
            }
        }
        return result;
    }

    /** 验证正则表达式是否合法。 */
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
