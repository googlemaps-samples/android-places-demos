package com.example.placedetailsuikit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.placedetailsuikit.compact.ConfigurablePlaceDetailsActivity
import com.example.placedetailsuikit.full.FullConfigurablePlaceDetailsActivity
import com.example.placedetailsuikit.ui.theme.PlaceDetailsUIKitTheme

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlaceDetailsUIKitTheme {
                LauncherScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Place Details UIKit Demos") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                context.startActivity(Intent(context, MainActivity::class.java))
            }) {
                Text("Main Activity")
            }
            Button(onClick = {
                context.startActivity(Intent(context, ConfigurablePlaceDetailsActivity::class.java))
            }) {
                Text("Compact Place Details")
            }
            Button(onClick = {
                context.startActivity(Intent(context, FullConfigurablePlaceDetailsActivity::class.java))
            }) {
                Text("Full Place Details")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LauncherScreenPreview() {
    PlaceDetailsUIKitTheme {
        LauncherScreen()
    }
}