import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.gr05307.julia.ResizableJuliaPanel
import ru.gr05307.math.Complex
import ru.gr05307.ui.FractalMenu
import ru.gr05307.ui.PaintPanel
import ru.gr05307.ui.SelectionPanel
import ru.gr05307.viewmodels.AppViewModel
import ru.gr05307.viewmodels.JuliaViewModel
import ru.gr05307.viewmodels.MainViewModel


@Composable
@Preview
fun App() {
    val viewModel = remember { AppViewModel() }

    MaterialTheme {
        FractalApp(viewModel)
    }
}

@Composable
fun FractalApp(viewModel: AppViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        MainFractalView(
            viewModel = viewModel.mainViewModel,
            modifier = Modifier
                .fillMaxHeight()
                .weight(7f)
        )

        JuliaSidePanel(
            viewModel = viewModel.juliaViewModel,
            modifier = Modifier
                .fillMaxHeight()
                .weight(3f)
        )
    }
}

@Composable
fun MainFractalView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        PaintPanel(
            modifier = Modifier.fillMaxSize(),
            onImageUpdate = { image -> viewModel.onImageUpdate(image) },
            onPaint = { scope -> viewModel.paint(scope) }
        )

        SelectionPanel(
            viewModel.selectionOffset,
            viewModel.selectionSize,
            Modifier.fillMaxSize(),
            onClick = { pos -> viewModel.onPointClicked(pos.x, pos.y) },
            onDragStart = viewModel::onStartSelecting,
            onDragEnd = viewModel::onStopSelecting,
            onDrag = viewModel::onSelecting,
            onPan = viewModel::onPanning,
        )

        // Кнопки сверху, абсолютно позиционированные
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Меню слева
            FractalMenu(
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.TopStart),
                isShowingTourControls = viewModel.showTourControls
            )

            // Кнопка Назад справа
            Button(
                onClick = { viewModel.performUndo() },
                enabled = viewModel.canUndo(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text("Назад")
            }
        }
        // Tour control panel
        if (viewModel.showTourControls) {
            TourControlPanel(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .width(300.dp)
            )
        }

        // Floating action button to show/hide tour controls
        FloatingActionButton(
            onClick = { viewModel.toggleTourControls() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = if (viewModel.showTourControls) MaterialTheme.colors.primary else Color.Gray
        ) {
            Icon(
                if (viewModel.showTourControls) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = "Tour controls"
            )
        }
    }
}

// Custom stop icon during keyframe playback
@Composable
fun StopIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    isActive: Boolean = false,
    color: Color = Color.Black,
    padding: Float = 0.1f // Relative padding inside icon (0-0.5)
) {
    Canvas(
        modifier = modifier.size(iconSize)
    ) {
        val totalWidth = size.width
        val totalHeight = size.height

        // Calculate padding in pixels
        val paddingPx = totalWidth * padding

        // Draw square
        drawRect(
            color = color,
            alpha = if (!isActive) .5f else 1.0f,
            topLeft = Offset(x = paddingPx, y = paddingPx),
            size = Size(
                width = totalWidth - 2 * paddingPx,
                height = totalHeight - 2 * paddingPx
            )
        )
    }
}

@Composable
fun TourControlPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f)
    ) {}
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Face, contentDescription = "Tour", tint= MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Animation",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
        }

        Divider(modifier = Modifier.padding(8.dp))

        //
        Text("Keyframes:", style = MaterialTheme.typography.subtitle1)
        if (viewModel.tourKeyframes.isEmpty()) {
            Text(
                "No keyframes",
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .height(150.dp)
                    .padding(vertical = 4.dp)
            ) {
                items(viewModel.tourKeyframes.size) {index ->
                    val keyframe = viewModel.tourKeyframes[index]
                    keyframeItem(
                        keyframe = keyframe,
                        onGoto = { viewModel.goToKeyframe(keyframe) },
                        onDelete = { viewModel.removeKeyframe(keyframe.id) },
                        isPlaying = viewModel.isTourPlaying
                    )
                }
            }
        }

        // add keyframe button
        Button(
            onClick = { viewModel.addCurrentViewAsKeyframe()},
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isTourPlaying
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add current view")
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Tour controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Button(
                onClick = { viewModel.startTour() },
                enabled = viewModel.tourKeyframes.size > 1 && !viewModel.isTourPlaying,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
            }

            Button(
                onClick = { viewModel.stopTour() },
                enabled = viewModel.isTourPlaying,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                //Icon(Icons.Default.Close, contentDescription = "Stop")
                StopIcon(modifier = Modifier, 18.dp)
            }
        }

        // progress indicator
        if (viewModel.isTourPlaying) {
            LinearProgressIndicator(
                progress = viewModel.tourProgress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.primary
            )
            Text(
                "Frame: ${viewModel.currentTourFrame}/${viewModel.totalTourFrames}",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun keyframeItem(
    keyframe: MainViewModel.TourKeyframe,
    onGoto: () -> Unit,
    onDelete: () -> Unit,
    isPlaying: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    keyframe.name,
                    style = MaterialTheme.typography.body2,
                    maxLines = 1
                )
                Text(
                    "X: [${String.format("%.3f", keyframe.xMin)}, ${String.format("%.3f", keyframe.xMax)}] " +
                            "Y: [${String.format("%.3f", keyframe.yMin)}, ${String.format("%.3f", keyframe.yMax)}]",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onGoto,
                enabled = !isPlaying
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Go to",
                    tint = MaterialTheme.colors.primary
                )
            }

            IconButton(
                onClick = onDelete,
                enabled = !isPlaying
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun JuliaSidePanel(
    viewModel: JuliaViewModel,
    modifier: Modifier = Modifier
) {
    val currentJuliaPoint = viewModel.currentJuliaPoint
    val showJuliaPanel = viewModel.showJuliaPanel

    AnimatedVisibility(
        visible = showJuliaPanel && currentJuliaPoint != null,
        enter = slideInHorizontally(animationSpec = tween(300)) { it },
        exit = slideOutHorizontally(animationSpec = tween(300)) { it },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White)
                .border(1.dp, Color.Gray)
        ) {
            PanelHeader(
                onClose = { viewModel.closeJuliaPanel() }
            )

            if (currentJuliaPoint != null) {
                PointInfoCard(currentJuliaPoint)
            }

            if (currentJuliaPoint != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    ResizableJuliaPanel(
                        c = currentJuliaPoint,
                        onClose = { viewModel.closeJuliaPanel() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PanelHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Множество Жюлиа",
            color = Color.White,
            style = MaterialTheme.typography.h6
        )
        IconButton(
            onClick = onClose
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Закрыть",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PointInfoCard(c: Complex) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Выбранная точка:",
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "c = ${"%.6f".format(c.re)} + ${"%.6f".format(c.im)}i",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

fun main(): Unit = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Фрактал - 2025 (гр. 05-307)"
    ) {
        App()
    }
}
