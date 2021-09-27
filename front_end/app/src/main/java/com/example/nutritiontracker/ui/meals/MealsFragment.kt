package com.example.nutritiontracker.ui.meals

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.nutritiontracker.FoodCapture
import com.example.nutritiontracker.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import layout.MealRecylerView
import org.json.JSONArray
import org.json.JSONObject

class MealsFragment : Fragment() {

  private lateinit var homeViewModel: HomeViewModel
  var nameList = mutableListOf<String>()
  var caloriesList = mutableListOf<String>()

  private fun addMeals(meal: String, calories: String) {
    nameList.add(meal)
    caloriesList.add(calories)
  }

  private fun populateList() {
    val url = "http://192.168.0.28:3000"
    var queue = Volley.newRequestQueue(activity)
    var jsonBody = JSONObject()
    jsonBody.put("type","querymeals")

    val stringRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            Response.Listener { response ->
                var result = JSONObject(response.toString())
                Log.d("TAG","response: $response")
                Log.d("TAG","response: $result")
                for (i in 0..result.length()-1) {
                  var meal = response.getJSONObject(i.toString())
                  Log.d("TAG","response: $meal")
                  addMeals(meal.getString("name"),meal.getInt("calories").toString())
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

  private fun sumCalories(): Int{
    var totalCalories = 0
      for (i in caloriesList) {
          totalCalories += i.toInt()
      }
    return totalCalories
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_meals, container, false)
    var addMealButton: FloatingActionButton = root.findViewById(R.id.btnAddMeal)
    var txtTotalCalories: TextView = root.findViewById(R.id.txtTotalCalories)
    var mealRecyclerView: RecyclerView = root.findViewById(R.id.mealRecyclerView)
    populateList()
    sumCalories()

    Handler(Looper.getMainLooper()).postDelayed({
      txtTotalCalories.text = sumCalories().toString() + " calories total"
      mealRecyclerView.layoutManager = LinearLayoutManager(activity)
      mealRecyclerView.adapter = MealRecylerView(nameList, caloriesList)
    },3000)
    addMealButton.setOnClickListener{
      val intent = Intent(activity, FoodCapture::class.java)
      startActivity(intent)
    }

    return root
  }
}