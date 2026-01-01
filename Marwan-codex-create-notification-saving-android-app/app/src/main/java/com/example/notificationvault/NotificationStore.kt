package com.example.notificationvault

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationStore(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE packages (
                package_name TEXT PRIMARY KEY,
                table_name TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS packages")
        onCreate(db)
    }

    fun insertNotification(packageName: String, title: String?, text: String?, postedAt: Long, raw: String?) {
        val tableName = ensureTable(packageName)
        val values = ContentValues().apply {
            put("posted_at", postedAt)
            put("title", title ?: "")
            put("text", text ?: "")
            put("raw", raw ?: "")
        }
        writableDatabase.insert(tableName, null, values)
    }

    fun listPackages(): List<PackageSummary> {
        val db = readableDatabase
        val packages = mutableListOf<PackageSummary>()
        val cursor = db.query(
            "packages",
            arrayOf("package_name", "table_name"),
            null,
            null,
            null,
            null,
            "package_name ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val packageName = it.getString(0)
                val tableName = it.getString(1)
                val count = queryCount(tableName)
                packages.add(PackageSummary(packageName, count))
            }
        }
        return packages
    }

    fun listNotifications(packageName: String): List<StoredNotification> {
        val tableName = getTableName(packageName) ?: return emptyList()
        val db = readableDatabase
        val notifications = mutableListOf<StoredNotification>()
        val cursor = db.query(
            tableName,
            arrayOf("posted_at", "title", "text"),
            null,
            null,
            null,
            null,
            "posted_at DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val postedAt = it.getLong(0)
                val title = it.getString(1)
                val text = it.getString(2)
                notifications.add(StoredNotification(title, text, postedAt))
            }
        }
        return notifications
    }

    private fun queryCount(tableName: String): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $tableName", null)
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    private fun ensureTable(packageName: String): String {
        val existing = getTableName(packageName)
        if (existing != null) {
            return existing
        }
        val tableName = "pkg_${packageName.replace(".", "_")}".take(60)
        writableDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $tableName (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                posted_at INTEGER,
                title TEXT,
                text TEXT,
                raw TEXT
            )
            """.trimIndent()
        )
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("table_name", tableName)
        }
        writableDatabase.insert("packages", null, values)
        return tableName
    }

    private fun getTableName(packageName: String): String? {
        val cursor = readableDatabase.query(
            "packages",
            arrayOf("table_name"),
            "package_name = ?",
            arrayOf(packageName),
            null,
            null,
            null
        )
        cursor.use {
            return if (it.moveToFirst()) it.getString(0) else null
        }
    }

    companion object {
        private const val DATABASE_NAME = "notifications.db"
        private const val DATABASE_VERSION = 1
    }
}

data class PackageSummary(
    val packageName: String,
    val notificationCount: Int
)

data class StoredNotification(
    val title: String,
    val text: String,
    val postedAt: Long
) {
    val formattedTime: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(postedAt))
        }
}
