package com.example.nutritiontracker

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ConfirmFood : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_food)
        val url = "http://192.168.0.28:3000"
        val text = findViewById<TextView>(R.id.txtFoodData)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        text.text = intent.getStringExtra("name")+"\n"+intent.getStringExtra("calories")+" calories"

        btnConfirm.setOnClickListener {
            var queue = Volley.newRequestQueue(this)
            var jsonBody = JSONObject()
            jsonBody.put("type","addmeal")
            jsonBody.put("name",intent.getStringExtra("name"))
            jsonBody.put("calories",intent.getStringExtra("calories").toInt())

            val stringRequest = JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    Response.Listener { response ->
                        var result = JSONObject(response.toString())
                        Log.d("TAG","response: $response")
                        Toast.makeText(this, "Successfully added item!", Toast.LENGTH_LONG).show()
                    },
                    Response.ErrorListener {
                        error ->
                        Log.d("TAG","response: ${error.message}")
                        Toast.makeText(this, "Failed to add item... try again? ", Toast.LENGTH_LONG).show()
                    }
            )
            stringRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)

            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
}