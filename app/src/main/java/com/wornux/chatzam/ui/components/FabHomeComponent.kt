package com.wornux.chatzam.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.wornux.chatzam.R

class FabHomeComponent : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createComposeView()

    private fun createComposeView(): View =
        ComposeView(requireContext()).apply {
            setContent {
                FloatingActionMenu(findNavController())
            }
        }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    @Preview
    private fun FloatingActionMenu(navController: NavController? = null) {
        val listState = rememberLazyListState()
        val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
        val focusRequester = remember { FocusRequester() }
        var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

        HandleBackPress(fabMenuExpanded) { fabMenuExpanded = false }

        Box {
            FloatingActionButtonMenu(
                modifier = Modifier.align(Alignment.BottomEnd),
                expanded = fabMenuExpanded,
                button = {
                    CreateToggleButton(
                        fabMenuExpanded = fabMenuExpanded,
                        fabVisible = fabVisible,
                        focusRequester = focusRequester,
                        onToggle = { fabMenuExpanded = !fabMenuExpanded }
                    )
                }
            ) {
                getMenuItems().forEach { (icon, label) ->
                    FloatingActionButtonMenuItem(
                        onClick = {
                            handleMenuItemClick(navController, label)
                            fabMenuExpanded = false
                        },
                        icon = { 
                            Icon(
                                icon, 
                                contentDescription = null,
                                tint = Color(0xFF03DAC5)
                            )
                        },
                        text = { Text(text = label, color = Color.White) },
                        containerColor = Color(0xFF1E1E1E),

                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun CreateToggleButton(
        fabMenuExpanded: Boolean,
        fabVisible: Boolean,
        focusRequester: FocusRequester,
        onToggle: () -> Unit
    ) {
        ToggleFloatingActionButton(
            modifier = Modifier.toggleButtonModifier(
                fabMenuExpanded,
                fabVisible,
                focusRequester
            ),
            checked = fabMenuExpanded,
            onCheckedChange = { onToggle() },
            containerColor = { progress ->
                androidx.compose.ui.graphics.lerp(
                    Color(0xFF018786),
                    Color(0xFF03DAC5),
                    progress
                )
            }
        ) {
            val imageVector by remember {
                derivedStateOf {
                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                }
            }
            Icon(
                painter = rememberVectorPainter(imageVector),
                contentDescription = null,
                modifier = Modifier.animateIcon({ checkedProgress }),
                tint = Color.White
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    private fun Modifier.toggleButtonModifier(
        fabMenuExpanded: Boolean,
        fabVisible: Boolean,
        focusRequester: FocusRequester
    ): Modifier =
        this
            .semantics {
                traversalIndex = -1f
                stateDescription =
                    if (fabMenuExpanded) "Expanded" else "Collapsed"
                contentDescription = "Toggle menu"
            }
            .animateFloatingActionButton(
                visible = fabVisible || fabMenuExpanded,
                alignment = Alignment.BottomEnd
            )
            .focusRequester(focusRequester)

    @Composable
    private fun HandleBackPress(enabled: Boolean, onBack: () -> Unit) {
        BackHandler(enabled, onBack)
    }

    private fun getMenuItems(): List<Pair<ImageVector, String>> =
        listOf(
            Icons.Filled.People to "New group",
            Icons.Filled.Contacts to "New chat"
        )

    private fun handleMenuItemClick(
        navController: NavController?,
        label: String
    ) {
        when (label) {
            "New group" -> navController?.navigate(R.id.nav_group_creation)
            "New chat" -> navController?.navigate(R.id.nav_chat_creation)
        }
    }
}