package com.thiago.android.breakingnews.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetailsContentSection (
    modifier: Modifier = Modifier,
    content: String
) {
    Column(
        modifier = modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        Box(
            modifier = modifier.fillMaxWidth().height(4.dp).background(Color.Gray)
        )
        Spacer(modifier = modifier.height(16.dp))
        Text(
            content,
            fontSize = 16.sp,
            fontStyle = FontStyle.Normal,
            color =  Color.Black,
            textAlign = TextAlign.Justify
        )
    }
}