package com.fire1.modcom

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class PostActivity : AppCompatActivity() {
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var ivImage: ImageView
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private  var imageUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar = findViewById(R.id.progressbar)
        ivImage = findViewById(R.id.ivImage)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescripion)

        storageReference = FirebaseStorage.getInstance().reference.child("MODCOM/IMAGES").child("${System.currentTimeMillis()}"+".jpg")
        databaseReference = FirebaseDatabase.getInstance().reference.child("MODCOM/POSTS")
        //When taping on the image view we created above, we shiuld get to the gallery, This is achieved by the following code, add it after the above code

        ivImage.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.setType("image/*")
            startActivityForResult(galleryIntent,100)
        }

    }//end oncreate
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK){
            imageUri = data?.data!!
            ivImage.setImageURI(imageUri)
        }
    }//end
    fun postItem(view: View) {
        progressBar.visibility = View.VISIBLE
        val title = etTitle.text.toString()
        val descrition = etDescription.text.toString()
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(descrition)){
            //ready to post
            var imageurl = "default"
            if (imageUri !=null){
                //image  supplied
                val uploadTask: UploadTask = storageReference.putFile(imageUri!!)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        uploadToFirebase(title = title,description= descrition,imageurl = downloadUri.toString())

                    } else {
                        Toast.makeText(this, "Something went wrong while uploading image", Toast.LENGTH_SHORT).show()
                    }
                }

            }else{
                //image not supplied
                progressBar.visibility = View.VISIBLE
                uploadToFirebase(title = title,description= descrition,imageurl = imageurl)

            }
        }

        else {
            Toast.makeText(applicationContext, "Empty Fields", Toast.LENGTH_SHORT).show()
        }

    }//end Post
    private fun uploadToFirebase(title:String, description:String,  imageurl:String){
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["timestamp"] = System.currentTimeMillis()
        hashMap["image"]=imageurl
        //random key
        val postid = databaseReference.push().key.toString()
        databaseReference.child(postid).updateChildren(hashMap).addOnCompleteListener{
            if (it.isSuccessful){
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Posted Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }else{
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error:${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}