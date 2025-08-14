package com.example.location_share

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.location_share.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            val phone = binding.phoneNumberEditText.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show()
                binding.toggleButton.isChecked = false
                return@setOnCheckedChangeListener
            }

            if (isChecked) startWorker(phone)
            else stopWorker()
        }
    }

    private fun startWorker(phone: String) {
        val data = Data.Builder()
            .putString("PHONE_NUMBER", phone)
            .build()

        val request = PeriodicWorkRequestBuilder<LocationWorker>(1, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag("location_share")
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "location_share",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )

        Toast.makeText(this, "Sharing started", Toast.LENGTH_SHORT).show()
    }

    private fun stopWorker() {
        WorkManager.getInstance(this)
            .cancelUniqueWork("location_share")
        Toast.makeText(this, "Sharing stopped", Toast.LENGTH_SHORT).show()
    }
}
