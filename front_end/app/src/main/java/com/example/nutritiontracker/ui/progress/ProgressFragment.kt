package com.example.nutritiontracker.ui.progress

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.nutritiontracker.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.textfield.TextInputEditText
import layout.MealRecylerView
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

class ProgressFragment : Fragment() {

  private lateinit var dashboardViewModel: DashboardViewModel
  private var datapoints = ArrayList<Entry>()
  private var recommendation = ""

  private fun writeDataPoints(weight: Float, date: LocalDate) {
    var newdate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    Log.d("TAG","response: $newdate")
    datapoints.add(Entry(newdate.time.toFloat(),weight))
  }

  private fun getData() {
      val url = "http://192.168.0.28:3000"
      var queue = Volley.newRequestQueue(activity)
      var jsonBody = JSONObject()
      jsonBody.put("type","queryweights")

      val stringRequest = JsonObjectRequest(
          Request.Method.POST, url, jsonBody,
          Response.Listener { response ->
              var result = JSONObject(response.toString())
              Log.d("TAG","response: $response")
              Log.d("TAG","response: $result")
              for (i in 0..result.length()-1) {
                  var weight = response.getJSONObject(i.toString())
                  Log.d("TAG","response: $weight")
                  writeDataPoints(weight.getDouble("weight").toFloat(),LocalDate.parse(weight.getString("date").substring(0,10))  )
              }
          },
          Response.ErrorListener {
              error ->
            Log.d("TAG","response: ${error.message}")
          }
      )
      stringRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
      queue.add(stringRequest)
  }


  private fun sendData(weight: Int){
      val url = "http://192.168.0.28:3000"
      var queue = Volley.newRequestQueue(activity)
      var jsonBody = JSONObject()
      jsonBody.put("type","addweight")
      jsonBody.put("weight",weight)

      val stringRequest = JsonObjectRequest(
          Request.Method.POST, url, jsonBody,
          Response.Listener { response ->
              var result = JSONObject(response.toString())
              Log.d("TAG","response: $response")
              Log.d("TAG","response: $result")
          },
          Response.ErrorListener {
                  error ->
              Log.d("TAG","response: ${error.message}")
          }
      )
      stringRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
      queue.add(stringRequest)
  }

    private fun getRecommendation() {
        val url = "http://192.168.0.28:3000"
        var queue = Volley.newRequestQueue(activity)
        var jsonBody = JSONObject()
        jsonBody.put("type","recommend")

        val stringRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            Response.Listener { response ->
                var result = JSONObject(response.toString())
                Log.d("TAG","response: $response")
                Log.d("TAG","response: $result")
                recommendation = response.getString("recommendation")
            },
            Response.ErrorListener {
                    error ->
                Log.d("TAG","response: ${error.message}")
            }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(stringRequest)

    }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
      dashboardViewModel =
              ViewModelProvider(this).get(DashboardViewModel::class.java)
      val root = inflater.inflate(R.layout.fragment_progress, container, false)

      val graph = root.findViewById<LineChart>(R.id.graphProgress)
      val inputWeight = root.findViewById<TextInputEditText>(R.id.txtinputWeight)
      val submitButton = root.findViewById<Button>(R.id.btnAddWeight)
      val recommendText = root.findViewById<TextView>(R.id.txtSuggestion)


      getData()

      Handler(Looper.getMainLooper()).postDelayed({
          var dataset = LineData(LineDataSet(datapoints, "Weight"))
          graph.data=dataset
          graph.notifyDataSetChanged()
          graph.invalidate()
          getRecommendation()
          Handler(Looper.getMainLooper()).postDelayed({
                recommendText.text = "Why not try "+recommendation+"?"
          },5000)
      },5000)



      submitButton.setOnClickListener {
          var weight = inputWeight.text.toString()
          sendData(weight.toInt())
          getData()
          Handler(Looper.getMainLooper()).postDelayed({
              graph.notifyDataSetChanged()
              graph.invalidate()
          }, 5000)
          graph.notifyDataSetChanged()
          graph.invalidate()
      }
      return root
  }
}