package com.msd.stockaverage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msd.stockaverage.ui.theme.StockAverageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockAverageTheme(darkTheme = true) {
                StockAverageHome()
            }
        }
    }
}

@Composable
fun StockAverageHome() {
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
            StockTextFieldInput("Company Name", KeyboardType.Text)
            StockTextFieldInput("Holding Quantity", KeyboardType.Phone)
            StockTextFieldInput("Purchase Price", KeyboardType.Decimal)
            StockTextFieldInput("New Quantity", KeyboardType.Phone)
            StockTextFieldInput("New Purchase Price", KeyboardType.Decimal)

            StockButtons()

            Spacer(modifier = Modifier.padding(16.dp))

            StockTextFieldOutput(text = "Total Buy Price: ")
            StockTextFieldOutput(text = "Total Shares: ")
            StockTextFieldOutput(text = "Average Price Per Share: ")

        }
    }
}

@Composable
fun StockButtons() {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { /*TODO*/ }) {
            Text(text = "Calculate")
        }

        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = { /*TODO*/ }) {
            Text(text="Reset")
        }

        Spacer(modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun StockTextFieldInput(label: String, keyboardType: KeyboardType) {
    var input by remember{mutableStateOf(TextFieldValue(""))}
    TextField(
        value = input,
        onValueChange = {
            input = it
        },
        singleLine = true,
        label = {
            Text(label)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.padding(16.dp))
}

@Composable
fun StockTextFieldOutput(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.padding(16.dp))
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StockAverageTheme(darkTheme = true) {
        StockAverageHome()
    }
}