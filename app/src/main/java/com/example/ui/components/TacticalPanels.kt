package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.WeatheredIvory

/**
 * Presentation primitives shared by the Tactical Evolution screens.
 *
 * They only lay out strings the domain packages already computed, so a card can
 * never disagree with the contract gate.
 */
@Composable
internal fun TacticalSectionCard(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.sp
            )
            MonoLabel(text = subtitle, size = 8)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
internal fun TacticalDetailRow(
    label: String,
    value: String,
    valueColor: Color = WeatheredIvory
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonoLabel(text = label, modifier = Modifier.weight(1f))
        MonoLabel(text = value, color = valueColor, weight = FontWeight.Bold)
    }
}

@Composable
internal fun MonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 9,
    color: Color = CoolSteel,
    weight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        fontSize = size.sp,
        fontWeight = weight,
        color = color
    )
}
