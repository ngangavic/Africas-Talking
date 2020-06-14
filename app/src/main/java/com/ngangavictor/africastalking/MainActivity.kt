package com.ngangavictor.africastalking

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.africastalking.AfricasTalking
import com.africastalking.PaymentService
import com.google.android.material.snackbar.Snackbar
import com.ngangavictor.africastalking.Secrets.Companion.API_KEY
import com.ngangavictor.africastalking.Secrets.Companion.USERNAME
import com.ngangavictor.africastalking.Secrets.Companion.productName
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var phoneInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var buttonPay: Button
    lateinit var alert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        phoneInput = findViewById(R.id.phoneInput)
        amountInput = findViewById(R.id.amountInput)
        buttonPay = findViewById(R.id.buttonPay)

        buttonPay.setOnClickListener { sendRequest() }

    }

    private fun sendRequest() {
        val phone = phoneInput.text.toString()
        val amount = amountInput.text.toString()

        if (TextUtils.isEmpty(phone)) {
            phoneInput.requestFocus()
            phoneInput.error = "Required"
        } else if (TextUtils.isEmpty(amount)) {
            amountInput.requestFocus()
            amountInput.error = "Required"
        } else if (!verifyPhone(phone)) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Invalid phone number. Required format +25471xxxxxxx",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            loadAlert()
            AfricasTalking.initialize(USERNAME, API_KEY)
            val payment =
                AfricasTalking.getService(AfricasTalking.SERVICE_PAYMENT) as PaymentService

            val productName = productName

            val currencyCode = "KES"

            val metadata = HashMap<String, String>()
            metadata["someKey"] = "someValue"
            try {
                alert.cancel()
                val response = payment.mobileCheckout(
                    productName, phone, currencyCode, amount.toFloat(), metadata
                )
                val jsonObject = JSONObject(response.toString())
                if (jsonObject.getString("status") == "PendingConfirmation") {
                    messageAlert("Request Successful", "Success")
                } else {
                    messageAlert("Request failed", "Error")
                }
                println(response.toString())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun loadAlert() {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(false)
        val progressBar = ProgressBar(this)
        alertBuilder.setView(progressBar)
        alert = alertBuilder.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()
    }

    private fun messageAlert(msg: String, title: String) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(false)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alert = alertBuilder.create()
        alert.show()
    }

    private fun verifyPhone(phone: String): Boolean {
        return phone.contains("+") && phone.length == 13
    }

}