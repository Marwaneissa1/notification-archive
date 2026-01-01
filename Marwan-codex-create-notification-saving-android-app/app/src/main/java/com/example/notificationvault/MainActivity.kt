package com.example.notificationvault

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var store: NotificationStore
    private lateinit var adapter: PackageAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = NotificationStore(this)
        adapter = PackageAdapter { summary ->
            val intent = Intent(this, NotificationsActivity::class.java)
            intent.putExtra(NotificationsActivity.EXTRA_PACKAGE_NAME, summary.packageName)
            startActivity(intent)
        }

        val recycler = findViewById<RecyclerView>(R.id.packageRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val listenerButton = findViewById<MaterialButton>(R.id.enableListenerButton)
        listenerButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        val keepAliveIntent = Intent(this, KeepAliveService::class.java)
        ContextCompat.startForegroundService(this, keepAliveIntent)
    }

    override fun onResume() {
        super.onResume()
        requestPostNotificationsIfNeeded()
        refreshData()
    }

    private fun refreshData() {
        adapter.submit(store.listPackages())
    }

    private fun requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private class PackageAdapter(
        private val onClick: (PackageSummary) -> Unit
    ) : RecyclerView.Adapter<PackageViewHolder>() {
        private val items = mutableListOf<PackageSummary>()

        fun submit(newItems: List<PackageSummary>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_package, parent, false)
            return PackageViewHolder(view, onClick)
        }

        override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    private class PackageViewHolder(
        itemView: View,
        private val onClick: (PackageSummary) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val packageNameView: TextView = itemView.findViewById(R.id.packageName)
        private val packageCountView: TextView = itemView.findViewById(R.id.packageCount)
        private var currentSummary: PackageSummary? = null

        init {
            itemView.setOnClickListener {
                currentSummary?.let(onClick)
            }
        }

        fun bind(summary: PackageSummary) {
            currentSummary = summary
            packageNameView.text = summary.packageName
            packageCountView.text = itemView.context.getString(
                R.string.notification_count,
                summary.notificationCount
            )
        }
    }
}
