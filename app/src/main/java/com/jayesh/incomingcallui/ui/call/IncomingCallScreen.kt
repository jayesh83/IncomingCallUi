package com.jayesh.incomingcallui.ui.call


import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.jayesh.incomingcallui.R
import com.jayesh.incomingcallui.util.ElasticEasing

@Composable
fun IncomingCallScreen(
    name: String,
    number: String,
    country: String? = null,
    avatarUrl: Uri? = null,
    onCallAccept: () -> Unit,
    onCallReject: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        val (callerInfo, replyMsgChip, btnReject, btnAccept, rejectBtnSlideUpText, acceptBtnSlideUpText) = createRefs()
        var acceptBtnHeld by remember { mutableStateOf(false) }
        var rejectBtnHeld by remember { mutableStateOf(false) }
        val showMessageChip by remember {
            derivedStateOf {
                !(acceptBtnHeld || rejectBtnHeld)
            }
        }

        CallerInfoComponent(
            modifier = Modifier.constrainAs(callerInfo) {
                top.linkTo(parent.top, margin = 64.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            name = name,
            number = number,
            country = country, avatarUrl = avatarUrl
        )

        AnimatedVisibility(
            visible = showMessageChip,
            modifier = Modifier.constrainAs(replyMsgChip) {
                top.linkTo(callerInfo.bottom)
                bottom.linkTo(parent.bottom, margin = 80.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            enter = fadeIn(tween(40, easing = LinearEasing)),
            exit = fadeOut(tween(80, easing = LinearEasing))
        ) {
            if (showMessageChip) {
                Chip(text = stringResource(id = R.string.reply_with_msg))
            }
        }

        CallRejectButton(
            modifier = Modifier
                .padding(end = 64.dp)
                .constrainAs(btnReject) {
                    start.linkTo(parent.start)
                    end.linkTo(btnAccept.start)
                    bottom.linkTo(parent.bottom, margin = 64.dp)
                }
                .alpha(if (acceptBtnHeld) 0f else 1f),
            buttonHeld = { isHeld ->
                rejectBtnHeld = isHeld
            },
            onReject = onCallReject
        )

        if (rejectBtnHeld) {
            Text(
                text = stringResource(R.string.slide_up),
                modifier = Modifier
                    .padding(end = 64.dp)
                    .constrainAs(rejectBtnSlideUpText) {
                        start.linkTo(btnReject.start)
                        end.linkTo(btnReject.end)
                        top.linkTo(btnReject.bottom, margin = 24.dp)
                    }
            )
        }

        CallAcceptButton(
            modifier = Modifier
                .padding(start = 64.dp)
                .constrainAs(btnAccept) {
                    start.linkTo(btnReject.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 64.dp)
                }
                .alpha(if (rejectBtnHeld) 0f else 1f),
            buttonHeld = { isHeld ->
                acceptBtnHeld = isHeld
            },
            onAccept = onCallAccept
        )

        if (acceptBtnHeld) {
            Text(
                text = stringResource(R.string.slide_up),
                modifier = Modifier
                    .padding(start = 64.dp)
                    .constrainAs(acceptBtnSlideUpText) {
                        start.linkTo(btnAccept.start)
                        end.linkTo(btnAccept.end)
                        top.linkTo(btnAccept.bottom, margin = 24.dp)
                    }
            )
        }

        createHorizontalChain(
            btnReject, btnAccept,
            chainStyle = ChainStyle.Packed
        )
    }
}

@Composable
fun CallAcceptButton(
    modifier: Modifier = Modifier,
    buttonHeld: (Boolean) -> Unit,
    onAccept: () -> Unit
) {
    var arrowUpAlpha by remember { mutableStateOf(1f) }
    var arrowUpOffsetY by remember { mutableStateOf(0.dp) }

    var btnTranslationY by remember { mutableStateOf(0f) }
    val animatingBtnTranslationY by animateFloatAsState(
        targetValue = btnTranslationY,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1350
                delayMillis = 450
                0f at 0 with FastOutSlowInEasing
                -(100f) at 700 with ElasticEasing
                0f at 1350
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "AcceptBtnTranslationY"
    )

    var btnHeld by remember { mutableStateOf(false) }

    var btnHeldTranslationY by remember { mutableStateOf(0f) }

    val acceptTranslationYThresholdStartPx = remember { 100 }

    val btnHeldAlphaDerived by remember {
        derivedStateOf {
            if (btnHeldTranslationY < -acceptTranslationYThresholdStartPx) {
                1f - ((btnHeldTranslationY + acceptTranslationYThresholdStartPx) * -2.5f) / 1000f
            } else {
                1f
            }
        }
    }

    val isCallAccepted by remember {
        derivedStateOf { btnHeldAlphaDerived <= 0f }
    }

    LaunchedEffect(key1 = isCallAccepted) {
        if (isCallAccepted) onAccept()
    }

    val arrowUpTranslationY by animateFloatAsState(
        targetValue = if (btnHeld) -150f else 0f,
        animationSpec = if (btnHeld) tween(durationMillis = 350, delayMillis = 30) else snap(),
        label = "AcceptBtnArrowUpTranslationY"
    )
    val animatingArrowUpAlpha by animateFloatAsState(
        targetValue = arrowUpAlpha,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1725
                0f at 0 with FastOutSlowInEasing
                1f at 450 with FastOutSlowInEasing
                1f at 900 with FastOutSlowInEasing
                0f at 1300 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "AcceptBtnArrowUpAlpha"
    )
    val animatingArrowUpOffsetY by animateDpAsState(
        targetValue = arrowUpOffsetY,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1725
                12.dp at 0 with FastOutSlowInEasing
                0.dp at 450 with FastOutSlowInEasing
                0.dp at 900 with FastOutSlowInEasing
                -(30).dp at 1300 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "AcceptBtnArrowUpOffsetY"
    )

    LaunchedEffect(key1 = Unit) {
        btnTranslationY = -100f
        arrowUpAlpha = 0f
        arrowUpOffsetY = (-50).dp
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_up),
            contentDescription = "arrow up",
            tint = Color.Gray,
            modifier = Modifier
                .size(48.dp)
                .offset(y = animatingArrowUpOffsetY)
                .graphicsLayer {
                    translationY = arrowUpTranslationY + (btnHeldTranslationY * 0.4f)
                    alpha = animatingArrowUpAlpha
                }
        )
        Surface(
            shape = CircleShape,
            color = colorResource(id = R.color.call_accept_cta_color),
            modifier = Modifier
                .padding(top = 48.dp)
                .requiredSize(72.dp)
                .graphicsLayer {
                    translationY = if (btnHeld) btnHeldTranslationY
                    else animatingBtnTranslationY
                    alpha = btnHeldAlphaDerived
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            btnHeldTranslationY = animatingBtnTranslationY
                            btnHeld = true
                            buttonHeld(true)

                            awaitRelease()

                            btnHeld = false
                            buttonHeld(false)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            btnHeld = false
                            buttonHeld(false)
                            btnHeldTranslationY = 0f
                        },
                        onDragCancel = {
                            btnHeld = false
                            buttonHeld(false)
                            btnHeldTranslationY = 0f
                        },
                        onVerticalDrag = { _, amount ->
                            btnHeldTranslationY = (btnHeldTranslationY + amount).coerceAtMost(0f)
                        }
                    )
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_telephone_accept),
                contentDescription = "icon accept",
                tint = Color.White,
                modifier = Modifier.requiredSize(40.dp)
            )
        }
    }
}

@Composable
fun CallRejectButton(
    modifier: Modifier = Modifier,
    buttonHeld: (Boolean) -> Unit,
    onReject: () -> Unit
) {
    var arrowUpAlpha by remember { mutableStateOf(1f) }
    var arrowUpOffsetY by remember { mutableStateOf(0.dp) }

    var btnTranslationY by remember { mutableStateOf(0f) }
    val animatingBtnTranslationY by animateFloatAsState(
        targetValue = btnTranslationY,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1350
                delayMillis = 450
                0f at 0 with FastOutSlowInEasing
                -(25f) at 700 with ElasticEasing
                0f at 1350
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "RejectBtnTranslationY"
    )
    var btnHeld by remember { mutableStateOf(false) }

    var btnHeldTranslationY by remember { mutableStateOf(0f) }


    val acceptTranslationYThresholdStartPx = remember { 100 }

    val btnHeldAlphaDerived by remember {
        derivedStateOf {
            if (btnHeldTranslationY < -acceptTranslationYThresholdStartPx) {
                1f - ((btnHeldTranslationY + acceptTranslationYThresholdStartPx) * -2.5f) / 1000f
            } else {
                1f
            }
        }
    }

    val isCallRejected by remember {
        derivedStateOf { btnHeldAlphaDerived <= 0f }
    }

    LaunchedEffect(key1 = isCallRejected) {
        if (isCallRejected) onReject()
    }

    val arrowUpTranslationY by animateFloatAsState(
        targetValue = if (btnHeld) -150f else 0f,
        animationSpec = if (btnHeld) tween(durationMillis = 350, delayMillis = 30) else snap(),
        label = "RejectBtnArrowUpTranslationY"
    )
    val animatingArrowUpAlpha by animateFloatAsState(
        targetValue = arrowUpAlpha,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 2025
                0f at 0 with FastOutSlowInEasing
                1f at 450 with FastOutSlowInEasing
                1f at 1400 with FastOutSlowInEasing
                0f at 1800 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "AcceptBtnArrowUpAlpha"
    )
    val animatingArrowUpOffsetY by animateDpAsState(
        targetValue = arrowUpOffsetY,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 2025
                12.dp at 0 with FastOutSlowInEasing
                0.dp at 450 with FastOutSlowInEasing
                0.dp at 1400 with FastOutSlowInEasing
                -(30).dp at 1800 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "AcceptBtnArrowUpOffsetY"
    )

    LaunchedEffect(key1 = Unit) {
        btnTranslationY = -25f
        arrowUpAlpha = 0f
        arrowUpOffsetY = (-50).dp
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (btnHeld) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = "arrow up",
                tint = Color.Gray,
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = animatingArrowUpOffsetY)
                    .graphicsLayer {
                        translationY = arrowUpTranslationY + (btnHeldTranslationY * 0.4f)
                        alpha = animatingArrowUpAlpha
                    }
            )
        }
        Surface(
            shape = CircleShape,
            color = colorResource(id = R.color.call_reject_cta_color),
            modifier = Modifier
                .padding(top = 48.dp)
                .requiredSize(72.dp)
                .graphicsLayer {
                    translationY = if (btnHeld) btnHeldTranslationY
                    else animatingBtnTranslationY
                    alpha = btnHeldAlphaDerived
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            btnHeldTranslationY = animatingBtnTranslationY
                            btnHeld = true
                            buttonHeld(true)

                            awaitRelease()

                            btnHeld = false
                            buttonHeld(false)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            btnHeld = false
                            buttonHeld(false)
                            btnHeldTranslationY = 0f
                        },
                        onDragCancel = {
                            btnHeld = false
                            buttonHeld(false)
                            btnHeldTranslationY = 0f
                        },
                        onVerticalDrag = { _, amount ->
                            btnHeldTranslationY = (btnHeldTranslationY + amount).coerceAtMost(0f)
                        }
                    )
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_telephone_reject),
                contentDescription = "icon reject",
                tint = Color.White,
                modifier = Modifier.requiredSize(40.dp)
            )
        }
    }
}

@Composable
fun CallerInfoComponent(
    modifier: Modifier = Modifier,
    name: String,
    number: String,
    country: String? = null,
    avatarUrl: Uri? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.demo_face), // TODO: use avatar URL
            contentDescription = "caller's image",
            modifier = Modifier
                .clip(CircleShape)
                .requiredSize(88.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(top = 12.dp)
        )

        if (country != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                VerticalDivider()
                Text(
                    text = country,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            }
        } else {
            Text(
                text = number,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    thickness: Dp = 1.dp
) {
    Box(
        modifier
            .height(Dp(MaterialTheme.typography.titleLarge.fontSize.value / 1.3f))
            .width(thickness)
            .background(color = color)
    )
}

@Composable
fun Chip(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(40.dp),
        color = Color.DarkGray.copy(alpha = 0.8f),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}