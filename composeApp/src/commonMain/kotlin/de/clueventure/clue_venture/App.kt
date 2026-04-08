package de.clueventure.clue_venture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private object BottomBarIcons {
    val Left: ImageVector by lazy {
        ImageVector.Builder(
            name = "LeftMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 5f)
                lineTo(21f, 5f)
                lineTo(21f, 7f)
                lineTo(3f, 7f)
                close()
                moveTo(3f, 11f)
                lineTo(17f, 11f)
                lineTo(17f, 13f)
                lineTo(3f, 13f)
                close()
                moveTo(3f, 17f)
                lineTo(21f, 17f)
                lineTo(21f, 19f)
                lineTo(3f, 19f)
                close()
            }
        }.build()
    }

    val Map: ImageVector by lazy {
        ImageVector.Builder(
            name = "Map",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 5f)
                lineTo(8f, 3f)
                lineTo(15f, 6f)
                lineTo(21f, 4f)
                lineTo(21f, 19f)
                lineTo(16f, 21f)
                lineTo(9f, 18f)
                lineTo(3f, 20f)
                close()
                moveTo(9f, 6f)
                lineTo(9f, 15f)
                lineTo(15f, 18f)
                lineTo(15f, 9f)
                close()
            }
        }.build()
    }

    val Right: ImageVector by lazy {
        ImageVector.Builder(
            name = "RightSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f)
                lineTo(20f, 6f)
                lineTo(20f, 8f)
                lineTo(4f, 8f)
                close()
                moveTo(9f, 4f)
                lineTo(11f, 4f)
                lineTo(11f, 10f)
                lineTo(9f, 10f)
                close()
                moveTo(4f, 11f)
                lineTo(20f, 11f)
                lineTo(20f, 13f)
                lineTo(4f, 13f)
                close()
                moveTo(14f, 9f)
                lineTo(16f, 9f)
                lineTo(16f, 15f)
                lineTo(14f, 15f)
                close()
                moveTo(4f, 16f)
                lineTo(20f, 16f)
                lineTo(20f, 18f)
                lineTo(4f, 18f)
                close()
                moveTo(7f, 14f)
                lineTo(9f, 14f)
                lineTo(9f, 20f)
                lineTo(7f, 20f)
                close()
            }
        }.build()
    }
}

private enum class BottomTab(
    val icon: ImageVector,
    val contentDescription: String,
) {
    Left(icon = BottomBarIcons.Left, contentDescription = "Menue links"),
    Map(icon = BottomBarIcons.Map, contentDescription = "Karte"),
    Right(icon = BottomBarIcons.Right, contentDescription = "Menue rechts"),
}

@Composable
@Preview
fun App() {
    var selectedTab by remember { mutableStateOf(BottomTab.Map) }

    MaterialTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.height(64.dp),
                ) {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.contentDescription,
                                )
                            },
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    BottomTab.Map -> PlatformMap(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )

                    BottomTab.Left,
                    BottomTab.Right -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        PlaceholderContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderContent() {
    Box(modifier = Modifier.fillMaxSize())
}

