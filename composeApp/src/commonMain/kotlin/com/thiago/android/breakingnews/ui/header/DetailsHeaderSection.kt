package com.thiago.android.breakingnews.ui.header


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import breakingnews.composeapp.generated.resources.Res
import breakingnews.composeapp.generated.resources.breaking_news_logo
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun DetailsHeaderSection(
    modifier: Modifier = Modifier,
    imagePainter : Painter = painterResource(Res.drawable.breaking_news_logo),
    onClick: () -> Unit
) {
    Box{
        Image(
            painter = imagePainter,
            null,
            modifier = modifier.fillMaxWidth().height(250.dp),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = modifier.fillMaxWidth().padding(16.dp).clickable{
                onClick.invoke()
            },
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Box(
                modifier = modifier.size(40.dp).background(Color.LightGray, RoundedCornerShape(32)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    null,
                    tint = Color.Black
                )
            }
        }
    }
}