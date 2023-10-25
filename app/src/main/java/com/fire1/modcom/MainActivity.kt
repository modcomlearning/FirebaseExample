package com.fire1.modcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fire1.modcom.adapters.PostsAdapter
import com.fire1.modcom.models.Post
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerPosts: RecyclerView
    private var mAdapter: PostsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Link Button to Post
        val post = findViewById<MaterialButton>(R.id.post_product)
        post.setOnClickListener {
            val i = Intent(applicationContext, PostActivity::class.java)
            startActivity(i)
        }


        recyclerPosts = findViewById(R.id.recycler)
        loadPosts()
    }//end oncreate

    private fun loadPosts() {
        val query= FirebaseDatabase.getInstance().reference.child("MODCOM").child("POSTS")
        val options = FirebaseRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        Log.d("taggg", ""+options)
        mAdapter = PostsAdapter(options)
        //we app;y in the context recyclerPosts
        recyclerPosts.apply {
            layoutManager = GridLayoutManager(this@MainActivity,2)
            adapter = mAdapter
        }
        //stop progress here
    }//end

    //Listen
    override fun onStart() {
        super.onStart()
        mAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (mAdapter != null) {
            mAdapter!!.stopListening()
        }
    }


}