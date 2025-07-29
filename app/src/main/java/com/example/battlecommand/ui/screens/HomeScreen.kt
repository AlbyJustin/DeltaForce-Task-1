package com.example.battlecommand.ui.screens

import android.graphics.drawable.ShapeDrawable
import android.media.tv.TvContract.Channels.Logo
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battlecommand.R


@Composable
fun HomeScreen(onPlayClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = "BATTLESHIPS ARMADA",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                lineHeight = 55.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)

            )
        }
        Box(modifier = Modifier.weight(2f)) {
            DrawCircle(Color.White, radius = 600f)
            DrawCircle(Color.LightGray, radius = 480f)
            DrawCircle(Color.White, radius = 190f)

            Button(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = CircleShape,
                border = null,
                onClick = { onPlayClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(120.dp)
                )
            }
        }
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxSize(),horizontalArrangement = Arrangement.SpaceEvenly) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                DrawCircle(Color.White, radius = 160f)
                Button(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.Center),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                    shape = CircleShape,
                    border = null,
                    onClick = { onPlayClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                DrawCircle(Color.White, radius = 160f)

                Button(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.Center),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                    shape = CircleShape,
                    border = null,
                    onClick = { onPlayClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawCircle(color: Color, modifier: Modifier = Modifier, radius: Float = 100f) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(color = color, radius = radius)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}