package com.akash.projectforinternshala

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akash.projectforinternshala.databinding.ActivityNumberBinding
import com.google.firebase.auth.FirebaseAuth

class NumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNumberBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNumberBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)

        if (auth.currentUser != null){
            val intent = Intent(this@NumberActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.sendOTPBtn.setOnClickListener {
            if(binding.phoneEditTextNumber.text!!.isEmpty()){
                Toast.makeText(this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show()
                binding.phoneProgressBar.visibility = View.VISIBLE
            } else{
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("number",binding.phoneEditTextNumber.text!!.toString())
                startActivity(intent)
                finish()
            }

        }
        binding.phoneProgressBar.visibility = View.INVISIBLE
    }
}