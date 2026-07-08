package com.example.calculator

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvExpression = findViewById<TextView>(R.id.tvExpression)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnToggle = findViewById<Button>(R.id.btnToggleMode)

        viewModel.expression.observe(this) { tvExpression.text = it }
        viewModel.result.observe(this) { tvResult.text = it }

        val isDemo = BuildConfig.FLAVOR == "demo"
        val orientation = resources.configuration.orientation

        if (isDemo) {
            btnToggle.visibility = View.GONE
            showEngineering(false)
        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                btnToggle.visibility = View.GONE
                showEngineering(true)
            } else {
                btnToggle.visibility = View.VISIBLE
                btnToggle.setOnClickListener {
                    val container = findViewById<FrameLayout>(R.id.engineeringContainer)
                    showEngineering(container.visibility == View.GONE)
                }
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.basicContainer, BasicFragment())
            .commit()
    }

    private fun showEngineering(show: Boolean) {
        val container = findViewById<FrameLayout>(R.id.engineeringContainer)
        if (show) {
            container.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.engineeringContainer, EngineeringFragment())
                .commit()
        } else {
            container.visibility = View.GONE
            val fragment = supportFragmentManager.findFragmentById(R.id.engineeringContainer)
            if (fragment != null) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
        }
    }
}