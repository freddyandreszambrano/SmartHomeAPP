package com.modelomatematico.smarthome.features.auth.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.modelomatematico.smarthome.databinding.ActivityLoginBinding
import com.modelomatematico.smarthome.features.home.view.ui.LoadingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signInButton.setOnClickListener {
            goToLoadingActivity()
        }

    }

    private fun goToLoadingActivity() {
        val i = Intent(this, LoadingActivity::class.java)
        startActivity(i)
        finish()
    }

}