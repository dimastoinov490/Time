package com.neo.clock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuRed
import com.neo.clock.ui.theme.NeuText

@Composable
fun PermissionBanner(
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    onRequestNotification: () -> Unit,
    onRequestExactAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val needsNotification = !hasNotificationPermission
    val needsExactAlarm = !hasExactAlarmPermission

    AnimatedVisibility(visible = needsNotification || needsExactAlarm, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .neuRaised(cornerRadius = 16.dp)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = NeuRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (needsExactAlarm) "Точные будильники отключены" else "Уведомления отключены",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NeuText,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (needsExactAlarm) "Разрешите точные сигналы для своевременного срабатывания" else "Включите уведомления, чтобы не пропустить звонок",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeuMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .padding(horizontal = 4.dp)
                        .neuClickable(
                            cornerRadius = 12.dp,
                            onClick = if (needsExactAlarm) onRequestExactAlarm else onRequestNotification
                        )
                        .testTag("enable_permission_button")
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Включить",
                        color = NeuBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
