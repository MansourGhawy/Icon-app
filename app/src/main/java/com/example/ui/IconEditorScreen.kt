package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackgroundType
import com.example.model.IconShape
import com.example.model.IconSettings
import com.example.model.AppInfo
import com.example.viewmodel.AppListViewModel

// Preset vibrant color options (custom hex values)
private val DESIGN_COLORS = listOf(
    0xFF4285F4.toInt() to "Cobalt",
    0xFF10A173.toInt() to "Teal",
    0xFF00E676.toInt() to "Mint",
    0xFFD500F9.toInt() to "Violet",
    0xFFFF3D00.toInt() to "Crimson",
    0xFFFFC400.toInt() to "Gold",
    0xFFE65100.toInt() to "Amber",
    0xFF00B0FF.toInt() to "Sky",
    0xFFFFFFFF.toInt() to "White",
    0xFF212121.toInt() to "Obsidian"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconEditorScreen(
    viewModel: AppListViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedApp by viewModel.selectedApp.collectAsState()
    val settings by viewModel.iconSettings.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()

    // Shortcut customize name state
    var customLabel by remember(selectedApp) { mutableStateOf(selectedApp?.appName ?: "") }
    var activeTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit Icon",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        selectedApp?.let {
                            Text(
                                text = customLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back backstack",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveShortcut(customLabel) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(38.dp)
                    ) {
                        Text(
                            text = "Create",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("editor_top_bar")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Live Preview Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .drawBehind {
                        // Background Glow Effect - absolute centered blur circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x333B82F6), Color.Transparent),
                                center = center,
                                radius = size.minDimension * 0.5f
                            ),
                            radius = size.minDimension * 0.5f,
                            center = center
                        )
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Modern Icon Frame (rounded-[40px] and shadow border logic)
                    Box(
                        modifier = Modifier
                            .size(192.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF2D2E30), Color(0xFF1A1C1E))
                                ),
                                shape = RoundedCornerShape(40.dp)
                            )
                            .border(1.dp, Color(0x1BFFFFFF), RoundedCornerShape(40.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "Active customization preview output",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("preview_image"),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "LIVE PREVIEW SYSTEM",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Quick Shortcut Name Field
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shortcut Label Name",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        singleLine = true,
                        placeholder = { Text("Enter launcher shortcut name", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("custom_label_input")
                    )
                }
            }

            // Tab Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val headers = listOf("Shape", "Colors", "Sliders", "Filters", "Borders")
                headers.forEachIndexed { index, header ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { activeTab = index }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .testTag("editor_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = header,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Tab Content Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    0 -> ShapeEditorTab(settings, onSettingsChanged = { viewModel.updateSettings(it) })
                    1 -> ColorsEditorTab(settings, onSettingsChanged = { viewModel.updateSettings(it) })
                    2 -> SlidersEditorTab(settings, onSettingsChanged = { viewModel.updateSettings(it) })
                    3 -> FiltersEditorTab(settings, onSettingsChanged = { viewModel.updateSettings(it) })
                    4 -> BordersEditorTab(settings, onSettingsChanged = { viewModel.updateSettings(it) })
                }
            }

            // Master Submit Button (creates pinned shortcut)
            Button(
                onClick = { viewModel.saveShortcut(customLabel) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .height(56.dp)
                    .testTag("save_shortcut_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon", tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Custom Shortcut to Launcher",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// TAB 1: SHAPES
@Composable
fun ShapeEditorTab(
    settings: IconSettings,
    onSettingsChanged: (IconSettings) -> Unit
) {
    Column {
        Text(
            text = "Adaptive Masking Shape",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        val shapes = listOf(
            IconShape.ROUNDED_RECT to "Rounded Card",
            IconShape.CIRCLE to "Circle Circle",
            IconShape.SQUIRCLE to "Squircle Pure",
            IconShape.HEXAGON to "Hexagon Hex",
            IconShape.OCTAGON to "Octagon Oct",
            IconShape.SQUARE to "Plain Square"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shapes.forEach { (shape, name) ->
                val isSelected = settings.shape == shape
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSettingsChanged(settings.copy(shape = shape)) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (shape) {
                                IconShape.CIRCLE -> Icons.Default.Circle
                                IconShape.SQUARE -> Icons.Default.CropSquare
                                IconShape.ROUNDED_RECT -> Icons.Default.RoundedCorner
                                IconShape.HEXAGON -> Icons.Default.Hexagon
                                IconShape.OCTAGON -> Icons.Default.Pentagon // Pentagonal approximation vector
                                IconShape.SQUIRCLE -> Icons.Default.PlayForWork
                            },
                            contentDescription = name,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name.split(" ")[0],
                            fontSize = 11.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// TAB 2: BG & COLOR GRADIENTS
@Composable
fun ColorsEditorTab(
    settings: IconSettings,
    onSettingsChanged: (IconSettings) -> Unit
) {
    Column {
        Text(
            text = "Background Strategy",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Background type selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bgTypes = listOf(
                BackgroundType.SOLID to "Solid Color",
                BackgroundType.LINEAR_GRADIENT to "Linear Span",
                BackgroundType.RADIAL_GRADIENT to "Radial Core",
                BackgroundType.TRANSPARENT to "Transparent"
            )

            bgTypes.forEach { (type, label) ->
                val isSelected = settings.bgType == type
                Button(
                    onClick = { onSettingsChanged(settings.copy(bgType = type)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp).border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = label.split(" ")[0],
                        fontSize = 11.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (settings.bgType != BackgroundType.TRANSPARENT) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Primary Color Selection (Base Accent)",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DESIGN_COLORS.forEach { (colorVal, name) ->
                    val isSelected = settings.bgColor1 == colorVal
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0xFF2E2E2E),
                                shape = CircleShape
                            )
                            .clickable { onSettingsChanged(settings.copy(bgColor1 = colorVal)) }
                    )
                }
            }

            if (settings.bgType == BackgroundType.LINEAR_GRADIENT || settings.bgType == BackgroundType.RADIAL_GRADIENT) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Secondary Gradient Color Selection",
                    color = Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DESIGN_COLORS.forEach { (colorVal, name) ->
                        val isSelected = settings.bgColor2 == colorVal
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0xFF2E2E2E),
                                    shape = CircleShape
                                )
                                .clickable { onSettingsChanged(settings.copy(bgColor2 = colorVal)) }
                        )
                    }
                }
            }
        }
    }
}

// TAB 3: DIMENSIONS (SCALE & TRANSLATION)
@Composable
fun SlidersEditorTab(
    settings: IconSettings,
    onSettingsChanged: (IconSettings) -> Unit
) {
    Column {
        Text(
            text = "Inner Icon Dimensions",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Scale Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Icon Scaling", color = Color.Gray, fontSize = 13.sp)
                Text(
                    text = "${(settings.scale * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = settings.scale,
                onValueChange = { onSettingsChanged(settings.copy(scale = it)) },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    thumbColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("scale_slider")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Offset X
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Translation X Offset", color = Color.Gray, fontSize = 13.sp)
                Text(
                    text = "${settings.offsetX.toInt()} px",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = settings.offsetX,
                onValueChange = { onSettingsChanged(settings.copy(offsetX = it)) },
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    thumbColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("offset_x_slider")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Offset Y
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Translation Y Offset", color = Color.Gray, fontSize = 13.sp)
                Text(
                    text = "${settings.offsetY.toInt()} px",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = settings.offsetY,
                onValueChange = { onSettingsChanged(settings.copy(offsetY = it)) },
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    thumbColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("offset_y_slider")
            )
        }
    }
}

// TAB 4: FILTERS & COLOR MATRIX
@Composable
fun FiltersEditorTab(
    settings: IconSettings,
    onSettingsChanged: (IconSettings) -> Unit
) {
    Column {
        Text(
            text = "Professional Image Filters",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Hue
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hue Rotation", color = Color.Gray, fontSize = 13.sp)
                Text("${settings.hue.toInt()}°", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.hue,
                onValueChange = { onSettingsChanged(settings.copy(hue = it)) },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("hue_slider")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Saturation
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Color Saturation", color = Color.Gray, fontSize = 13.sp)
                Text("${(settings.saturation * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.saturation,
                onValueChange = { onSettingsChanged(settings.copy(saturation = it)) },
                valueRange = 0.0f..2.0f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("saturation_slider")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Brightness
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Brightness Adjustment", color = Color.Gray, fontSize = 13.sp)
                Text("${settings.brightness.toInt()}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.brightness,
                onValueChange = { onSettingsChanged(settings.copy(brightness = it)) },
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("brightness_slider")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Contrast
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Contrast Scaling", color = Color.Gray, fontSize = 13.sp)
                Text("${(settings.contrast * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.contrast,
                onValueChange = { onSettingsChanged(settings.copy(contrast = it)) },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("contrast_slider")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Soft Blur
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gaussian Software Blur", color = Color.Gray, fontSize = 13.sp)
                Text("Radius: ${settings.blurRadius} px", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.blurRadius.toFloat(),
                onValueChange = { onSettingsChanged(settings.copy(blurRadius = it.toInt())) },
                valueRange = 0f..20f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("blur_slider")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checked Filters Checkboxes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Invert Color Palette", color = Color.Gray, fontSize = 14.sp)
                Switch(
                    checked = settings.invert,
                    onCheckedChange = { onSettingsChanged(settings.copy(invert = it)) },
                    modifier = Modifier.testTag("filter_invert_switch")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grayscale Mono Accent", color = Color.Gray, fontSize = 14.sp)
                Switch(
                    checked = settings.grayscale,
                    onCheckedChange = { onSettingsChanged(settings.copy(grayscale = it)) },
                    modifier = Modifier.testTag("filter_grayscale_switch")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rustic Sepia Tone", color = Color.Gray, fontSize = 14.sp)
                Switch(
                    checked = settings.sepia,
                    onCheckedChange = { onSettingsChanged(settings.copy(sepia = it)) },
                    modifier = Modifier.testTag("filter_sepia_switch")
                )
            }
        }
    }
}

// TAB 5: BORDERS & SHADOWS
@Composable
fun BordersEditorTab(
    settings: IconSettings,
    onSettingsChanged: (IconSettings) -> Unit
) {
    Column {
        Text(
            text = "Fine Borders & Shadow Effects",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Border Width Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Border Outline Thickness", color = Color.Gray, fontSize = 13.sp)
                Text("${settings.borderWidth.toInt()} dp", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.borderWidth,
                onValueChange = { onSettingsChanged(settings.copy(borderWidth = it)) },
                valueRange = 0f..16f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("border_width_slider")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Border Color Picker If border exists
        if (settings.borderWidth > 0f) {
            Text(
                text = "Border Outline Color",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DESIGN_COLORS.forEach { (colorVal, name) ->
                    val isSelected = settings.borderColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0xFF2E2E2E),
                                shape = CircleShape
                            )
                            .clickable { onSettingsChanged(settings.copy(borderColor = colorVal)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Shadow Radius Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Adaptive Dropshadow Radius", color = Color.Gray, fontSize = 13.sp)
                Text("${settings.shadowRadius.toInt()} px", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Slider(
                value = settings.shadowRadius,
                onValueChange = { onSettingsChanged(settings.copy(shadowRadius = it)) },
                valueRange = 0f..24f,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("shadow_radius_slider")
            )
        }
    }
}
