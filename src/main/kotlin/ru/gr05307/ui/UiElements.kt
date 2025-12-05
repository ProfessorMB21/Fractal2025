package ru.gr05307.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.launch
// Изменения от Артема этап 3
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun PaintPanel(
    modifier: Modifier = Modifier,
    onImageUpdate: (ImageBitmap)->Unit = {},
    onPaint: (DrawScope)->Unit = {},
    // Изменения от Артема
    onClick: (Offset)->Unit = {},
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    // Изменения от Артёма, этап 3
    Canvas(
        modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { pos ->
                        println("TAP $pos")
                        onClick(pos)
                    }
                )
            }
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
                scope.launch { onImageUpdate(graphicsLayer.toImageBitmap()) }
            }
    ) {
        onPaint(this)
    }
}

@Composable
fun SelectionPanel(
    offset: Offset,
    size: Size,
    modifier: Modifier = Modifier,
    onDragStart: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onPanStart: (Offset) -> Unit = {},
    onPanEnd: () -> Unit = {},
    onPan: (Offset) -> Unit = {},
){
    var dragButton by remember { mutableStateOf<PointerButton?>(null) }
    // Изменения Артема 7
    var isDragging by remember { mutableStateOf(false) }
    // Конец изменений

    Canvas(modifier = modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()


                when (event.type) {
                    PointerEventType.Press -> {
                        val buttons = event.buttons
                        dragButton = when {
                            buttons.isPrimaryPressed -> PointerButton.Primary
                            buttons.isSecondaryPressed -> PointerButton.Secondary
                            else -> null
                        }

                        val position = event.changes.first().position
                        when (dragButton) {
                            // Начало изменений Артема 7
                            /*PointerButton.Primary -> onDragStart(position)
                            PointerButton.Secondary -> onPanStart(position)
                            else -> {}*/
                            PointerButton.Primary -> {
                                isDragging = true
                                onDragStart(position)
                            }
                            PointerButton.Secondary -> {
                                isDragging = true
                                onPanStart(position)
                            }
                            else -> {}
                        }
                    }

                    PointerEventType.Move -> {
                        if (dragButton != null /*Изменения Артема 7 ->*/&& isDragging) {
                            val change = event.changes.first()
                            val dragAmount = change.position - change.previousPosition

                            when (dragButton) {
                                PointerButton.Primary -> onDrag(dragAmount)
                                PointerButton.Secondary -> onPan(dragAmount)
                                else -> {}
                            }
                            change.consume()
                        }
                    }

                    PointerEventType.Release -> {
                        if (dragButton != null /*Изменения Артема 7*/&& isDragging) {
                            when (dragButton) {
                                PointerButton.Primary -> onDragEnd()
                                PointerButton.Secondary -> onPanEnd()
                                else -> {}
                            }
                            dragButton = null
                            /*Изменения Артема 7*/ isDragging = false
                        }
                    }

                    // Изменения Артема 7
                    else -> {}
                }
            }
        }
    }){
        // Изменения Артема 7
        //this.drawRect(Color.Blue, offset, size, alpha = 0.2f)
        if (size.width > 0f && size.height > 0f) {
            this.drawRect(Color.Blue, offset, size, alpha = 0.2f)
        }
    }
}