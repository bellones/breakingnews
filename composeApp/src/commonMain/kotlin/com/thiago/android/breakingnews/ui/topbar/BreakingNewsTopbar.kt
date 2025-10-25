package com.thiago.android.breakingnews.ui.topbar


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import breakingnews.composeapp.generated.resources.Res
import breakingnews.composeapp.generated.resources.breaking_news_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun BreakingNewsTopBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .height(56.dp )
    ) {
        Image(
            painter = painterResource(Res.drawable.breaking_news_logo),
            "Logo",
            modifier = modifier.align(Alignment.Center).height(56.dp)
        )
        Row(modifier = modifier.padding(start = 16.dp, end= 16.dp).align(alignment = Alignment.CenterEnd).clickable{
            onClick.invoke()
        }) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                "Settings",
                tint= Color.Black
            )
        }
    }
}