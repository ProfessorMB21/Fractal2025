import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import ru.gr05307.viewmodels.AppViewModel
import ru.gr05307.viewmodels.MainViewModel
import ru.gr05307.viewmodels.JuliaViewModel
import ru.gr05307.ui.PaintPanel
import ru.gr05307.ui.SelectionPanel
import ru.gr05307.ui.FractalMenu
import ru.gr05307.julia.ResizableJuliaPanel
import ru.gr05307.math.Complex

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
                modifier = Modifier.align(Alignment.TopStart)
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
