package com.msd.stockaverage

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msd.stockaverage.domain.UIEvent
import com.msd.stockaverage.ui.theme.StockAverageTheme
import com.msd.stockaverage.util.TAG
import com.msd.stockaverage.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {

    private val requestWriteStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted -> if (isGranted)  Log.i(TAG, "Write Storage Permission Granted")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockAverageTheme {
                StockAverageHome()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }else {
            Log.i(TAG, "App has write permission")
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBitmapFromView(activity: Activity, callback: (Bitmap) -> Unit) {

        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(
                window.decorView.width,
                window.decorView.height,
                Bitmap.Config.ARGB_8888
            )
            try {
                Log.i(TAG, "Requesting pixel copy")
                PixelCopy.request(window, bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    } else {
                        Log.i(TAG, "Copy result is not success")
                    }
                    // possible to handle other result codes ...
                }, Handler(Looper.getMainLooper()))
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                Log.e(TAG, "PixelCopy IllegalArgumentException", e)
            }
        } ?: Log.i(TAG, "Window is null")
    }

   private fun getScreenshot(rootView: View, mainViewModel: MainViewModel) {
        var bitmap: Bitmap?
        rootView.let { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getBitmapFromView(this@MainActivity) {
                    Log.i(TAG, "Setting bitmap")
                    bitmap = it
                    mainViewModel.saveScreenshot(it, Date().toString() + mainViewModel.companyName)
                }
            } else {
                bitmap = getBitmapFromView(view)
                bitmap?.let {
                    mainViewModel.saveScreenshot(it, Date().toString() + mainViewModel.companyName)
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StockAverageHome(mainViewModel: MainViewModel = viewModel()) {
        val activity = LocalContext.current as Activity
        var scrollToPosition by remember { mutableStateOf(0F)}
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.app_name),
                            color = Color.White
                        )
                    },
                    contentColor = contentColorFor(backgroundColor),
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                scrollState.scrollTo(scrollToPosition.roundToInt())
                                getScreenshot(activity.window.decorView.rootView, mainViewModel)
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_screenshot_24),
                                contentDescription = stringResource(
                                    id = R.string.app_name
                                ),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                )
            },
            content = { innerPadding ->
                Box(modifier = Modifier
                    .padding(innerPadding)
                    .onGloballyPositioned { layoutCoordinates ->
                        scrollToPosition = layoutCoordinates.size.height.toFloat()

                    }) {
                    StockAverageContent()
                }
            }
        )
    }


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
                    label = resources.getString(R.string.label_company_name),
                    defaultInput = mainViewModel.companyName,
                    keyboardType = KeyboardType.Text,
                    onTextChanged = { mainViewModel.onEvent(UIEvent.CompanyNameChanged(it)) },
                    onNext = { localFocus.moveFocus(FocusDirection.Next) },
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
                            label = resources.getString(R.string.label_holding_quantity),
                            defaultInput = mainViewModel.holdingQuantity,
                            keyboardType = KeyboardType.Number,
                            onTextChanged = {
                                mainViewModel.onEvent(
                                    UIEvent.HoldingQuantityChanged(
                                        it
                                    )
                                )
                            },
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
                            label = resources.getString(R.string.label_purchase_price),
                            defaultInput = mainViewModel.purchasePrice,
                            keyboardType = KeyboardType.Decimal,
                            onTextChanged = { mainViewModel.onEvent(UIEvent.PurchasePriceChanged(it)) },
                            onNext = { localFocus.moveFocus(FocusDirection.Next) },
                            onDone = { localFocus.clearFocus() }
                        )
                    }

                }

                Spacer(modifier = Modifier.padding(4.dp))

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
                            label = resources.getString(R.string.label_new_quantity),
                            defaultInput = mainViewModel.newQuantity,
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
                            label = resources.getString(R.string.label_new_purchase_price),
                            defaultInput = mainViewModel.newPurchasePrice,
                            keyboardType = KeyboardType.Decimal,
                            onTextChanged = {
                                mainViewModel.onEvent(
                                    UIEvent.NewPurchasePriceChanged(
                                        it
                                    )
                                )
                            },
                            onNext = { localFocus.moveFocus(FocusDirection.Next) },
                            onDone = { localFocus.clearFocus() },
                            imeAction = ImeAction.Done
                        )
                    }

                }

                Spacer(modifier = Modifier.padding(4.dp))

                StockButtons()

                Spacer(modifier = Modifier.padding(4.dp))

                StockTextFieldOutput(text = "${resources.getString(R.string.label_total_buy_price)} ${mainViewModel.totalBuyPrice.value}")
                StockTextFieldOutput(text = "${resources.getString(R.string.label_total_shares)} ${mainViewModel.totalShares.value}")
                StockTextFieldOutput(text = "${resources.getString(R.string.label_average_price_per_share)} ${mainViewModel.averagePricePerShare.value}")

            }
        }
    }

    @Composable
    fun StockButtons(mainViewModel: MainViewModel = viewModel()) {
        val focusManager = LocalFocusManager.current

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.weight(1f), onClick = {
                focusManager.clearFocus()
                mainViewModel.onEvent(UIEvent.Calculate)
            }) {
                Text(text = resources.getString(R.string.label_calculate), color = MaterialTheme.colorScheme.onSecondary)
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Button(modifier = Modifier.weight(1f),  onClick = {
                focusManager.clearFocus()
                mainViewModel.onEvent(UIEvent.Reset)
            }) {
                Text(text = resources.getString(R.string.label_reset), color = MaterialTheme.colorScheme.onSecondary)
            }

        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StockTextFieldInput(
        label: String,
        defaultInput: State<String>,
        keyboardType: KeyboardType,
        onTextChanged: (String) -> Unit,
        onNext: (KeyboardActionScope) -> Unit,
        onDone: (KeyboardActionScope) -> Unit,
        imeAction: ImeAction = ImeAction.Next
    ) {
        TextField(
            value = defaultInput.value,
            onValueChange = {
                onTextChanged(it)
            },
            singleLine = true,
            label = {
                Text(
                    label,
                    fontSize = 10.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = onDone, onNext = onNext),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(4.dp))
    }


    @Composable
    fun StockTextFieldOutput(text: String) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.padding(4.dp))
    }

}




