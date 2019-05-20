package com.shah.megh.kotlinapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Register_button.setOnClickListener {
            val email = Email_editText3.text.toString()
            val password = Password_editText4.text.toString()


            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter valid text in above field.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                    Log.d("main","Successfully created user with id ${it.result.user.uid}")

                    uploadImageToFirebaseStorage()

            }
                .addOnFailureListener(){
                    Log.d("main","Failed to create User ${it.message}")
                }

        }

        already_have_acc_textView.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        select_photo_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }
    var selectedphotouri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            //proceed and check what the selected image was..
            Log.d("Main","photo selected")
            selectedphotouri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedphotouri)

            selectphoto_imageview.setImageBitmap(bitmap)
            select_photo_button.alpha =0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            select_photo_button.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage(){
        if(selectedphotouri == null ) return

        val filename =  UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedphotouri!!)
            .addOnSuccessListener {
                Log.d("MainActivity","Successfully upload image : ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("MainActivity","File Location:$it")

                    saveUserToFirebaseStorage(it.toString())
                }
            }
            .addOnFailureListener{

            }
    }

    private  fun saveUserToFirebaseStorage(profieImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user1 = Users(uid,Username_editText.text.toString(),profieImageUrl)

        ref.setValue(user1)
            .addOnSuccessListener {
                Log.d("MainActivity","User Saved to FirebaseDatabase")

                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}
@Parcelize
class Users(val uid:String , val username: String , val profieImageUrl: String): Parcelable {
    constructor() : this("","","")
}