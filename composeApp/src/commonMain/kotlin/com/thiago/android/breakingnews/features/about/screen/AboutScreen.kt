package com.thiago.android.breakingnews.features.about.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thiago.android.breakingnews.features.about.action.AboutAction
import com.thiago.android.breakingnews.features.about.state.AboutState
import com.thiago.android.breakingnews.features.about.viewmodel.AboutViewModel
import com.thiago.android.breakingnews.features.details.action.DetailsAction
import com.thiago.android.breakingnews.platform.Platform
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AboutScreen(
    onBackPress: () -> Unit
) {
    val viewModel = koinViewModel<AboutViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AboutScreenContent(onBackPress = onBackPress, state = state, action = viewModel::submitActions)

}

@Composable
fun AboutScreenContent (
    state: AboutState,
    onBackPress: () -> Unit,

    action: (AboutAction) -> Unit,

) {
    Column (modifier = Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {


            when(state) {
                is AboutState.Idle -> {}
                is AboutState.Loading -> {}
                is AboutState.OnBackPressed -> {
                    action(AboutAction.Idle)
                    onBackPress.invoke()
                }
            }

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
                action(AboutAction.OnBackPressed)
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