package com.ivy.wallet.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.ivy.wallet.Constants
import com.ivy.wallet.R
import com.ivy.wallet.base.*
import com.ivy.wallet.ui.IvyActivity
import com.ivy.wallet.ui.IvyAppPreview
import com.ivy.wallet.ui.LocalIvyContext
import com.ivy.wallet.ui.Screen
import com.ivy.wallet.ui.theme.*
import com.ivy.wallet.ui.theme.components.BufferBattery
import com.ivy.wallet.ui.theme.components.CircleButtonFilled
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.AddModalBackHandling
import com.ivy.wallet.ui.theme.wallet.AmountCurrencyB1
import java.util.*
import kotlin.math.roundToInt

@Composable
fun BoxWithConstraintsScope.MoreMenu(
    expanded: Boolean,

    balance: Double,
    buffer: Double,
    currency: String,
    theme: Theme,

    setExpanded: (Boolean) -> Unit,
    onSwitchTheme: () -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit
) {
    val ivyContext = LocalIvyContext.current

    val percentExpanded by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = springBounce()
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) -180f else 0f,
        animationSpec = springBounce()
    )

    val buttonSizePx = 40.dp.toDensityPx()

    val xBase = ivyContext.screenWidth - 24.dp.toDensityPx()
    val yBaseCollapsed = 20.dp.toDensityPx() + statusBarInset()
    val yBaseExpanded = ivyContext.screenHeight - 48.dp.toDensityPx() - navigationBarInset()

    val yButton = lerp(
        start = yBaseCollapsed,
        end = yBaseExpanded - buttonSizePx,
        fraction = percentExpanded
    )

    //Background
    val colorMedium = IvyTheme.colors.medium
    if (percentExpanded > 0.01f) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickableNoIndication {
                    //do nothing
                }
                .zIndex(500f)
        ) {
            val radiusCollapsed = buttonSizePx / 2f
            val radiusExpanded = ivyContext.screenHeight * 1.5f
            val radius = lerp(radiusCollapsed, radiusExpanded, percentExpanded)

            val yBackground = lerp(
                start = yBaseCollapsed + radius,
                end = yBaseExpanded,
                fraction = percentExpanded
            )

            drawCircle(
                color = colorMedium,
                center = Offset(
                    x = xBase - buttonSizePx / 2f,
                    y = yBackground
                ),
                radius = radius
            )
        }
    }

    if (percentExpanded > 0.01f) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize()
                .alpha(percentExpanded)
                .zIndex(510f)
        ) {
            val modalId = remember {
                UUID.randomUUID()
            }

            AddModalBackHandling(
                modalId = modalId,
                visible = expanded
            ) {
                setExpanded(false)
            }

            Content(
                theme = theme,
                onSwitchTheme = onSwitchTheme,
                balance = balance,
                buffer = buffer,
                currency = currency,
                onBufferClick = onBufferClick,
                onCurrencyClick = onCurrencyClick
            )
        }
    }

    CircleButtonFilled(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)


                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = xBase.roundToInt() - buttonSizePx.roundToInt(),
                        y = yButton.roundToInt()
                    )
                }
            }
            .rotate(iconRotation)
            .thenIf(expanded) {
                zIndex(520f)
            },
        backgroundColor = colorLerp(IvyTheme.colors.medium, IvyTheme.colors.pure, percentExpanded),
        icon = R.drawable.ic_expandarrow
    ) {
        setExpanded(!expanded)
    }

}

@Composable
private fun ColumnScope.Content(
    balance: Double,
    buffer: Double,
    currency: String,
    theme: Theme,

    onSwitchTheme: () -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
) {
    Spacer(Modifier.height(32.dp))

    QuickAccess(
        theme = theme,
        onSwitchTheme = onSwitchTheme
    )

    Spacer(Modifier.height(40.dp))

    Buffer(
        buffer = buffer,
        currency = currency,
        balance = balance,
        onBufferClick = onBufferClick
    )

    Spacer(Modifier.height(16.dp))

    OpenSource()

    Spacer(Modifier.weight(1f))

    IvyButton(
        modifier = Modifier.padding(start = 24.dp),
        text = currency,
        iconStart = R.drawable.ic_currency
    ) {
        onCurrencyClick()
    }

    Spacer(Modifier.height(40.dp))
}

@Composable
private fun ColumnScope.OpenSource() {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(Shapes.rounded16)
            .background(IvyTheme.colors.pure)
            .clickable {
                openUrl(
                    uriHandler = uriHandler,
                    url = Constants.URL_IVY_WALLET_REPO
                )
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))

        IvyIcon(
            icon = R.drawable.github_logo
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 24.dp)
        ) {
            Text(
                text = "Ivy Wallet is open-source",
                style = Typo.body2.style(
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = Constants.URL_IVY_WALLET_REPO,
                style = Typo.caption.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = Blue
                )
            )
        }

    }
}

@Composable
private fun ColumnScope.Buffer(
    buffer: Double,
    currency: String,
    balance: Double,
    onBufferClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication {
                onBufferClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        Text(
            text = "Savings goal",
            style = Typo.body1.style(
                color = IvyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.weight(1f))

        AmountCurrencyB1(
            amount = buffer,
            currency = currency,
            amountFontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.width(32.dp))
    }

    Spacer(Modifier.height(12.dp))

    BufferBattery(
        modifier = Modifier.padding(horizontal = 16.dp),
        buffer = buffer,
        currency = currency,
        balance = balance,
    ) {
        onBufferClick()
    }
}

@Composable
private fun QuickAccess(
    theme: Theme,
    onSwitchTheme: () -> Unit
) {
    val ivyContext = LocalIvyContext.current

    Text(
        modifier = Modifier.padding(start = 24.dp),
        text = "Quick access"
    )

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Spacer(Modifier.width(4.dp))

        MoreMenuButton(
            icon = R.drawable.ic_settings,
            label = "Settings"
        ) {
            ivyContext.navigateTo(Screen.Settings)
        }

        Spacer(Modifier.width(0.dp))

        MoreMenuButton(
            icon = R.drawable.ic_categories,
            label = "Categories"
        ) {
            ivyContext.navigateTo(Screen.Categories)
        }

        Spacer(Modifier.width(0.dp))

        MoreMenuButton(
            icon = when (theme) {
                Theme.LIGHT -> R.drawable.ic_lightmode
                Theme.DARK -> R.drawable.ic_darkmode
            },
            label = when (theme) {
                Theme.LIGHT -> "Light mode"
                Theme.DARK -> "Dark mode"
            },
            backgroundColor = when (theme) {
                Theme.LIGHT -> IvyTheme.colors.pure
                Theme.DARK -> IvyTheme.colors.pureInverse
            },
            tint = when (theme) {
                Theme.LIGHT -> IvyTheme.colors.pureInverse
                Theme.DARK -> IvyTheme.colors.pure
            }
        ) {
            onSwitchTheme()
        }

        Spacer(Modifier.width(0.dp))

        MoreMenuButton(
            icon = R.drawable.ic_planned_payments,
            label = "Planned\nPayments"
        ) {
            ivyContext.navigateTo(Screen.PlannedPayments)
        }

        Spacer(Modifier.width(12.dp))
    }

    Spacer(Modifier.height(16.dp))

    //Second Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Spacer(Modifier.width(4.dp))

        val context = LocalContext.current
        MoreMenuButton(
            icon = R.drawable.ic_share,
            label = "Share\nIvy Wallet"
        ) {
            (context as IvyActivity).shareIvyWallet()
        }

        MoreMenuButton(
            icon = R.drawable.ic_statistics_s,
            label = "Reports",
            expandPadding = 11.dp
        ) {
            ivyContext.navigateTo(Screen.Report)
        }

        MoreMenuButton(
            icon = R.drawable.ic_budget_s,
            label = "Budgets",
            expandPadding = 11.dp
        ) {
            ivyContext.navigateTo(Screen.Budget)
        }
    }
}

@Composable
private fun MoreMenuButton(
    @DrawableRes icon: Int,
    label: String,

    backgroundColor: Color = IvyTheme.colors.pure,
    tint: Color = IvyTheme.colors.pureInverse,
    expandPadding: Dp = 8.dp,

    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircleButtonFilled(
            icon = icon,
            backgroundColor = backgroundColor,
            tint = tint,
            clickAreaPadding = expandPadding,
            onClick = onClick
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier
                .defaultMinSize(minWidth = 92.dp)
                .clickableNoIndication {
                    onClick()
                },
            text = label,
            style = Typo.caption.style(
                color = IvyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Preview
@Composable
private fun Preview_Expanded() {
    IvyAppPreview {
        MoreMenu(
            expanded = true,
            balance = 7523.43,
            buffer = 5000.0,
            currency = "BGN",
            theme = Theme.LIGHT,
            setExpanded = {
            },
            onSwitchTheme = { },
            onBufferClick = { }
        ) {

        }
    }
}

@Preview
@Composable
private fun Preview() {
    IvyAppPreview {
        var expanded by remember { mutableStateOf(false) }

        MoreMenu(
            expanded = expanded,
            balance = 7523.43,
            buffer = 5000.0,
            currency = "BGN",
            theme = Theme.LIGHT,
            setExpanded = {
                expanded = it
            },
            onSwitchTheme = { },
            onBufferClick = { }
        ) {

        }
    }
}