package com.msd.stockaverage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msd.stockaverage.domain.UIEvent
import com.msd.stockaverage.ui.theme.StockAverageTheme
import com.msd.stockaverage.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //TODO:: Add app bar and screenshot functionality
            StockAverageTheme(darkTheme = true) {
                StockAverageHome()
            }
        }
    }
}

//TODO:: Move composable functions to a separate file
@Composable
fun StockAverageHome(mainViewModel: MainViewModel = viewModel()) {

    val localFocus = LocalFocusManager.current

    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .padding(8.dp)
                .fillMaxWidth()) {

            StockTextFieldInput(
                label ="Company Name",
                defaultInput = mainViewModel.companyName.value,
                keyboardType = KeyboardType.Text,
                onTextChanged = {mainViewModel.onEvent(UIEvent.CompanyNameChanged(it))},
                onNext = {localFocus.moveFocus(FocusDirection.Down)},
                onDone = {localFocus.clearFocus()}
            )

            StockTextFieldInput(
                label ="Holding Quantity",
                defaultInput = mainViewModel.holdingQuantity.value,
                keyboardType = KeyboardType.Number,
                onTextChanged = {mainViewModel.onEvent(UIEvent.HoldingQuantityChanged(it))},
                onNext = {localFocus.moveFocus(FocusDirection.Down)},
                onDone = {localFocus.clearFocus()}
            )

            StockTextFieldInput(
                label ="Purchase Price",
                defaultInput=mainViewModel.purchasePrice.value,
                keyboardType = KeyboardType.Decimal,
                onTextChanged = {mainViewModel.onEvent(UIEvent.PurchasePriceChanged(it))},
                onNext = {localFocus.moveFocus(FocusDirection.Down)},
                onDone = {localFocus.clearFocus()}
            )

            StockTextFieldInput(
                label ="New Quantity",
                defaultInput=mainViewModel.newQuantity.value,
                keyboardType = KeyboardType.Number,
                onTextChanged = {mainViewModel.onEvent(UIEvent.NewQuantityChanged(it))},
                onNext = {localFocus.moveFocus(FocusDirection.Down)},
                onDone = {localFocus.clearFocus()}
            )

            StockTextFieldInput(
                label ="New Purchase Price",
                defaultInput=mainViewModel.newPurchasePrice.value,
                keyboardType = KeyboardType.Decimal,
                onTextChanged = {mainViewModel.onEvent(UIEvent.NewPurchasePriceChanged(it))},
                onNext = {localFocus.moveFocus(FocusDirection.Down)},
                onDone = {localFocus.clearFocus()},
                imeAction = ImeAction.Done
            )


            StockButtons()

            Spacer(modifier = Modifier.padding(16.dp))

            StockTextFieldOutput(text = "Total Buy Price: ${mainViewModel.totalBuyPrice.value}")
            StockTextFieldOutput(text = "Total Shares: ${mainViewModel.totalShares.value}")
            StockTextFieldOutput(text = "Average Price Per Share: ${mainViewModel.averagePricePerShare.value}")

        }
    }
}

@Composable
fun StockButtons(mainViewModel: MainViewModel = viewModel()) {
    val focusManager = LocalFocusManager.current

    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = {
            focusManager.clearFocus()
            mainViewModel.onEvent(UIEvent.Calculate)
        }) {
            Text(text = "Calculate")
        }

        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = {
            focusManager.clearFocus()
            mainViewModel.onEvent(UIEvent.Reset)
        }) {
            Text(text="Reset")
        }

        Spacer(modifier = Modifier.padding(16.dp))
    }
}


@Composable
fun StockTextFieldInput(
    label: String,
    defaultInput: String,
    keyboardType: KeyboardType,
    onTextChanged: (String) -> Unit,
    onNext: (KeyboardActionScope) -> Unit,
    onDone: (KeyboardActionScope) -> Unit,
    imeAction: ImeAction = ImeAction.Next
) {
    println("Composing $label")

    TextField(
        value = defaultInput,
        onValueChange = {
            onTextChanged(it)
            println("value changed for $label to $it}")
        },
        singleLine = true,
        label = {
            Text(label)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = onDone, onNext = onNext),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.padding(8.dp))
}



@Composable
fun StockTextFieldOutput(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.padding(8.dp))
}
