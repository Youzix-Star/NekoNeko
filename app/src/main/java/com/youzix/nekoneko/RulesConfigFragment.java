package com.youzix.nekoneko;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文本规则配置页：正则表达式替换规则的增删改查与拖拽排序。
 * 规则按顺序依次应用：第 1 条的输出作为第 2 条的输入。
 */
public class RulesConfigFragment extends Fragment {

    private List<RuleManager.Rule> rules = new ArrayList<>();
    private RuleAdapter adapter;
    private TextView emptyHint;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rules_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyHint = view.findViewById(R.id.rules_empty_hint);
        recyclerView = view.findViewById(R.id.rules_recycler_view);

        // 加载规则
        rules = RuleManager.load(requireContext());

        // RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RuleAdapter();
        recyclerView.setAdapter(adapter);

        // 拖拽排序
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(rules, from, to);
                adapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                // 不支持滑动删除
            }
        });
        touchHelper.attachToRecyclerView(recyclerView);

        // 添加按钮
        view.findViewById(R.id.add_rule_button).setOnClickListener(v -> showRuleDialog(-1));

        // 保存按钮
        view.findViewById(R.id.save_rules_button).setOnClickListener(v -> saveRules());

        refreshEmptyState();
    }

    private void refreshEmptyState() {
        if (rules.isEmpty()) {
            emptyHint.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyHint.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void saveRules() {
        RuleManager.save(requireContext(), rules);
        Toast.makeText(requireContext(), R.string.rules_saved, Toast.LENGTH_SHORT).show();
    }

    /** 弹出规则编辑对话框。editIndex == -1 表示新建。 */
    private void showRuleDialog(int editIndex) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rule_edit, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.rule_name_input);
        TextInputEditText patternInput = dialogView.findViewById(R.id.rule_pattern_input);
        TextInputEditText replacementInput = dialogView.findViewById(R.id.rule_replacement_input);
        TextInputLayout patternLayout = dialogView.findViewById(R.id.rule_pattern_layout);

        // 预填（编辑模式）
        if (editIndex >= 0 && editIndex < rules.size()) {
            RuleManager.Rule r = rules.get(editIndex);
            nameInput.setText(r.name);
            patternInput.setText(r.pattern);
            replacementInput.setText(r.replacement);
        }

        // 实时校验正则
        patternInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.isEmpty()) {
                    patternLayout.setError(null);
                } else if (!RuleManager.isValidPattern(text)) {
                    patternLayout.setError(getString(R.string.rule_invalid_pattern));
                } else {
                    patternLayout.setError(null);
                }
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editIndex >= 0 ? R.string.rule_edit_title : R.string.rule_add_title)
                .setView(dialogView)
                .setPositiveButton(R.string.rules_confirm, (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    String pattern = patternInput.getText().toString().trim();
                    String replacement = replacementInput.getText().toString();

                    if (name.isEmpty()) {
                        name = pattern; // 用正则作为默认名称
                    }
                    if (pattern.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.rule_pattern_required,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!RuleManager.isValidPattern(pattern)) {
                        Toast.makeText(requireContext(), R.string.rule_invalid_pattern,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (editIndex >= 0) {
                        // 编辑
                        RuleManager.Rule r = rules.get(editIndex);
                        r.name = name;
                        r.pattern = pattern;
                        r.replacement = replacement;
                        adapter.notifyItemChanged(editIndex);
                    } else {
                        // 新建
                        rules.add(new RuleManager.Rule(name, pattern, replacement, true));
                        adapter.notifyItemInserted(rules.size() - 1);
                    }
                    refreshEmptyState();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** 删除规则。 */
    private void deleteRule(int index) {
        if (index < 0 || index >= rules.size()) return;
        String name = rules.get(index).name;
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.rule_confirm_delete, name))
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    rules.remove(index);
                    adapter.notifyItemRemoved(index);
                    refreshEmptyState();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ---------- Adapter ----------

    private class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rule, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            RuleManager.Rule r = rules.get(position);
            holder.nameText.setText(r.name);
            holder.patternText.setText("正则: " + r.pattern);
            holder.replacementText.setText("替换: " + r.replacement);
            holder.toggle.setChecked(r.enabled);

            holder.toggle.setOnCheckedChangeListener((btn, checked) -> {
                r.enabled = checked;
            });

            holder.editBtn.setOnClickListener(v -> showRuleDialog(holder.getAdapterPosition()));
            holder.deleteBtn.setOnClickListener(v -> deleteRule(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return rules.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView nameText, patternText, replacementText;
            com.google.android.material.materialswitch.MaterialSwitch toggle;
            View editBtn, deleteBtn;

            VH(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.rule_item_name);
                patternText = itemView.findViewById(R.id.rule_item_pattern);
                replacementText = itemView.findViewById(R.id.rule_item_replacement);
                toggle = itemView.findViewById(R.id.rule_item_switch);
                editBtn = itemView.findViewById(R.id.rule_item_edit);
                deleteBtn = itemView.findViewById(R.id.rule_item_delete);
            }
        }
    }
}
