package com.univalle.equipocinco.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.univalle.equipocinco.R
import com.univalle.equipocinco.ui.MainActivity
import java.text.NumberFormat
import java.util.*

class InventoryWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_BALANCE = "com.univalle.equipocinco.TOGGLE_BALANCE"
        private const val ACTION_MANAGE_INVENTORY = "com.univalle.equipocinco.MANAGE_INVENTORY"
        private const val PREFS_NAME = "inventory_widget_prefs"
        private const val PREF_SHOW_BALANCE = "show_balance"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, InventoryWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(component)

        when (intent.action) {
            ACTION_TOGGLE_BALANCE -> {
                handleToggleBalance(context, appWidgetManager, appWidgetIds)
            }
            ACTION_MANAGE_INVENTORY -> {
                handleManageInventory(context)
            }
        }
    }

    private fun handleToggleBalance(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // Usuario NO logueado → dirigir a Login
            openLoginActivity(context)
            return
        }

        // Usuario logueado → toggle balance
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentShowBalance = prefs.getBoolean(PREF_SHOW_BALANCE, false)
        prefs.edit().putBoolean(PREF_SHOW_BALANCE, !currentShowBalance).apply()

        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    private fun handleManageInventory(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // Ir al login, pero después del login debe ir al Home
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("destination", "login")
                putExtra("fromWidget", false)
                putExtra("goHomeAfterLogin", true)
            }
            context.startActivity(intent)
        } else {
            openHomeActivity(context)
        }
    }

    private fun openLoginActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "login")
            putExtra("fromWidget", true)
            putExtra("goHomeAfterLogin", false)
        }
        context.startActivity(intent)
    }

    private fun openHomeActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "home")
            putExtra("fromWidget", true)
            putExtra("goHomeAfterLogin", true)
        }
        context.startActivity(intent)
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.inventory_widget)

        // Configurar botón toggle balance (ojo)
        val toggleIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_BALANCE
        }
        val togglePending = PendingIntent.getBroadcast(
            context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.img_eye, togglePending)

        // Configurar botón gestionar inventario
        val manageIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
            action = ACTION_MANAGE_INVENTORY
        }
        val managePending = PendingIntent.getBroadcast(
            context, 1, manageIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.ll_manage, managePending)
        views.setOnClickPendingIntent(R.id.img_manage, managePending)

        // Mostrar/Ocultar balance
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val showBalance = prefs.getBoolean(PREF_SHOW_BALANCE, false)

        if (showBalance) {
            views.setImageViewResource(R.id.img_eye, R.drawable.ic_eye_closed)
            loadBalanceFromFirestore(context) { balance ->
                views.setTextViewText(R.id.tv_saldo, balance)
                manager.updateAppWidget(widgetId, views)
            }
        } else {
            views.setImageViewResource(R.id.img_eye, R.drawable.ic_eye_open)
            views.setTextViewText(R.id.tv_saldo, "$ ****")
            manager.updateAppWidget(widgetId, views)
        }
    }

    private fun loadBalanceFromFirestore(context: Context, callback: (String) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            callback("$ 0,00")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(userId)
            .collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val total = snapshot.documents.sumOf { doc ->
                    val price = doc.getDouble("price") ?: 0.0
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    price * quantity
                }
                val formatted = formatCurrency(total)
                callback(formatted)
            }
            .addOnFailureListener {
                callback("$ 0,00")
            }
    }

    private fun formatCurrency(value: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return "$ " + nf.format(value)
    }
}