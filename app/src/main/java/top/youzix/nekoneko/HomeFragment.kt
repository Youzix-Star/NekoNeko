package top.youzix.nekoneko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import top.youzix.nekoneko.ui.screens.HomeScreen
import top.youzix.nekoneko.ui.theme.NekoNekoTheme

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            NekoNekoTheme {
                HomeScreen()
            }
        }
    }
}
