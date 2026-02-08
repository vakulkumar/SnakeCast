package com.snakecast.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakecast.mobile.controller.ControlMode
import com.snakecast.mobile.ui.theme.AccentBlue
import com.snakecast.mobile.ui.theme.CardDark
import com.snakecast.mobile.ui.theme.SnakeGreen
import com.snakecast.mobile.ui.theme.TextGray
import com.snakecast.mobile.ui.theme.TextWhite

/**
 * Settings panel for control mode switching.
 */
@Composable
fun SettingsPanel(
    currentMode: ControlMode,
    onModeChanged: (ControlMode) -> Unit,
    isMotionAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Control Settings",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Motion Controls",
                    fontSize = 14.sp,
                    color = TextWhite
                )
                Text(
                    text = if (isMotionAvailable) "Tilt your phone to control" else "Accelerometer not available",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            
            Switch(
                checked = currentMode == ControlMode.MOTION,
                onCheckedChange = { isMotion ->
                    onModeChanged(if (isMotion) ControlMode.MOTION else ControlMode.JOYSTICK)
                },
                enabled = isMotionAvailable,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SnakeGreen,
                    checkedTrackColor = SnakeGreen.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = TextGray.copy(alpha = 0.3f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Current mode indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active: ",
                fontSize = 12.sp,
                color = TextGray
            )
            Text(
                text = when (currentMode) {
                    ControlMode.JOYSTICK -> "🎮 D-Pad"
                    ControlMode.MOTION -> "📱 Motion"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
            )
        }
    }
}
