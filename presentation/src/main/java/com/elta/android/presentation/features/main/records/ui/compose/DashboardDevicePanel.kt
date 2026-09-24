package com.elta.android.presentation.features.main.records.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elta.android.presentation.R
import kotlinx.coroutines.delay

@Composable
internal fun DashboardDevicePanel(
    device: DashboardDevice?, sensor: DashboardSensor?, syncState: DashboardSyncUiState,
    glucoseState: GlucoseState, onSync: () -> Unit, onConnect: () -> Unit,
    onDismissMessage: () -> Unit,
    onRetry: (DashboardSyncTarget) -> Unit, modifier: Modifier = Modifier,
    initialDetail: DashboardPanelDetail? = null,
    meterInfoSubtitle: String? = null
) {
    var showingConnection by rememberSaveable { mutableStateOf(false) }
    var detail by rememberSaveable(device?.address, sensor?.id) { mutableStateOf(initialDetail) }
    var inactiveDismissed by rememberSaveable(sensor?.id, sensor?.isActive) { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isPreview = LocalInspectionMode.current
    LaunchedEffect(device?.lastSyncAtMillis, sensor?.lastSyncAtMillis, isPreview) {
        if (isPreview) return@LaunchedEffect
        while (true) { now = System.currentTimeMillis(); delay(60_000L) }
    }
    if (!isPreview) BackHandler(detail != null) { detail = null }
    val accessibilityManager = LocalAccessibilityManager.current
    var presentedDetail by remember { mutableStateOf(initialDetail) }
    val slide = remember { Animatable(if (initialDetail == null) -1f else 0f) }
    LaunchedEffect(detail, device?.address, sensor?.id) {
        if (isPreview && initialDetail != null) return@LaunchedEffect
        if (detail != null) {
            presentedDetail = detail
            slide.snapTo(-1f)
            slide.animateTo(0f, tween(PanelSlideMillis, easing = FastOutSlowInEasing))
            delay(accessibilityManager?.calculateRecommendedTimeoutMillis(
                PanelDisplayMillis, containsIcons = true, containsText = true, containsControls = true
            ) ?: PanelDisplayMillis)
            detail = null
        } else {
            slide.animateTo(-1f, tween(PanelSlideMillis, easing = FastOutSlowInEasing))
            presentedDetail = null
        }
    }
    val displayedSensor = sensor?.takeUnless { !it.isActive && inactiveDismissed }
    val syncAt = listOfNotNull(device?.lastSyncAtMillis, displayedSensor?.lastSyncAtMillis).maxOrNull()
    val elapsed = lastSyncText(syncAt, now)
    val lastSync = if (device != null && displayedSensor != null && syncAt != null) {
        val source = if (syncAt == displayedSensor.lastSyncAtMillis) R.string.dashboard_sensor else R.string.dashboard_meter
        stringResource(R.string.dashboard_sync_source, elapsed, stringResource(source).lowercase())
    } else elapsed
    val actionColor = when (glucoseState) {
        GlucoseState.NORMAL -> Color(0xFF00837D)
        GlucoseState.HIGH -> Color(0xFFD66500)
        GlucoseState.LOW -> Color(0xFFB91400)
    }
    val infoColor = when (glucoseState) {
        GlucoseState.NORMAL -> Color(0xFF009B8D)
        GlucoseState.HIGH -> Color(0xFFDD8000)
        GlucoseState.LOW -> Color(0xFFBC1000)
    }
    val largeText = LocalDensity.current.fontScale > 1.3f
    val inactive = displayedSensor?.isActive == false
    val requestSync: () -> Unit = {
        if (device != null && displayedSensor != null) detail = DashboardPanelDetail.SYNC_CHOICE
        else if (device != null) onSync()
        else detail = DashboardPanelDetail.SENSOR
    }
    Column(modifier.fillMaxWidth()) {
        if (device == null && displayedSensor == null) {
            // The visual pill is thin; its touch target remains at least 48dp high.
            Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { showingConnection = true }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.dashboard_connect_device), color = Color(0xFF16AE9B), fontSize = 16.sp, lineHeight = 20.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFEAF8F5), CircleShape).padding(horizontal = 12.dp, vertical = 7.dp))
            }
        } else {
            val panelContent: @Composable (DashboardPanelDetail?) -> Unit = { shownDetail ->
                val panelBackground = when {
                    inactive || shownDetail == DashboardPanelDetail.SYNC_CHOICE -> actionColor
                    shownDetail != null -> infoColor
                    else -> Color.White.copy(alpha = .18f)
                }
                Column(Modifier.fillMaxWidth().background(panelBackground, RoundedCornerShape(28.dp)).padding(horizontal = 4.dp)) {
                    val label: @Composable () -> Unit = {
                        when {
                            inactive -> PanelLabel(stringResource(R.string.dashboard_sensor_inactive), stringResource(R.string.dashboard_sensor_replace), compact = true)
                            shownDetail == DashboardPanelDetail.SYNC_CHOICE -> PanelLabel(stringResource(R.string.dashboard_sync_choose), compact = true)
                            shownDetail == DashboardPanelDetail.METER && device != null -> PanelLabel(stringResource(R.string.dashboard_meter),
                                meterInfoSubtitle ?: device.serialNumber?.takeIf(String::isNotBlank)?.let { stringResource(R.string.dashboard_serial_number, it) } ?: lastSync)
                            shownDetail == DashboardPanelDetail.SENSOR && displayedSensor != null -> PanelLabel(
                                stringResource(R.string.dashboard_sensor_resource, displayedSensor.remainingPercent.coerceIn(0, 100)),
                                pluralStringResource(R.plurals.dashboard_sensor_remaining_days, displayedSensor.remainingDays.coerceAtLeast(0), displayedSensor.remainingDays.coerceAtLeast(0)))
                            else -> PanelLabel(
                                when {
                                    syncState.isError -> stringResource(R.string.dashboard_sync_error_short)
                                    syncState.isSyncing -> stringResource(R.string.dashboard_sync)
                                    else -> syncState.statusMessage ?: stringResource(R.string.dashboard_sync)
                                },
                                when {
                                    syncState.isError -> stringResource(R.string.dashboard_sync_retry_hint)
                                    syncState.isSyncing -> stringResource(R.string.dashboard_sync_in_progress)
                                    else -> lastSync
                                }, compact = syncState.statusMessage != null
                            )
                        }
                    }
                    val labelClick: () -> Unit = {
                        if (shownDetail != null) detail = null
                        else if (syncState.isError) onRetry(syncState.retryTarget)
                        else if (syncState.statusMessage != null && !syncState.isSyncing) onDismissMessage()
                        else if (!syncState.isSyncing) requestSync()
                    }
                    if (largeText) Box(Modifier.fillMaxWidth().clickable(onClick = labelClick).padding(horizontal = 12.dp)) { label() }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (shownDetail != DashboardPanelDetail.SYNC_CHOICE) {
                            if (displayedSensor != null && shownDetail != DashboardPanelDetail.METER) DeviceAction(true, displayedSensor, Color.White) { detail = if (shownDetail == DashboardPanelDetail.SENSOR) null else DashboardPanelDetail.SENSOR }
                            if (device != null && shownDetail != DashboardPanelDetail.SENSOR) DeviceAction(false, null, Color.White) { detail = if (shownDetail == DashboardPanelDetail.METER) null else DashboardPanelDetail.METER }
                        }
                        if (largeText) Spacer(Modifier.weight(1f))
                        else Box(Modifier.weight(1f).clickable(onClick = labelClick).padding(horizontal = 4.dp)) { label() }
                        when {
                            inactive -> {
                                RoundAction(stringResource(R.string.dashboard_connect_device), actionColor, { showingConnection = true }, plus = true, compact = !largeText)
                                RoundAction(stringResource(R.string.dashboard_close), actionColor, { inactiveDismissed = true }, R.drawable.ic_dialog_close_white, Color(0xFFFF606B), compact = !largeText)
                            }
                            shownDetail == DashboardPanelDetail.SYNC_CHOICE -> {
                                if (displayedSensor != null) DeviceAction(true, null, actionColor, true) { detail = DashboardPanelDetail.SENSOR }
                                if (device != null) DeviceAction(false, null, actionColor, true) { detail = null; onSync() }
                            }
                            shownDetail == null -> RoundAction(stringResource(R.string.dashboard_sync), Color(0xFF45BCAA), { if (syncState.isError) onRetry(syncState.retryTarget) else requestSync() }, R.drawable.ic_refresh_2, enabled = !syncState.isSyncing, isRotating  = syncState.isSyncing)
                        }
                    }
            }
            }
            SlidingDevicePanel(
                activeDetail = if (inactive) null else presentedDetail,
                slideProgress = { slide.value },
                content = panelContent
            )
        }
    }
    if (showingConnection) DeviceConnectionSheet({ showingConnection = false }) { showingConnection = false; onConnect() }
}

@Composable
private fun PanelLabel(title: String, subtitle: String? = null, compact: Boolean = false) {
    Column(Modifier.padding(vertical = 9.dp)) {
        Text(title, color = Color.White, fontSize = if (compact) 12.sp else 14.sp, lineHeight = if (compact) 16.sp else 18.sp)
        if (subtitle != null) Text(subtitle, color = Color.White.copy(alpha = .85f), fontSize = if (compact) 10.sp else 11.sp, lineHeight = if (compact) 12.sp else 13.sp)
    }
}

@Composable
private fun DeviceAction(sensorIcon: Boolean, sensor: DashboardSensor?, tint: Color, filled: Boolean = false, onClick: () -> Unit) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    IconButton(onClick, Modifier.width(if (largeText) 48.dp else if (filled) 36.dp else 28.dp).height(48.dp)) {
        Box(Modifier.size(if (filled) 30.dp else 24.dp).then(if (filled) Modifier.background(Color(0xFFE8F7F3), CircleShape) else Modifier), contentAlignment = Alignment.Center) {
            DeviceGlyph(sensorIcon, tint, sensor, Modifier.size(24.dp))
        }
    }
}

@Composable
private fun DeviceGlyph(sensorIcon: Boolean, tint: Color, sensor: DashboardSensor? = null, modifier: Modifier = Modifier) {
    val description = stringResource(if (sensorIcon) R.string.dashboard_sensor else R.string.dashboard_meter)
    Canvas(modifier.semantics { contentDescription = description }) {
        val u = size.minDimension / 24f
        val stroke = Stroke(1.5f * u)
        if (sensorIcon) {
            drawCircle(if (sensor?.isActive == false) Color(0xFFFF606B) else tint.copy(alpha = .12f), 8f * u)
            drawCircle(tint, 8f * u, style = stroke)
            drawCircle(tint, 1.6f * u)
            if (sensor != null && sensor.isActive) drawArc(
                if (sensor.remainingPercent >= 90) Color(0xFF56FF83) else tint,
                -90f, 360f * sensor.remainingPercent.coerceIn(0, 100) / 100f, false,
                Offset(1f * u, 1f * u), Size(22f * u, 22f * u), style = Stroke(u))
        } else {
            drawRoundRect(tint, Offset(4f*u, 2f*u), Size(16f*u, 20f*u), CornerRadius(7f*u), style = stroke)
            drawRoundRect(tint, Offset(7f*u, 6f*u), Size(10f*u, 6f*u), CornerRadius(2f*u), style = stroke)
        }
    }
}

@Composable
private fun RoundAction(description: String, tint: Color, onClick: () -> Unit, icon: Int? = null,
    background: Color = Color(0xFFEAF8F5), plus: Boolean = false, enabled: Boolean = true, compact: Boolean = false, isRotating: Boolean = false) {
    val rotation = if (isRotating) {
        val transition = rememberInfiniteTransition(label = "sync")

        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "syncRotation"
        )

        angle
    } else {
        0f
    }
    IconButton(onClick, Modifier.width(if (compact) 32.dp else 48.dp).height(48.dp), enabled = enabled) {
        Box(Modifier.size(if (compact) 28.dp else 34.dp).background(background, CircleShape), contentAlignment = Alignment.Center) {
            if (plus) Text("+", color = tint, fontSize = if (compact) 23.sp else 27.sp, modifier = Modifier.semantics { contentDescription = description })
            else if (icon != null)
                Icon(
                    painter = painterResource(icon),
                    contentDescription = description,
                    modifier = Modifier
                        .size(if (compact) 22.dp else 26.dp)
                        .rotate(rotation),
                    tint = tint
                )
        }
    }
}

@Composable
private fun DeviceConnectionSheet(onDismiss: () -> Unit, onConnectMeter: () -> Unit) {
    var sensorSelected by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalDensity provides density) {
            BoxWithConstraints(Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.BottomCenter) {
                Box(Modifier.matchParentSize().clickable(onClick = onDismiss))
                Column(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 32.dp)
                    .padding(bottom = (maxHeight * .08f).coerceAtMost(64.dp))
                    .verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dashboard_connect_choose), color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
                    ConnectionOption(stringResource(R.string.dashboard_meter), false, onConnectMeter)
                    ConnectionOption(stringResource(R.string.dashboard_sensor), true) { sensorSelected = true }
                    if (sensorSelected) Text(stringResource(R.string.dashboard_sensor_unavailable), color = Color.White, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Button(onDismiss, Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE5E5E7), contentColor = Color(0xFF858B99)), elevation = null) {
                        Text(stringResource(R.string.dashboard_close))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionOption(label: String, sensor: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp)
        .background(Brush.horizontalGradient(listOf(Color(0xFF38AC99), Color(0xFF35AEBA))), RoundedCornerShape(8.dp))
        .clickable(role = Role.Button, onClick = onClick).padding(12.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        DeviceGlyph(sensor, Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun lastSyncText(timestamp: Long?, now: Long): String {
    val minutes = elapsedSyncMinutes(timestamp, now) ?: return stringResource(R.string.dashboard_sync_never)
    return when {
        minutes == 0L -> stringResource(R.string.dashboard_sync_now)
        minutes < 60 -> pluralStringResource(R.plurals.dashboard_elapsed_minutes, minutes.toInt(), minutes)
        minutes < 1440 -> pluralStringResource(R.plurals.dashboard_elapsed_hours, (minutes / 60).toInt(), minutes / 60)
        else -> pluralStringResource(R.plurals.dashboard_elapsed_days, (minutes / 1440).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), minutes / 1440)
    }
}

internal fun elapsedSyncMinutes(timestamp: Long?, now: Long): Long? =
    timestamp?.takeIf { it > 0 }?.let { ((now - it).coerceAtLeast(0)) / 60_000 }

internal enum class DashboardPanelDetail { SYNC_CHOICE, METER, SENSOR }

// Approximate timing from the observed prototype; exact Figma timing is unavailable.
private const val PanelSlideMillis = 350
private const val PanelDisplayMillis = 3_000L

/** All variants reserve the same height. Only the base and the moving overlay are placed. */
@Composable
private fun SlidingDevicePanel(
    activeDetail: DashboardPanelDetail?,
    slideProgress: () -> Float,
    content: @Composable (DashboardPanelDetail?) -> Unit
) {
    val states = listOf(null, DashboardPanelDetail.SYNC_CHOICE, DashboardPanelDetail.METER, DashboardPanelDetail.SENSOR)
    Layout(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)),
        content = {
            states.forEach { state ->
                Box(Modifier.fillMaxWidth().then(
                    if (state != activeDetail) Modifier.clearAndSetSemantics { } else Modifier
                )) { content(state) }
            }
        }
    ) { measurables, constraints ->
        val children = measurables.map { it.measure(Constraints.fixedWidth(constraints.maxWidth)) }
        val height = children.maxOf { it.height }
        layout(constraints.maxWidth, height) {
            children[0].placeRelative(0, (height - children[0].height) / 2)
            val index = states.indexOf(activeDetail)
            if (index > 0) {
                children[index].placeRelative(
                    (constraints.maxWidth * slideProgress()).toInt(),
                    (height - children[index].height) / 2
                )
            }
        }
    }
}
