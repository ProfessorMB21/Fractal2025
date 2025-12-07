package ru.gr05307.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.gr05307.viewmodels.MainViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import ru.gr05307.serialization.FractalSerializer
import ru.gr05307.painting.*

@Composable
fun FractalMenu(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    isShowingTourControls: Boolean = false,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Кнопка меню
        Button(
            onClick = { menuOpen = !menuOpen },
            enabled = !isShowingTourControls
            ) {
            Text(if (menuOpen) "Закрыть меню" else "Меню")
        }

        AnimatedVisibility(
            visible = menuOpen,
            enter = slideInHorizontally(tween(300)) { -it },
            exit = slideOutHorizontally(tween(300)) { -it },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFEFEFEF))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { menuOpen = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Закрыть меню") }

                    Divider()
                    Text("Настройки", style = MaterialTheme.typography.h6)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.showJulia,
                            onCheckedChange = { value -> viewModel.setJuliaEnabled(value) }
                        )
                        Text("Отображать множества Жюлиа")
                    }

                    Divider()
                    Text("Фрактал:", style = MaterialTheme.typography.subtitle1)
                    Button(onClick = { viewModel.switchToMandelbrot() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Mandelbrot")
                    }
                    Button(onClick = { viewModel.switchToJulia() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Julia")
                    }
                    Button(onClick = { viewModel.switchToNewton() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Newton")
                    }

                    Divider()
                    Text("Цветовая схема:", style = MaterialTheme.typography.subtitle1)
                    Button(onClick = { viewModel.switchToRainbow() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Rainbow")
                    }
                    Button(onClick = { viewModel.switchToGrayscale() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Grayscale")
                    }
                    Button(onClick = { viewModel.switchToIce() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ice")
                    }
                    Button(onClick = { viewModel.switchToNewtonColor() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Newton Color")
                    }

                    Divider()

                    Text("Сохранение:", style = MaterialTheme.typography.subtitle1)
                    Button(
                        onClick = {
                            val dialog = FileDialog(null as Frame?, "Загрузить фрактал", FileDialog.LOAD)
                            dialog.isVisible = true
                            dialog.file?.let { fileName ->
                                val file = File(dialog.directory, fileName)
                                if (!file.exists()) return@let
                                val serializer = FractalSerializer()
                                val data = serializer.load(file.absolutePath)
                                viewModel.plain.xMin = data.plain.xMin
                                viewModel.plain.xMax = data.plain.xMax
                                viewModel.plain.yMin = data.plain.yMin
                                viewModel.plain.yMax = data.plain.yMax
                                when (data.fractalType) {
                                    "mandelbrot" -> viewModel.switchToMandelbrot()
                                    "julia" -> viewModel.switchToJulia()
                                    "newton" -> viewModel.switchToNewton()
                                }
                                when (data.colorType) {
                                    "rainbow" -> viewModel.switchToRainbow()
                                    "grayscale" -> viewModel.switchToGrayscale()
                                    "ice" -> viewModel.switchToIce()
                                    "newtonColor" -> viewModel.switchToNewtonColor()
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Загрузить JSON") }

                    Button(
                        onClick = {
                            val dialog = FileDialog(null as Frame?, "Сохранить фрактал", FileDialog.SAVE)
                            dialog.file = "fractal.json"
                            dialog.isVisible = true
                            val directory = dialog.directory
                            val name = dialog.file
                            if (directory != null && name != null) {
                                val file = File(directory, name)
                                val serializer = FractalSerializer()
                                val frac = when (viewModel.currentFractalFuncPublic) {
                                    mandelbrotFunc -> "mandelbrot"
                                    juliaFunc -> "julia"
                                    newtonFunc -> "newton"
                                    else -> "mandelbrot"
                                }
                                val col = when (viewModel.currentColorFuncPublic) {
                                    rainbow -> "rainbow"
                                    grayscale -> "grayscale"
                                    iceGradient -> "ice"
                                    newtonColor -> "newtonColor"
                                    else -> "rainbow"
                                }
                                serializer.save(
                                    plain = viewModel.plain,
                                    fractalType = frac,
                                    colorType = col,
                                    filePath = file.absolutePath
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Сохранить JSON") }

                    Button(
                        onClick = {
                            val dialog = FileDialog(null as Frame?, "Сохранить изображение", FileDialog.SAVE)
                            dialog.file = "fractal.jpg"
                            dialog.isVisible = true
                            dialog.file?.let { name ->
                                val file = File(dialog.directory, name)
                                viewModel.saveFractalToJpg(file.absolutePath)
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Сохранить JPG") }
                }
            }
        }
    }
}
