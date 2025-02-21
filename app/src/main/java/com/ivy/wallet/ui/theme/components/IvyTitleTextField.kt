package com.ivy.wallet.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.ivy.wallet.base.hideKeyboard
import com.ivy.wallet.base.isNotNullOrBlank
import com.ivy.wallet.ui.theme.*

@Composable
fun ColumnScope.IvyTitleTextField(
    modifier: Modifier = Modifier,
    dividerModifier: Modifier = Modifier,
    value: TextFieldValue,
    textColor: Color = IvyTheme.colors.pureInverse,
    hint: String?,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrect = true,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
        capitalization = KeyboardCapitalization.Sentences
    ),
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (isEmpty && hint.isNotNullOrBlank()) {
            Text(
                modifier = Modifier,
                text = hint!!,
                style = Typo.h2.style(
                    color = IvyTheme.colors.gray,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Start
                ),
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = Modifier,
            value = value,
            onValueChange = onValueChanged,
            textStyle = Typo.h2.style(
                color = textColor,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start
            ),
            singleLine = false,
            cursorBrush = SolidColor(IvyTheme.colors.pureInverse),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    hideKeyboard(view)
                }
            )
        )
    }

    Spacer(Modifier.height(8.dp))

    Spacer(
        modifier = dividerModifier
            .fillMaxWidth()
            .height(2.dp)
            .background(IvyTheme.colors.medium, Shapes.roundedFull),
    )
}


@Preview
@Composable
private fun PreviewIvyTitleTextField() {
    IvyComponentPreview {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            IvyTitleTextField(
                modifier = Modifier.padding(horizontal = 32.dp),
                dividerModifier = Modifier.padding(horizontal = 24.dp),
                value = TextFieldValue("Title"),
                hint = "Title",
                onValueChanged = {}
            )
        }

    }
}