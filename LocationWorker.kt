package com.example.location_share

import android.content.Context
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import kotlinx.coroutines.tasks.await

class LocationWorker(
    ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val phone = inputData.getString("PHONE_NUMBER") ?: return Result.failure()

        val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val loc: Location = fusedClient.lastLocation.await()
        val msgText = "Lat: ${loc.latitude}, Lon: ${loc.longitude} @ ${System.currentTimeMillis()}"

        return try {
            Twilio.init(
                BuildConfig.TWILIO_ACCOUNT_SID,
                BuildConfig.TWILIO_AUTH_TOKEN
            )
            Message.creator(
                PhoneNumber("whatsapp:$phone"),
                PhoneNumber(BuildConfig.TWILIO_FROM_NUMBER),
                msgText
            ).create()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
