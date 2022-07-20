package com.msd.stockaverage

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msd.stockaverage.domain.UIEvent
import com.msd.stockaverage.ui.theme.StockAverageTheme
import com.msd.stockaverage.viewmodel.MainViewModel
import java.util.*


class MainActivity : ComponentActivity() {

   private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockAverageTheme(darkTheme = true) {
                StockAverageHome()
            }
        }
    }

    fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1], locationOfViewInWindow[0] + view.width, locationOfViewInWindow[1] + view.height), bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    }
                    // possible to handle other result codes ...
                }, Handler(Looper.getMainLooper()))
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }

    fun getScreenshot() {
        var bitmap:Bitmap? = null
        val rootView = this@MainActivity.currentFocus?.rootView
        rootView?.let { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getBitmapFromView(view, this@MainActivity) {
                    bitmap = it
                }
            } else {
                bitmap = getBitmapFromView(view)
            }

            bitmap?.let {
               val file = mainViewModel.saveScreenshot(it, Date().toString() + mainViewModel.companyName )
                mainViewModel.shareScreenshot(file)
            } ?: println("Bitmap is null")

        } ?: println("RootView is null")

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Preview
    fun StockAverageHome() {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.inversePrimary) },
                    contentColor = contentColorFor(backgroundColor),
                    actions = {
                        IconButton(onClick = {
                                getScreenshot()
                        }) {
                            Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = stringResource(
                                id = R.string.app_name
                            ), tint = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                )
            },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    StockAverageContent()
                }
            }
        )
    }


    //TODO:: Move composable functions to a separate file
    @Composable
    fun StockAverageContent(mainViewModel: MainViewModel = viewModel()) {

        val localFocus = LocalFocusManager.current

        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                Modifier
                    .padding(8.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                StockTextFieldInput(
                    label = "Company Name",
                    defaultInput = mainViewModel.companyName.value,
                    keyboardType = KeyboardType.Text,
                    onTextChanged = { mainViewModel.onEvent(UIEvent.CompanyNameChanged(it)) },
                    onNext = { localFocus.moveFocus(FocusDirection.Down) },
                    onDone = { localFocus.clearFocus() }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        StockTextFieldInput(
                            label = "Holding Quantity",
                            defaultInput = mainViewModel.holdingQuantity.value,
                            keyboardType = KeyboardType.Number,
                            onTextChanged = { mainViewModel.onEvent(UIEvent.HoldingQuantityChanged(it)) },
                            onNext = { localFocus.moveFocus(FocusDirection.Next) },
                            onDone = { localFocus.clearFocus() }
                        )
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        StockTextFieldInput(
                            label = "Purchase Price",
                            defaultInput = mainViewModel.purchasePrice.value,
                            keyboardType = KeyboardType.Decimal,
                            onTextChanged = { mainViewModel.onEvent(UIEvent.PurchasePriceChanged(it)) },
                            onNext = { localFocus.moveFocus(FocusDirection.Next) },
                            onDone = { localFocus.clearFocus() }
                        )
                    }

                }

                Spacer(modifier = Modifier.padding(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        StockTextFieldInput(
                            label = "New Quantity",
                            defaultInput = mainViewModel.newQuantity.value,
                            keyboardType = KeyboardType.Number,
                            onTextChanged = { mainViewModel.onEvent(UIEvent.NewQuantityChanged(it)) },
                            onNext = { localFocus.moveFocus(FocusDirection.Next) },
                            onDone = { localFocus.clearFocus() }
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {

                        StockTextFieldInput(
                            label = "New Purchase Price",
                            defaultInput = mainViewModel.newPurchasePrice.value,
                            keyboardType = KeyboardType.Decimal,
                            onTextChanged = { mainViewModel.onEvent(UIEvent.NewPurchasePriceChanged(it)) },
                            onNext = { localFocus.moveFocus(FocusDirection.Next) },
                            onDone = { localFocus.clearFocus() },
                            imeAction = ImeAction.Done
                        )
                    }

                }

                Spacer(modifier = Modifier.padding(8.dp))

                StockButtons()

                Spacer(modifier = Modifier.padding(8.dp))

                StockTextFieldOutput(text = "Total Buy Price: ${mainViewModel.totalBuyPrice.value}")
                StockTextFieldOutput(text = "Total Shares: ${mainViewModel.totalShares.value}")
                StockTextFieldOutput(text = "Average Price Per Share: ${mainViewModel.averagePricePerShare.value}")

            }
        }
    }

    @Composable
    fun StockButtons() {
        val focusManager = LocalFocusManager.current

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.weight(1f), onClick = {
                focusManager.clearFocus()
                mainViewModel.onEvent(UIEvent.Calculate)
            }) {
                Text(text = "Calculate")
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Button(modifier = Modifier.weight(1f), onClick = {
                focusManager.clearFocus()
                mainViewModel.onEvent(UIEvent.Reset)
            }) {
                Text(text = "Reset")
            }

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

        TextField(
            value = defaultInput,
            onValueChange = {
                onTextChanged(it)
            },

            singleLine = true,
            label = {
                Text(
                    label,
                    fontSize = 8.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
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

}




