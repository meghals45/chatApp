package com.shah.megh.kotlinapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = email_editText2.text.toString()
        val password = password_editText4.text.toString()

        back_to_register_textView2.setOnClickListener {
            finish()
        }
    }
}
