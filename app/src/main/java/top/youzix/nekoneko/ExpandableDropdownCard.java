package top.youzix.nekoneko;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 可展开下拉选择卡片（ZL2 风格）。
 * <p>
 * 标题行显示当前选中文本和旋转箭头，点击后垂直展开/收起列表区域，
 * 列表项使用 RadioButton 单选，选中后自动收起。
 * <p>
 * 使用方式：
 * <pre>
 * dropdownCard.setHint("模型名称");
 * dropdownCard.setItems(modelNames, selectedId -> { ... });
 * dropdownCard.showLoading();
 * dropdownCard.setItems(modelNames, selectedId -> { ... });
 * </pre>
 */
public class ExpandableDropdownCard extends LinearLayout {

    // ---- 标题行 ----
    private TextView hintText;
    private TextView selectedText;
    private ImageView arrowIcon;
    private ProgressBar loadingIndicator;

    // ---- 展开区域 ----
    private View expandContainer;
    private RadioGroup radioGroup;

    // ---- 动画 ----
    private Animation expandAnim;
    private Animation shrinkAnim;
    private boolean isExpanded = false;
    private boolean isLoading = false;

    // ---- 数据 ----
    private String[] items;
    private OnItemSelectedListener listener;
    private Runnable onFirstClickAction;

    public interface OnItemSelectedListener {
        void onItemSelected(int position, String itemName);
    }

    public ExpandableDropdownCard(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ExpandableDropdownCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExpandableDropdownCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        setOrientation(VERTICAL);

        // 加载动画
        expandAnim = AnimationUtils.loadAnimation(context, R.anim.expand_vertically);
        shrinkAnim = AnimationUtils.loadAnimation(context, R.anim.shrink_vertically);

        // ---- 标题行 ----
        View titleRow = LayoutInflater.from(context).inflate(R.layout.dropdown_title_row, this, false);
        addView(titleRow);

        hintText = titleRow.findViewById(R.id.dropdown_hint);
        selectedText = titleRow.findViewById(R.id.dropdown_selected_text);
        arrowIcon = titleRow.findViewById(R.id.dropdown_arrow);
        loadingIndicator = titleRow.findViewById(R.id.dropdown_loading);

        // 标题行点击 → 展开/收起 或 首次点击触发加载
        OnClickListener toggleListener = v -> {
            if (isLoading) return;
            if (items == null || items.length == 0) {
                // 没有数据时执行首次点击回调（如获取模型列表）
                if (onFirstClickAction != null) {
                    onFirstClickAction.run();
                }
                return;
            }
            toggle();
        };
        titleRow.setOnClickListener(toggleListener);

        // ---- 展开容器 ----
        expandContainer = LayoutInflater.from(context).inflate(R.layout.dropdown_expand_content, this, false);
        addView(expandContainer);

        radioGroup = expandContainer.findViewById(R.id.dropdown_radio_group);
        expandContainer.setVisibility(GONE);
    }

    // ========== 公开 API ==========

    /**
     * 设置提示文本（未选择时显示）
     */
    public void setHint(String hint) {
        hintText.setText(hint);
    }

    /**
     * 设置首次点击时的回调（通常用于触发数据加载）。
     * 当没有已加载数据时，点击标题行会执行此回调而不是 toggle。
     */
    public void setOnFirstClickAction(@Nullable Runnable action) {
        this.onFirstClickAction = action;
    }

    /**
     * 设置列表数据并注册选中回调
     */
    public void setItems(String[] items, @Nullable OnItemSelectedListener listener) {
        this.items = items;
        this.listener = listener;
        this.isLoading = false;

        loadingIndicator.setVisibility(GONE);
        rebuildRadioGroup();
        updateTitleDisplay();
    }

    /**
     * 显示加载状态（获取模型中...）
     */
    public void showLoading() {
        isLoading = true;
        items = null;

        // 收起列表
        if (isExpanded) {
            collapse();
        }

        loadingIndicator.setVisibility(VISIBLE);
        arrowIcon.setVisibility(GONE);
        selectedText.setVisibility(GONE);
        hintText.setVisibility(VISIBLE);
        hintText.setText(getContext().getString(R.string.ai_loading_models));
    }

    /**
     * 显示错误状态
     */
    public void showError(String message) {
        isLoading = false;
        loadingIndicator.setVisibility(GONE);
        hintText.setVisibility(VISIBLE);
        hintText.setText(message);
        arrowIcon.setVisibility(items != null && items.length > 0 ? VISIBLE : GONE);
    }

    /**
     * 获取当前选中的索引，未选择返回 -1
     */
    public int getSelectedPosition() {
        if (radioGroup == null) return -1;
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == -1) return -1;
        return radioGroup.indexOfChild(radioGroup.findViewById(checkedId));
    }

    /**
     * 获取当前选中的文本
     */
    @Nullable
    public String getSelectedText() {
        int pos = getSelectedPosition();
        if (pos < 0 || items == null || pos >= items.length) return null;
        return items[pos];
    }

    /**
     * 手动展开
     */
    public void expand() {
        if (isExpanded || isLoading) return;
        isExpanded = true;

        expandContainer.setVisibility(VISIBLE);
        expandContainer.clearAnimation();
        expandContainer.startAnimation(expandAnim);

        // 箭头旋转 0 -> 180
        rotateArrow(0f, 180f);
    }

    /**
     * 手动收起
     */
    public void collapse() {
        if (!isExpanded) return;
        isExpanded = false;

        shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                expandContainer.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        expandContainer.clearAnimation();
        expandContainer.startAnimation(shrinkAnim);

        // 箭头旋转 180 -> 0
        rotateArrow(180f, 0f);
    }

    /**
     * 切换展开/收起状态
     */
    public void toggle() {
        if (isExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    // ========== 内部方法 ==========

    private void rebuildRadioGroup() {
        radioGroup.removeAllViews();

        if (items == null) return;

        for (int i = 0; i < items.length; i++) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(items[i]);
            rb.setId(View.generateViewId());
            rb.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
            rb.setTextSize(14f);

            final int position = i;
            rb.setOnClickListener(v -> {
                // 选中后回调
                if (listener != null) {
                    listener.onItemSelected(position, items[position]);
                }
                // 自动收起
                postDelayed(this::collapse, 150);
            });

            radioGroup.addView(rb);
        }
    }

    private void updateTitleDisplay() {
        if (items == null || items.length == 0) {
            hintText.setVisibility(VISIBLE);
            selectedText.setVisibility(GONE);
            arrowIcon.setVisibility(GONE);
            return;
        }

        int selected = getSelectedPosition();
        if (selected >= 0) {
            // 已选中
            hintText.setVisibility(GONE);
            selectedText.setVisibility(VISIBLE);
            selectedText.setText(items[selected]);
            arrowIcon.setVisibility(VISIBLE);
        } else {
            // 未选中
            hintText.setVisibility(VISIBLE);
            selectedText.setVisibility(GONE);
            arrowIcon.setVisibility(VISIBLE);
        }
    }

    private void rotateArrow(float from, float to) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(arrowIcon, "rotation", from, to);
        rotation.setDuration(300);
        rotation.start();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
