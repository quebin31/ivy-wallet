package com.ivy.wallet.ui.theme.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ivy.wallet.base.hideKeyboard
import com.ivy.wallet.base.isNotNullOrBlank
import com.ivy.wallet.ui.theme.IvyComponentPreview
import com.ivy.wallet.ui.theme.IvyTheme
import com.ivy.wallet.ui.theme.Typo
import com.ivy.wallet.ui.theme.style

@Composable
fun IvyNumberTextField(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    value: TextFieldValue,
    hint: String?,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    textColor: Color = IvyTheme.colors.pureInverse,
    hintColor: Color = Color.Gray,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty && hint.isNotNullOrBlank()) {
            Text(
                modifier = textModifier,
                text = hint!!,
                textAlign = TextAlign.Start,
                style = Typo.numberBody2.style(
                    color = hintColor,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center
                )
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = textModifier,
            value = value,
            onValueChange = onValueChanged,
            textStyle = Typo.numberBody2.style(
                color = textColor,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(IvyTheme.colors.pureInverse),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions ?: KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                autoCorrect = false
            ),
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    hideKeyboard(view)
                }
            )
        )
    }
}

@Preview
@Composable
private fun Preview() {
    IvyComponentPreview {
        IvyNumberTextField(
            value = TextFieldValue(),
            hint = "0"
        ) {

        }
    }
}
