package com.modelomatematico.smarthome.features.auth.view.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.modelomatematico.smarthome.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize other components or observers here
    }

}