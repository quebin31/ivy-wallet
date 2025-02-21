package com.ivy.wallet.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.ivy.wallet.BuildConfig
import com.ivy.wallet.Constants
import com.ivy.wallet.Constants.URL_IVY_CONTRIBUTORS
import com.ivy.wallet.R
import com.ivy.wallet.base.*
import com.ivy.wallet.model.AuthProviderType
import com.ivy.wallet.model.IvyCurrency
import com.ivy.wallet.model.entity.User
import com.ivy.wallet.ui.IvyActivity
import com.ivy.wallet.ui.IvyAppPreview
import com.ivy.wallet.ui.LocalIvyContext
import com.ivy.wallet.ui.Screen
import com.ivy.wallet.ui.theme.*
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.IvySwitch
import com.ivy.wallet.ui.theme.components.IvyToolbar
import com.ivy.wallet.ui.theme.modal.ChooseStartDateOfMonthModal
import com.ivy.wallet.ui.theme.modal.CurrencyModal
import com.ivy.wallet.ui.theme.modal.NameModal
import com.ivy.wallet.ui.theme.modal.RequestFeatureModal
import java.util.*

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.SettingsScreen(screen: Screen.Settings) {
    val viewModel: SettingsViewModel = viewModel()

    val user by viewModel.user.observeAsState()
    val opSync by viewModel.opSync.observeAsState()
    val currencyCode by viewModel.currencyCode.observeAsState("")
    val lockApp by viewModel.lockApp.observeAsState(false)
    val startDateOfMonth by viewModel.startDateOfMonth.observeAsState(1)

    val nameLocalAccount by viewModel.nameLocalAccount.observeAsState()

    onScreenStart {
        viewModel.start()
    }

    val ivyActivity = LocalContext.current as IvyActivity
    val context = LocalContext.current
    UI(
        user = user,
        currencyCode = currencyCode,
        opSync = opSync,
        lockApp = lockApp,

        nameLocalAccount = nameLocalAccount,
        startDateOfMonth = startDateOfMonth,


        onSetCurrency = viewModel::setCurrency,
        onSetName = viewModel::setName,

        onSync = viewModel::sync,
        onLogout = viewModel::logout,
        onLogin = viewModel::login,
        onExportToCSV = {
            viewModel.exportToCSV(context)
        },
        onSetLockApp = viewModel::setLockApp,
        onSetStartDateOfMonth = viewModel::setStartDateOfMonth,
        onRequestFeature = { title, body ->
            viewModel.requestFeature(
                ivyActivity = ivyActivity,
                title = title,
                body = body
            )
        }
    )
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    user: User?,
    currencyCode: String,
    opSync: OpResult<Boolean>?,

    lockApp: Boolean,

    nameLocalAccount: String?,
    startDateOfMonth: Int = 1,

    onSetCurrency: (String) -> Unit,
    onSetName: (String) -> Unit = {},


    onSync: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onExportToCSV: () -> Unit = {},
    onSetLockApp: (Boolean) -> Unit = {},
    onSetStartDateOfMonth: (Int) -> Unit = {},
    onRequestFeature: (String, String) -> Unit = { _, _ -> }
) {
    var currencyModalVisible by remember { mutableStateOf(false) }
    var nameModalVisible by remember { mutableStateOf(false) }
    var chooseStartDateOfMonthVisible by remember { mutableStateOf(false) }
    var requestFeatureModalVisible by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        stickyHeader {
            val ivyContext = LocalIvyContext.current
            IvyToolbar(
                onBack = { ivyContext.onBackPressed() },
            ) {
                Spacer(Modifier.weight(1f))

                Text(
                    modifier = Modifier.clickableNoIndication {
                        ivyContext.navigateTo(Screen.Test)
                    },
                    text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = Typo.numberCaption.style(
                        color = IvyTheme.colors.gray,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.width(32.dp))
            }
            //onboarding toolbar include paddingBottom 16.dp
        }

        item {
            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = "Settings",
                style = Typo.h2.style(
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(Modifier.height(24.dp))

            CurrencyButton(currency = currencyCode) {
                currencyModalVisible = true
            }

            Spacer(Modifier.height(12.dp))

            AccountCard(
                user = user,
                opSync = opSync,
                nameLocalAccount = nameLocalAccount,

                onSync = onSync,
                onLogout = onLogout,
                onLogin = onLogin,
            ) {
                nameModalVisible = true
            }

            Spacer(Modifier.height(20.dp))

            Premium()
        }

        item {
            SettingsSectionDivider(text = "Import & Export")

            Spacer(Modifier.height(16.dp))

            val ivyContext = LocalIvyContext.current
            ExportCSV {
                onExportToCSV()
            }

            Spacer(Modifier.height(12.dp))

            SettingsPrimaryButton(
                icon = R.drawable.ic_export_csv,
                text = "Import CSV",
                backgroundGradient = GradientGreen
            ) {
                ivyContext.navigateTo(
                    Screen.Import(
                        launchedFromOnboarding = false
                    )
                )
            }
        }

        item {
            SettingsSectionDivider(text = "Other")

            Spacer(Modifier.height(16.dp))

            val ivyActivity = LocalContext.current as IvyActivity
            SettingsPrimaryButton(
                icon = R.drawable.ic_custom_star_m,
                text = "Rate us on Google Play",
                backgroundGradient = GradientIvy
            ) {
                ivyActivity.reviewIvyWallet(dismissReviewCard = false)
            }

            Spacer(Modifier.height(12.dp))

            SettingsPrimaryButton(
                icon = R.drawable.ic_custom_family_m,
                text = "Share Ivy Wallet",
                backgroundGradient = Gradient.solid(Red3)
            ) {
                ivyActivity.shareIvyWallet()
            }

            Spacer(Modifier.height(12.dp))

            LockAppSwitch(
                lockApp = lockApp,
                onSetLockApp = onSetLockApp
            )

            Spacer(Modifier.height(12.dp))

            StartDateOfMonth(
                startDateOfMonth = startDateOfMonth
            ) {
                chooseStartDateOfMonthVisible = true
            }
        }

        item {
            SettingsSectionDivider(text = "Product")

            Spacer(Modifier.height(16.dp))

            HelpCenter()

            Spacer(Modifier.height(12.dp))

            Roadmap()

            Spacer(Modifier.height(12.dp))

            RequestFeature {
                requestFeatureModalVisible = true
            }

            Spacer(Modifier.height(12.dp))

            ContactSupport()

            Spacer(Modifier.height(12.dp))

            ProjectContributors()

            Spacer(Modifier.height(12.dp))

            TCAndPrivacyPolicy()
        }

        item {
            Spacer(modifier = Modifier.height(120.dp)) //last item spacer
        }
    }

    CurrencyModal(
        title = "Set currency",
        initialCurrency = IvyCurrency.fromCode(currencyCode),
        visible = currencyModalVisible,
        dismiss = { currencyModalVisible = false }
    ) {
        onSetCurrency(it)
    }

    NameModal(
        visible = nameModalVisible,
        name = nameLocalAccount ?: "",
        dismiss = { nameModalVisible = false }
    ) {
        onSetName(it)
    }

    ChooseStartDateOfMonthModal(
        visible = chooseStartDateOfMonthVisible,
        selectedStartDateOfMonth = startDateOfMonth,
        dismiss = { chooseStartDateOfMonthVisible = false }
    ) {
        onSetStartDateOfMonth(it)
    }

    RequestFeatureModal(
        visible = requestFeatureModalVisible,
        dismiss = {
            requestFeatureModalVisible = false
        },
        onSubmit = onRequestFeature
    )
}

@Composable
private fun StartDateOfMonth(
    startDateOfMonth: Int,
    onClick: () -> Unit
) {
    SettingsButtonRow(
        onClick = onClick
    ) {
        Spacer(Modifier.width(16.dp))

        IvyIcon(
            modifier = Modifier
                .size(48.dp)
                .padding(all = 4.dp),
            icon = R.drawable.ic_custom_calendar_m,
            tint = IvyTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = "Start date of month",
            style = Typo.body2.style(
                color = IvyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = startDateOfMonth.toString(),
            style = Typo.numberBody2.style(
                fontWeight = FontWeight.ExtraBold,
                color = IvyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun HelpCenter() {
    val ivyContext = LocalIvyContext.current
    SettingsDefaultButton(
        icon = R.drawable.ic_custom_education_m,
        text = "Help Center",
    ) {
        ivyContext.navigateTo(
            Screen.WebView(url = Constants.URL_HELP_CENTER)
        )
    }
}

@Composable
private fun Roadmap() {
    val ivyContext = LocalIvyContext.current
    SettingsDefaultButton(
        icon = R.drawable.ic_custom_rocket_m,
        text = "Roadmap",
    ) {
        ivyContext.navigateTo(
            Screen.WebView(url = Constants.URL_ROADMAP)
        )
    }
}

@Composable
private fun RequestFeature(
    onClick: () -> Unit
) {
    SettingsDefaultButton(
        icon = R.drawable.ic_custom_programming_m,
        text = "Request a feature",
    ) {
        onClick()
    }
}

@Composable
private fun ContactSupport() {
    val ivyContext = LocalIvyContext.current
    SettingsDefaultButton(
        icon = R.drawable.ic_support,
        text = "Contact support",
    ) {
        ivyContext.contactSupport()
    }
}

@Composable
private fun ProjectContributors() {
    val ivyContext = LocalIvyContext.current
    SettingsDefaultButton(
        icon = R.drawable.ic_custom_people_m,
        text = "Project Contributors",
    ) {
        ivyContext.navigateTo(
            Screen.WebView(url = URL_IVY_CONTRIBUTORS)
        )
    }
}

@Composable
private fun LockAppSwitch(
    lockApp: Boolean,
    onSetLockApp: (Boolean) -> Unit
) {
    SettingsButtonRow(
        onClick = {
            onSetLockApp(!lockApp)
        }
    ) {
        Spacer(Modifier.width(16.dp))

        IvyIcon(
            icon = R.drawable.ic_custom_fingerprint_m,
            tint = IvyTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = "Lock app",
            style = Typo.body2.style(
                color = IvyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.weight(1f))

        IvySwitch(enabled = lockApp) {
            onSetLockApp(it)
        }

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun AccountCard(
    user: User?,
    opSync: OpResult<Boolean>?,
    nameLocalAccount: String?,

    onSync: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onCardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(Shapes.rounded24)
            .background(IvyTheme.colors.medium, Shapes.rounded24)
            .clickable {
                onCardClick()
            }
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(24.dp))

            Text(
                text = "ACCOUNT",
                style = Typo.caption.style(
                    fontWeight = FontWeight.Black,
                    color = IvyTheme.colors.gray
                )
            )

            Spacer(Modifier.weight(1f))

            if (user != null) {
                AccountCardButton(
                    icon = R.drawable.ic_logout,
                    text = "Logout"
                ) {
                    onLogout()
                }
            } else {
                AccountCardButton(
                    icon = R.drawable.ic_login,
                    text = "Login"
                ) {
                    onLogin()
                }
            }

            Spacer(Modifier.width(16.dp))
        }

        if (user != null) {
            AccountCardUser(
                localName = nameLocalAccount,
                user = user,
                opSync = opSync,
                onSync = onSync
            )
        } else {
            AccountCardLocalAccount(
                name = nameLocalAccount,
            )
        }

    }
}

@Composable
private fun AccountCardUser(
    localName: String?,
    user: User,
    opSync: OpResult<Boolean>?,

    onSync: () -> Unit,
) {
    Spacer(Modifier.height(4.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        if (user.profilePicture != null) {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(32.dp),
                painter = rememberCoilPainter(request = user.profilePicture),
                contentScale = ContentScale.FillBounds,
                contentDescription = "profile picture"
            )

            Spacer(Modifier.width(12.dp))
        }

        Text(
            text = localName ?: user.names(),
            style = Typo.body2.style(
                fontWeight = FontWeight.ExtraBold,
                color = IvyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.width(24.dp))
    }

    Spacer(Modifier.height(12.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        IvyIcon(
            icon = R.drawable.ic_email
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = user.email,
            style = Typo.body2.style(
                fontWeight = FontWeight.ExtraBold,
                color = IvyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.width(24.dp))
    }

    Spacer(Modifier.height(12.dp))

    when (opSync) {
        is OpResult.Loading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(24.dp))

                IvyIcon(
                    icon = R.drawable.ic_data_synced,
                    tint = Orange
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Syncing...",
                    style = Typo.body2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = Orange
                    )
                )

                Spacer(Modifier.width(24.dp))
            }
        }
        is OpResult.Success -> {
            if (opSync.data) {
                //synced
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(24.dp))

                    IvyIcon(
                        icon = R.drawable.ic_data_synced,
                        tint = Green
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Data synced to cloud",
                        style = Typo.body2.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = Green
                        )
                    )

                    Spacer(Modifier.width(24.dp))
                }
            } else {
                //not synced
                IvyButton(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    iconStart = R.drawable.ic_sync,
                    text = "Tap to sync",
                    backgroundGradient = GradientRed
                ) {
                    onSync()
                }
            }
        }
        is OpResult.Failure -> {
            IvyButton(
                modifier = Modifier.padding(horizontal = 24.dp),
                iconStart = R.drawable.ic_sync,
                text = "Sync failed. Tap to sync",
                backgroundGradient = GradientRed
            ) {
                onSync()
            }
        }
    }

    Spacer(Modifier.height(24.dp))
}

@Composable
private fun AccountCardLocalAccount(
    name: String?,
) {
    Spacer(Modifier.height(4.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        IvyIcon(icon = R.drawable.ic_local_account)

        Spacer(Modifier.width(12.dp))

        Text(
            text = if (name != null && name.isNotBlank()) name else "Anonymous",
            style = Typo.body2.style(
                fontWeight = FontWeight.Bold
            )
        )
    }

    Spacer(Modifier.height(24.dp))
}

@Composable
private fun Premium() {
    val ivyContext = LocalIvyContext.current
    SettingsPrimaryButton(
        icon = R.drawable.ic_custom_crown_s,
        text = if (ivyContext.isPremium) "Premium" else "Buy premium",
        hasShadow = true,
        backgroundGradient = GradientOrange
    ) {
        ivyContext.navigateTo(
            Screen.Paywall(
                paywallReason = null
            )
        )
    }
}

@Composable
private fun ExportCSV(
    onExportToCSV: () -> Unit
) {
    SettingsDefaultButton(
        icon = R.drawable.ic_export_csv,
        text = "Export to CSV",
    ) {
        onExportToCSV()
    }
}

@Composable
private fun TCAndPrivacyPolicy() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        val uriHandler = LocalUriHandler.current

        Text(
            modifier = Modifier
                .weight(1f)
                .clip(Shapes.roundedFull)
                .border(2.dp, IvyTheme.colors.medium, Shapes.roundedFull)
                .clickable {
                    uriHandler.openUri(Constants.URL_TC)
                }
                .padding(vertical = 14.dp),
            text = "Terms & Conditions",
            style = Typo.caption.style(
                fontWeight = FontWeight.ExtraBold,
                color = IvyTheme.colors.pureInverse,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.width(12.dp))

        Text(
            modifier = Modifier
                .weight(1f)
                .clip(Shapes.roundedFull)
                .border(2.dp, IvyTheme.colors.medium, Shapes.roundedFull)
                .clickable {
                    uriHandler.openUri(Constants.URL_PRIVACY_POLICY)
                }
                .padding(vertical = 14.dp),
            text = "Privacy Policy",
            style = Typo.caption.style(
                fontWeight = FontWeight.ExtraBold,
                color = IvyTheme.colors.pureInverse,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun SettingsPrimaryButton(
    @DrawableRes icon: Int,
    text: String,
    hasShadow: Boolean = false,
    backgroundGradient: Gradient = Gradient.solid(IvyTheme.colors.medium),
    textColor: Color = White,
    onClick: () -> Unit
) {
    SettingsButtonRow(
        hasShadow = hasShadow,
        backgroundGradient = backgroundGradient,
        onClick = onClick
    ) {
        Spacer(Modifier.width(16.dp))

        IvyIcon(
            icon = icon,
            tint = textColor
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = text,
            style = Typo.body2.style(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun SettingsButtonRow(
    hasShadow: Boolean = false,
    backgroundGradient: Gradient = Gradient.solid(IvyTheme.colors.medium),
    onClick: (() -> Unit)?,
    Content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .thenIf(hasShadow) {
                drawColoredShadow(color = backgroundGradient.startColor)
            }
            .fillMaxWidth()
            .clip(Shapes.rounded16)
            .background(backgroundGradient.asHorizontalBrush(), Shapes.rounded16)
            .thenIf(onClick != null) {
                clickable {
                    onClick?.invoke()
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Content()
    }
}

@Composable
private fun AccountCardButton(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(Shapes.roundedFull)
            .background(IvyTheme.colors.pure, Shapes.roundedFull)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        IvyIcon(
            icon = icon
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 12.dp),
            text = text,
            style = Typo.body2.style(
                fontWeight = FontWeight.Bold,
                color = IvyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun CurrencyButton(
    currency: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(Shapes.rounded16)
            .border(2.dp, IvyTheme.colors.medium, Shapes.rounded16)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        IvyIcon(icon = R.drawable.ic_currency)

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = "Set currency",
            style = Typo.body2.style(
                color = IvyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = currency,
            style = Typo.body1.style(
                color = IvyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(4.dp))

        IvyIcon(icon = R.drawable.ic_arrow_right)

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun SettingsSectionDivider(
    text: String
) {
    Spacer(Modifier.height(32.dp))

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = text,
        style = Typo.body2.style(
            color = Gray,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun SettingsDefaultButton(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit
) {
    SettingsPrimaryButton(
        icon = icon,
        text = text,
        backgroundGradient = Gradient.solid(IvyTheme.colors.medium),
        textColor = IvyTheme.colors.pureInverse
    ) {
        onClick()
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
private fun Preview_synced() {
    IvyAppPreview {
        UI(
            user = User(
                email = "iliyan.germanov971@gmail.com",
                authProviderType = AuthProviderType.GOOGLE,
                firstName = "Iliyan",
                lastName = "Germanov",
                color = 11,
                id = UUID.randomUUID(),
                profilePicture = null
            ),
            nameLocalAccount = null,
            opSync = OpResult.success(true),
            lockApp = false,
            currencyCode = "BGN",
            onSetCurrency = {},
            onLogout = {},
            onLogin = {},
            onSync = {}
        )
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
private fun Preview_notSynced() {
    IvyAppPreview {
        UI(
            user = User(
                email = "iliyan.germanov971@gmail.com",
                authProviderType = AuthProviderType.GOOGLE,
                firstName = "Iliyan",
                lastName = "Germanov",
                color = 11,
                id = UUID.randomUUID(),
                profilePicture = null
            ),
            lockApp = false,
            nameLocalAccount = null,
            opSync = OpResult.success(false),
            currencyCode = "BGN",
            onSetCurrency = {},
            onLogout = {},
            onLogin = {},
            onSync = {}
        )
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
private fun Preview_loading() {
    IvyAppPreview {
        UI(
            user = User(
                email = "iliyan.germanov971@gmail.com",
                authProviderType = AuthProviderType.GOOGLE,
                firstName = "Iliyan",
                lastName = null,
                color = 11,
                id = UUID.randomUUID(),
                profilePicture = null
            ),
            lockApp = false,
            nameLocalAccount = null,
            opSync = OpResult.loading(),
            currencyCode = "BGN",
            onSetCurrency = {},
            onLogout = {},
            onLogin = {},
            onSync = {}
        )
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
private fun Preview_localAccount() {
    IvyAppPreview {
        UI(
            user = null,
            nameLocalAccount = "Iliyan",
            opSync = null,
            currencyCode = "BGN",
            lockApp = false,
            onSetCurrency = {},
            onLogout = {},
            onLogin = {},
            onSync = {}
        )
    }
}