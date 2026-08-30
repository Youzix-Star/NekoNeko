package top.youzix.nekoneko;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.compose.ui.platform.ViewCompositionStrategy;
import androidx.fragment.app.Fragment;

import top.youzix.nekoneko.ui.NekoNekoThemeKt;
import top.youzix.nekoneko.ui.screens.AboutScreenKt;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ComposeView composeView = new ComposeView(requireContext());
        composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed);
        composeView.setContent(() -> {
            NekoNekoThemeKt.NekoNekoTheme(
                    false, false, () -> {
                        AboutScreenKt.AboutScreen();
                        return kotlin.Unit.INSTANCE;
                    });
            return kotlin.Unit.INSTANCE;
        });
        return composeView;
    }
}
