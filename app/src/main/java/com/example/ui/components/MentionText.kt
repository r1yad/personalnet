package com.example.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan

@Composable
fun MentionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 21.sp,
    onMentionClick: (String) -> Unit = {}
) {
    val mentionRegex = Regex("(@[a-zA-Z0-9_]+)")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        mentionRegex.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val username = matchResult.value.removePrefix("@")
            pushStringAnnotation(tag = "MENTION", annotation = username)
            withStyle(
                style = SpanStyle(
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(matchResult.value)
            }
            pop()

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "MENTION", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onMentionClick(annotation.item)
                }
        }
    )
}
