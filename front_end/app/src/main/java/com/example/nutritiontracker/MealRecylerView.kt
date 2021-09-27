package layout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritiontracker.R

class MealRecylerView (private var names: List<String>, private var calories: List<String>) : RecyclerView.Adapter<MealRecylerView.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodNames: TextView = view.findViewById(R.id.rTextFoodName)
        val foodCalories: TextView = view.findViewById(R.id.rTextFoodCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.meal_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        holder.foodNames.text = names[position]
        holder.foodCalories.text = calories[position]
    }

}