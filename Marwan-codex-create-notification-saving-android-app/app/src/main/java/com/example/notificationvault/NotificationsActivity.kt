package com.example.notificationvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity() {
    private lateinit var store: NotificationStore
    private lateinit var adapter: NotificationAdapter
    private var packageNameValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        packageNameValue = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
        store = NotificationStore(this)
        adapter = NotificationAdapter()

        findViewById<TextView>(R.id.notificationsHeader).text = packageNameValue

        val recycler = findViewById<RecyclerView>(R.id.notificationRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.submit(store.listNotifications(packageNameValue))
    }

    private class NotificationAdapter : RecyclerView.Adapter<NotificationViewHolder>() {
        private val items = mutableListOf<StoredNotification>()

        fun submit(newItems: List<StoredNotification>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    private class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.notificationTitle)
        private val textView: TextView = itemView.findViewById(R.id.notificationText)
        private val timeView: TextView = itemView.findViewById(R.id.notificationTime)

        fun bind(notification: StoredNotification) {
            titleView.text = notification.title
            textView.text = notification.text
            timeView.text = notification.formattedTime
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}
