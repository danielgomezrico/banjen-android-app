package com.makingiants.android.banjotuner

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding

class TunerWidget : GlanceAppWidget() {
    companion object {
        val STRING_INDEX_KEY = ActionParameters.Key<Int>("string_index")
        val STRING_LABELS = listOf("4 - D", "3 - G", "2 - B", "1 - D")
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        Row(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            STRING_LABELS.forEachIndexed { index, label ->
                Button(
                    text = label,
                    onClick =
                        actionStartActivity<EarActivity>(
                            actionParametersOf(STRING_INDEX_KEY to index),
                        ),
                    modifier = GlanceModifier.defaultWeight().padding(2.dp),
                )
            }
        }
    }
}

class TunerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TunerWidget()
}
