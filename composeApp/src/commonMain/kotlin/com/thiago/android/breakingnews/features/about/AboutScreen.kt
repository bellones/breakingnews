package com.thiago.android.breakingnews.features.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thiago.android.breakingnews.platform.Platform
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun AboutScreen(
    onBackPress: () -> Unit
) {
    Column (modifier = Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        LazyColumn {
            items(makeItems()) { item ->
                AboutComponent(item.first, item.second)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            colors = ButtonDefaults.buttonColors(
                Color.LightGray,
                contentColor = Color.Black
            ),
            onClick = {
                onBackPress.invoke()
            },

            ) {
            Text(
                "Voltar"
            )
        }

    }

}

@Composable
fun AboutComponent (
    title: String = "",
    subtitle: String = ""
) {
    Column (modifier = Modifier.padding(16.dp)) {
        Text(
            title,
            color = Color.Black

        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            subtitle,
            color = Color.Black

        )

        HorizontalDivider()
    }
}
private fun makeItems(): List<Pair<String, String>> {
    val platform = Platform()
    return listOf(
        Pair("Sistema Operacional", "${platform.osName}, ${platform.osVersion}"),
        Pair("Dispositivo   ", platform.deviceModel)
    )
}