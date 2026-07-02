package com.example

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun PortfolioStatsChart(profits: List<Float>) {
    // If empty, show a flat line or default data
    val entries = if (profits.isEmpty()) {
        entryModelOf(0f, 0f)
    } else {
        // Create an entry model mapping index to profit accumulation
        val data = mutableListOf<Float>()
        var acc = 0f
        data.add(acc)
        profits.forEach { 
            acc += it
            data.add(acc) 
        }
        entryModelOf(*data.toTypedArray())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Chart(
            chart = lineChart(),
            model = entries,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
