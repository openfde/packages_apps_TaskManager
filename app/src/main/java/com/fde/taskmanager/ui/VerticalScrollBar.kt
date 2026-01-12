package com.fde.taskmanager.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.ranges.coerceIn
import kotlin.ranges.rangeTo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerEventType


@Composable
fun VerticalScrollBar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    // 自定义滚动条
    val scrollbarWidth = 16.dp
    val scrollbarColor = Color(0xFF999999)
    val hoverColor: Color = Color(0x40000000)
    val isDragging = remember { mutableStateOf(false) }
    val dragOffset = remember { mutableStateOf(0f) }
    val containerHeight = remember { mutableStateOf(0f) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(scrollbarWidth)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val totalItems = listState.layoutInfo.totalItemsCount
                            if (totalItems == 0) return@detectDragGestures

                            val thumbTop = thumbTopOffset(listState, containerHeight.value)
                            val thumbBottom =
                                thumbTop + thumbHeight(listState, containerHeight.value)
                            if (offset.y in thumbTop..thumbBottom) {
                                isDragging.value = true
                                dragOffset.value = offset.y - thumbTop
                            }
                        },
                        onDrag = { change, _ ->
                            if (isDragging.value) {
                                val totalItems = listState.layoutInfo.totalItemsCount
                                val visibleItems = listState.layoutInfo.visibleItemsInfo.size
                                if (totalItems <= visibleItems) return@detectDragGestures

                                val newThumbTop = change.position.y - dragOffset.value
                                val fraction =
                                    (newThumbTop / max(
                                        1f,
                                        containerHeight.value - thumbHeight(
                                            listState,
                                            containerHeight.value
                                        )
                                    ))
                                        .coerceIn(0f, 1f)

                                val targetIndex = (fraction * (totalItems - visibleItems)).toInt()

                                coroutineScope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        },
                        onDragEnd = { isDragging.value = false },
                        onDragCancel = { isDragging.value = false }
                    )
                }
                .onSizeChanged { containerHeight.value = it.height.toFloat() }
        ) {
            val thumbH = thumbHeight(listState, size.height.toFloat())
            val thumbTop = thumbTopOffset(listState, size.height.toFloat())
            drawRoundRect(
                color = if (isDragging.value) hoverColor else scrollbarColor,
                topLeft = Offset(0f, thumbTop),
                size = Size(size.width, thumbH),
                cornerRadius = CornerRadius(0f, 0f)
            )
        }
    }
}


private fun thumbHeight(listState: LazyListState, containerHeight: Float): Float {
    val totalItems = listState.layoutInfo.totalItemsCount
    val visibleItems = listState.layoutInfo.visibleItemsInfo.size
    if (totalItems == 0) return 48f
    val fractionVisible = visibleItems.toFloat() / totalItems
    val minHeight = 48f
    return max(minHeight, fractionVisible * containerHeight)
}

private fun thumbTopOffset(listState: LazyListState, containerHeight: Float): Float {
    val totalItems = listState.layoutInfo.totalItemsCount
    val visibleItems = listState.layoutInfo.visibleItemsInfo.size
    if (totalItems == 0) return 0f
    val scrollFraction = listState.firstVisibleItemIndex.toFloat() / (totalItems - visibleItems)
    return scrollFraction * (containerHeight - thumbHeight(listState, containerHeight))
}


