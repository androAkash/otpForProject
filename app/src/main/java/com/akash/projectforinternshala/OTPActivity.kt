package com.akash.projectforinternshala

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.akash.projectforinternshala.brodCastReceiver.MySMSBroadcastReceiver
import com.akash.projectforinternshala.databinding.ActivityOtpactivityBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity(), MySMSBroadcastReceiver.OTPReceiveListener {
    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var verificationId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private var smsReceiver: MySMSBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()

        startSMSListener()

        val builder = AlertDialog.Builder(this)

        builder.setMessage("Please Wait..")
        builder.setTitle("Loading")
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()

        val phoneNumber = "+91" + intent.getStringExtra("number")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(this@OTPActivity, "Please try again $p0", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)

                    dialog.dismiss()
                    verificationId = p0
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.btnOtpContinue.setOnClickListener {
            if (binding.otp.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                val credential =
                    PhoneAuthProvider.getCredential(verificationId, binding.otp.text!!.toString())
                auth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            dialog.dismiss()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            dialog.dismiss()
                            Toast.makeText(this, "Error ${it.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        setContentView(binding.root)
    }

    /**For autoFilling**/

    private fun startSMSListener() {
        try {
            smsReceiver = MySMSBroadcastReceiver()
            smsReceiver!!.initOTPListener(this)

            val intentFilter = IntentFilter()
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
            this.registerReceiver(smsReceiver, intentFilter)

            val client = SmsRetriever.getClient(this)

            val task = client.startSmsRetriever()
            task.addOnSuccessListener {
                // API successfully started
            }

            task.addOnFailureListener {
                // Fail to start API
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

//    fun onBtnResendClick(view: View){
//        startSMSListener()
//    }

    override fun onOTPReceived(otp: String) {
        //showToast("OTP Received: " + otp)
        binding.otp.setText(otp)
        if (smsReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(smsReceiver!!)
        }
    }

    override fun onOTPTimeOut() {
        Toast.makeText(this, "Time Out", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (smsReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(smsReceiver!!)
        }
    }
}