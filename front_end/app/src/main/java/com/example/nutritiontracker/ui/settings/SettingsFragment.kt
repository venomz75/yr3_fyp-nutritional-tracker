package com.example.nutritiontracker.ui.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.nutritiontracker.R
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.json.JSONObject
import org.w3c.dom.Text

class SettingsFragment : Fragment() {

  private lateinit var notificationsViewModel: NotificationsViewModel
  private var pref = ""
  private fun getPref() {
    val url = "http://192.168.0.28:3000"
    var queue = Volley.newRequestQueue(activity)
    var jsonBody = JSONObject()
    jsonBody.put("type","getpref")

    val stringRequest = JsonObjectRequest(
      Request.Method.POST, url, jsonBody,
      Response.Listener { response ->
        var result = JSONObject(response.toString())
        Log.d("TAG","response: $response")
        Log.d("TAG","response: $result")
        pref = result.getString("pref")
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
    notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_settings, container, false)
    val txtStatus = root.findViewById<TextView>(R.id.txtStatus)
    val radioGroup = root.findViewById<RadioGroup>(R.id.rdgDiet)
    val radioRegular = root.findViewById<RadioButton>(R.id.rdRegular)
    val radioVegetarian = root.findViewById<RadioButton>(R.id.rdVegetarian)
    val radioVegan = root.findViewById<RadioButton>(R.id.rdVegan)
    val btnUpdate = root.findViewById<Button>(R.id.btnUpdatePref)
    getPref()

    Handler(Looper.getMainLooper()).postDelayed({
      if (pref == "regular") radioRegular.isChecked = true
      if (pref == "vegetarian") radioVegetarian.isChecked = true
      if (pref == "vegan") radioVegan.isChecked = true
      txtStatus.text = ""
    },3000)

    btnUpdate.setOnClickListener {
      val url = "http://192.168.0.28:3000"
      var queue = Volley.newRequestQueue(activity)
      var jsonBody = JSONObject()
      var newpref = ""
      if (radioRegular.isChecked) newpref = "regular"
      if (radioVegetarian.isChecked) newpref = "vegetarian"
      if (radioVegan.isChecked) newpref = "vegan"
      Log.d("TAG","response: $newpref")
      jsonBody.put("type","setpref")
      jsonBody.put("pref",newpref)

      val stringRequest = JsonObjectRequest(
        Request.Method.POST, url, jsonBody,
        Response.Listener { response ->
          var result = JSONObject(response.toString())
          Log.d("TAG","response: $response")
          txtStatus.text = "Updated successfully!"
        },
        Response.ErrorListener {
            error ->
          Log.d("TAG","response: ${error.message}")
        }
      )
      stringRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
      queue.add(stringRequest)
    }
    return root
  }
}