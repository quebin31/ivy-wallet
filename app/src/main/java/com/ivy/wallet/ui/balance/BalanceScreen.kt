package com.ivy.wallet.ui.balance

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.ivy.wallet.R
import com.ivy.wallet.base.format
import com.ivy.wallet.base.onScreenStart
import com.ivy.wallet.ui.IvyAppPreview
import com.ivy.wallet.ui.LocalIvyContext
import com.ivy.wallet.ui.Screen
import com.ivy.wallet.ui.main.FAB_BUTTON_SIZE
import com.ivy.wallet.ui.onboarding.model.TimePeriod
import com.ivy.wallet.ui.theme.*
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.IvyCircleButton
import com.ivy.wallet.ui.theme.components.IvyDividerLine
import com.ivy.wallet.ui.theme.modal.ChoosePeriodModal
import com.ivy.wallet.ui.theme.modal.ChoosePeriodModalData
import com.ivy.wallet.ui.theme.wallet.PeriodSelector

@Composable
fun BoxWithConstraintsScope.BalanceScreen(screen: Screen.BalanceScreen) {
    val viewModel: BalanceViewModel = viewModel()

    val ivyContext = LocalIvyContext.current

    val period by viewModel.period.observeAsState(ivyContext.selectedPeriod)
    val currency by viewModel.currency.observeAsState("")
    val currentBalance by viewModel.currentBalance.observeAsState(0.0)
    val plannedPaymentsAmount by viewModel.plannedPaymentsAmount.observeAsState(0.0)
    val balanceAfterPlannedPayments by viewModel.balanceAfterPlannedPayments.observeAsState(0.0)

    onScreenStart {
        viewModel.start()
    }

    UI(
        period = period,
        currency = currency,
        currentBalance = currentBalance,
        plannedPaymentsAmount = plannedPaymentsAmount,
        balanceAfterPlannedPayments = balanceAfterPlannedPayments,

        onSetPeriod = viewModel::setPeriod,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    period: TimePeriod,

    currency: String,
    currentBalance: Double,
    plannedPaymentsAmount: Double,
    balanceAfterPlannedPayments: Double,

    onSetPeriod: (TimePeriod) -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {}
) {
    var choosePeriodModal: ChoosePeriodModalData? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(20.dp))

        PeriodSelector(
            period = period,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onShowChoosePeriodModal = {
                choosePeriodModal = ChoosePeriodModalData(
                    period = period
                )
            }
        )

        Spacer(Modifier.height(32.dp))

        CurrentBalance(
            currency = currency,
            currentBalance = currentBalance
        )

        Spacer(Modifier.height(32.dp))

        IvyDividerLine(
            modifier = Modifier
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(40.dp))

        BalanceAfterPlannedPayments(
            currency = currency,
            currentBalance = currentBalance,
            plannedPaymentsAmount = plannedPaymentsAmount,
            balanceAfterPlannedPayments = balanceAfterPlannedPayments
        )

        Spacer(Modifier.weight(1f))

        CloseButton()

        Spacer(Modifier.height(48.dp))
    }

    ChoosePeriodModal(
        modal = choosePeriodModal,
        dismiss = {
            choosePeriodModal = null
        }
    ) {
        onSetPeriod(it)
    }
}


@Composable
private fun ColumnScope.CurrentBalance(
    currency: String,
    currentBalance: Double
) {
    Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = "CURRENT BALANCE",
        style = Typo.body2.style(
            color = Gray,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(4.dp))

    BalanceRow(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        currency = currency,
        balance = currentBalance
    )
}

@Composable
private fun ColumnScope.BalanceAfterPlannedPayments(
    currency: String,
    currentBalance: Double,
    plannedPaymentsAmount: Double,
    balanceAfterPlannedPayments: Double
) {
    Text(
        modifier = Modifier
            .padding(horizontal = 32.dp),
        text = "BALANCE AFTER PLANNED PAYMENTS",
        style = Typo.body2.style(
            color = Orange,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(32.dp))

        BalanceRow(
            currency = currency,
            balance = balanceAfterPlannedPayments,

            integerFontSize = 30.sp,
            decimalFontSize = 18.sp,
            currencyFontSize = 18.sp,

            currencyUpfront = false
        )

        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "${currentBalance.format(2)} $currency",
                style = Typo.numberCaption.style(
                    color = IvyTheme.colors.pureInverse,
                    fontWeight = FontWeight.Normal
                )
            )

            Spacer(Modifier.height(2.dp))

            val plusSign = if (plannedPaymentsAmount >= 0) "+" else ""
            Text(
                text = "${plusSign}${plannedPaymentsAmount.format(2)} $currency",
                style = Typo.numberCaption.style(
                    color = IvyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun ColumnScope.CloseButton() {
    val ivyContext = LocalIvyContext.current
    IvyCircleButton(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(FAB_BUTTON_SIZE)
            .rotate(45f)
            .zIndex(200f),
        backgroundPadding = 8.dp,
        icon = R.drawable.ic_add,
        backgroundGradient = Gradient.solid(Gray),
        hasShadow = false,
        tint = White
    ) {
        ivyContext.back()
    }
}

@Preview
@Composable
private fun Preview() {
    IvyAppPreview {
        UI(
            period = TimePeriod.thisMonth(), //preview
            currency = "BGN",
            currentBalance = 9326.55,
            balanceAfterPlannedPayments = 8426.0,
            plannedPaymentsAmount = -900.55,

            onSetPeriod = {}
        )
    }
}
