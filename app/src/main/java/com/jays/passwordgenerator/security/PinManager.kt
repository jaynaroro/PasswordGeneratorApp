package com.jays.passwordgenerator.security

import android.content.Context
import java.security.MessageDigest

class PinManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(
        "pin_prefs",
        Context.MODE_PRIVATE
    )

    fun isPinSet(): Boolean {
        return prefs.contains("pin_hash")
    }

    fun savePin(pin: String){
        val hashedPin = hashPin(pin)

        prefs.edit()
            .putString("pin_hash", hashedPin)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = prefs.getString("pin_hash", null)
        return savedHash == hashPin(pin)
    }

    private fun hashPin(pin: String): String{
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

}