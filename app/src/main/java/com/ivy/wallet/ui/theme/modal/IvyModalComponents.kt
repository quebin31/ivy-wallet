package com.ivy.wallet.ui.theme.modal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.wallet.R
import com.ivy.wallet.ui.theme.*
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyCircleButton
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton

@Composable
fun ModalDynamicPrimaryAction(
    initialEmpty: Boolean,
    initialChanged: Boolean,

    onDelete: () -> Unit,
    dismiss: () -> Unit,
    onSave: () -> Unit
) {
    when {
        initialEmpty -> {
            ModalAdd {
                onSave()
                dismiss()
            }
        }
        else -> {
            if (!initialChanged) {
                ModalDelete {
                    onDelete()
                    dismiss()
                }
            } else {
                ModalSave {
                    onSave()
                    dismiss()
                }
            }
        }
    }
}

@Composable
fun ModalSet(
    label: String = "Set",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalCheck(
        label = label,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalCheck(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPositiveButton(
        text = label,
        iconStart = R.drawable.ic_check,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun <T> ModalAddSave(
    item: T,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    if (item != null) {
        ModalSave(
            enabled = enabled,
            onClick = onClick
        )
    } else {
        ModalAdd(
            enabled = enabled,
            onClick = onClick
        )
    }
}

@Composable
fun ModalSave(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPositiveButton(
        text = "Save",
        iconStart = R.drawable.ic_save,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalAdd(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPositiveButton(
        text = "Add",
        iconStart = R.drawable.ic_plus,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalCreate(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPositiveButton(
        text = "Create",
        iconStart = R.drawable.ic_plus,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalNegativeButton(
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IvyButton(
        text = text,
        backgroundGradient = GradientRed,
        iconStart = iconStart,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
fun ModalPositiveButton(
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IvyButton(
        text = text,
        backgroundGradient = GradientGreen,
        iconStart = iconStart,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
fun ModalPrimaryButton(
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IvyButton(
        text = text,
        backgroundGradient = GradientIvy,
        iconStart = iconStart,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
fun ModalDelete(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IvyCircleButton(
        modifier = Modifier.size(40.dp),
        icon = R.drawable.ic_delete,
        backgroundGradient = GradientRed,
        enabled = enabled,
        tint = White,
        onClick = onClick
    )
}

@Composable
fun ModalTitle(
    text: String
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = text,
        style = Typo.body1.style(
            color = IvyTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )
}

@Composable
fun ModalSkip(
    text: String = "Skip",
    onClick: () -> Unit
) {
    IvyOutlinedButton(
        text = text,
        iconStart = null,
        solidBackground = true
    ) {
        onClick()
    }
}