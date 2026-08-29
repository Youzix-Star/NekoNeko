package com.youzix.nekoneko;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import android.widget.TextView;

/**
 * 首次启动引导（仿照 legado-with-MD3 的 WelcomeActivity 结构）：
 * 1. 隐私与许可  2. 无障碍服务  3. 悬浮窗权限  4. 主题色
 */
public class WelcomeActivity extends AppCompatActivity {

    private ViewPager pager;
    private LinearProgressIndicator progress;
    private TextView titleView;
    private TextView summaryView;
    private MaterialButton prevButton;
    private MaterialButton nextButton;

    private final int[] pages = {
            R.layout.welcome_page_privacy,
            R.layout.welcome_page_accessibility,
            R.layout.welcome_page_overlay,
            R.layout.welcome_page_theme
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 已完成的引导直接进入主界面
        if (Guide.isDone(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        pager = findViewById(R.id.welcome_pager);
        progress = findViewById(R.id.welcome_progress);
        titleView = findViewById(R.id.welcome_title);
        summaryView = findViewById(R.id.welcome_summary);
        prevButton = findViewById(R.id.welcome_prev);
        nextButton = findViewById(R.id.welcome_next);

        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return pages.length;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View page = getLayoutInflater().inflate(pages[position], container, false);
                container.addView(page);
                wirePageActions(page, position);
                return page;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateUi(position);
            }
        });

        prevButton.setOnClickListener(v -> {
            int current = pager.getCurrentItem();
            if (current > 0) {
                pager.setCurrentItem(current - 1);
            }
        });

        nextButton.setOnClickListener(v -> {
            int current = pager.getCurrentItem();
            if (current == 0) {
                // 阅读并同意
                pager.setCurrentItem(1);
            } else if (current == pages.length - 1) {
                finishSetup();
            } else {
                pager.setCurrentItem(current + 1);
            }
        });

        updateUi(0);
    }

    private void updateUi(int position) {
        progress.setProgressCompat((position * 100) / (pages.length - 1), true);
        switch (position) {
            case 0:
                titleView.setText(R.string.welcome_0_title);
                summaryView.setText(R.string.welcome_0_summary);
                nextButton.setText(R.string.welcome_agree);
                break;
            case 1:
                titleView.setText(R.string.welcome_1_title);
                summaryView.setText(R.string.welcome_1_summary);
                nextButton.setText(R.string.welcome_next);
                break;
            case 2:
                titleView.setText(R.string.welcome_2_title);
                summaryView.setText(R.string.welcome_2_summary);
                nextButton.setText(R.string.welcome_next);
                break;
            default:
                titleView.setText(R.string.welcome_3_title);
                summaryView.setText(R.string.welcome_3_summary);
                nextButton.setText(R.string.welcome_finish);
                break;
        }
        prevButton.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void wirePageActions(View page, int position) {
        if (position == 1) {
            page.findViewById(R.id.welcome_accessibility_button).setOnClickListener(v -> {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                Toast.makeText(this, R.string.accessibility_service_enabled, Toast.LENGTH_LONG).show();
            });
        } else if (position == 2) {
            page.findViewById(R.id.welcome_overlay_button).setOnClickListener(v -> {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:" + getPackageName())));
            });
        } else if (position == 3) {
            final int[] buttonIds = {
                    R.id.accent_default_button, R.id.accent_blue_button, R.id.accent_green_button,
                    R.id.accent_purple_button, R.id.accent_pink_button, R.id.accent_orange_button
            };
            final int[] accents = {
                    AccentTheme.DEFAULT, AccentTheme.BLUE, AccentTheme.GREEN,
                    AccentTheme.PURPLE, AccentTheme.PINK, AccentTheme.ORANGE
            };
            for (int i = 0; i < buttonIds.length; i++) {
                final int accent = accents[i];
                page.findViewById(buttonIds[i]).setOnClickListener(v -> {
                    AccentTheme.save(WelcomeActivity.this, accent);
                    Toast.makeText(WelcomeActivity.this, R.string.about_accent_applied, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void finishSetup() {
        Guide.markDone(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        int current = pager.getCurrentItem();
        if (current > 0) {
            pager.setCurrentItem(current - 1);
        } else {
            super.onBackPressed();
        }
    }
}
