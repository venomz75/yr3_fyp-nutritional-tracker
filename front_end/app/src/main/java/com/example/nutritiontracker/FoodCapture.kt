package com.example.nutritiontracker

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import org.json.JSONObject

class FoodCapture : AppCompatActivity() {
    private lateinit var scanner: CodeScanner //won't work without being initialised later.
    private val url = "http://192.168.0.28:3000"
    override fun onResume() {
        super.onResume()
        scanner.startPreview()
    }

    override fun onPause() {
        scanner.releaseResources()
        super.onPause()
    }

    private fun getCameraPermissions() {
        val perms = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        if (perms != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        }
    }

    private fun sendBarcodeData(barcode: String) {
        var queue = Volley.newRequestQueue(this)
        var jsonBody = JSONObject()
        jsonBody.put("type","barcode")
        jsonBody.put("barcode",barcode)

        val stringRequest = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                Response.Listener { response ->
                    var result = JSONObject(response.toString())
                    Log.d("TAG","response: $response")
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, ConfirmFood::class.java).apply {
                            putExtra("name",result.getString("name"))
                            putExtra("calories",result.getString("calories"))
                        }
                        startActivity(intent)
                    }, 2000)
                },
                Response.ErrorListener {
                    error ->
                    Log.d("TAG","response: ${error.message}")
                }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(stringRequest)
    }

    private fun doBarcodeScan() {
        val scannerView = findViewById<CodeScannerView>(R.id.barcodeCaptureView)

        scanner = CodeScanner(this, scannerView)
        scanner.apply{
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread{
                    sendBarcodeData(it.text)
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("FoodCapture","Camera error!")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_capture)

        getCameraPermissions()
        doBarcodeScan()
    }
}