package de.clueventure.clue_venture

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformMap(
	modifier: Modifier = Modifier,
	routeTargets: List<GeoPoint> = emptyList(),
	onCurrentLocationChanged: (GeoPoint?) -> Unit = {},
	enablePointSelection: Boolean = false,
	selectedPoint: GeoPoint? = null,
	onMapPointSelected: (GeoPoint) -> Unit = {},
	showQuestionsButton: Boolean = false,
	questionCount: Int = 0,
	onQuestionsClicked: () -> Unit = {},
	onRouteDistanceChanged: (Double?) -> Unit = {},
)
