package com.youzix.nekoneko;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

import java.util.HashMap;
import java.util.Map;

/**
 * 宿主 Activity：底部导航栏（Material 3 NavigationBar）+ 三个页面
 * （首页 / AI 配置 / 关于），Fragment 用 show/hide 切换以保留状态。
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "home";
    private static final String TAG_CONFIG = "config";
    private static final String TAG_ABOUT = "about";

    private final Map<String, Fragment> fragments = new HashMap<>();
    private String currentTag = TAG_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 12+：显式应用莫奈动态取色（官方 API，最可靠路径，
        // 与 values-v31 的动态色主题双保险）
        DynamicColors.applyToActivityIfAvailable(this);

        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            switchTo(TAG_HOME, R.id.nav_home, false);
        } else {
            currentTag = savedInstanceState.getString("currentTag", TAG_HOME);
            nav.setSelectedItemId(itemIdOf(currentTag));
        }

        nav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home && !TAG_HOME.equals(currentTag)) {
                    switchTo(TAG_HOME, id, false);
                    return true;
                }
                if (id == R.id.nav_config && !TAG_CONFIG.equals(currentTag)) {
                    switchTo(TAG_CONFIG, id, false);
                    return true;
                }
                if (id == R.id.nav_about && !TAG_ABOUT.equals(currentTag)) {
                    switchTo(TAG_ABOUT, id, false);
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTag", currentTag);
    }

    /** 供首页"AI 配置"按钮跳转到配置页。 */
    public void selectTab(int menuItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(menuItemId);
    }

    private void switchTo(String tag, int menuItemId, boolean force) {
        if (!force && tag.equals(currentTag)) {
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();

        for (String t : new String[]{TAG_HOME, TAG_CONFIG, TAG_ABOUT}) {
            Fragment f = fm.findFragmentByTag(t);
            if (f != null && !t.equals(tag)) {
                tx.hide(f);
            }
        }

        Fragment target = fragments.get(tag);
        if (target == null) {
            target = createFragment(menuItemId);
            fragments.put(tag, target);
            tx.add(R.id.fragment_container, target, tag);
        } else {
            tx.show(target);
        }
        tx.commit();
        currentTag = tag;
    }

    private Fragment createFragment(int menuItemId) {
        if (menuItemId == R.id.nav_config) {
            return new AiConfigFragment();
        }
        if (menuItemId == R.id.nav_about) {
            return new AboutFragment();
        }
        return new HomeFragment();
    }

    private int itemIdOf(String tag) {
        if (TAG_CONFIG.equals(tag)) {
            return R.id.nav_config;
        }
        if (TAG_ABOUT.equals(tag)) {
            return R.id.nav_about;
        }
        return R.id.nav_home;
    }
}
