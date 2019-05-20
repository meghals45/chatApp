package com.shah.megh.kotlinapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Adapter
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycleView_chat_log.adapter = adapter

        val user = intent.getParcelableExtra<Users>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user.username

      //  setUpDummyData()
        listernForMessages()

        button_chat_log.setOnClickListener {
            performSendMessage()
        }
    }
    private fun listernForMessages()
    {
        val ref = FirebaseDatabase.getInstance().getReference("/messages/")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
               val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage!= null) {
                    Log.d(TAG, chatMessage.text)
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.CurrentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text,currentUser))
                    }else{
                        val toUser = intent.getParcelableExtra<Users>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatToItem(chatMessage.text,toUser))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }


    class ChatMessage(val id: String,val text: String, val fromId: String , val toId: String , val timeStamp : Long)
    {
        constructor(): this("","","","",-1)
    }
    private fun performSendMessage()
    {
        //val reference = FirebaseDatabase.getInstance().getReference("/messages/").push()

        val text = editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<Users>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/User-messages/$fromId/$toId").push()
        val chatMessage = ChatMessage(reference.key!!,text,toId,fromId,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                if(chatMessage != null){
                    Log.d("ChatLog","Message stored successfully ${reference.key}")
                }
            }
    }
//    private fun setUpDummyData()
//    {
//        val Adapter = GroupAdapter<ViewHolder>()
//
//        Adapter.add(ChatToItem("To Message\nTo Message"))
//        Adapter.add(ChatFromItem("FROM MEEEEEESSAGEE...."))
//
//        recycleView_chat_log.adapter = Adapter
//    }
}

class ChatFromItem(val text: String,val user: Users): Item<ViewHolder>()
{
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.from_row_textview.text = text
        val url = user.profieImageUrl
        val targetImageUrl = viewHolder.itemView.imageView_from
        Picasso.get().load(url).into(targetImageUrl)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
class ChatToItem(val text: String,val user: Users): Item<ViewHolder>()
{
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.to_row_textview.text =text
        val url = user.profieImageUrl
        val targetImageUrl = viewHolder.itemView.imageView_to
        Picasso.get().load(url).into(targetImageUrl)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}