package com.univalle.equipocinco.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.univalle.equipocinco.R
import com.univalle.equipocinco.ui.MainActivity

/**
 * Clase encargada de manejar la vista y actualización del widget.
 */
object InventoryWidget {

    /**
     * Actualiza el contenido del widget.
     */
    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val widgetText = "Inventario actualizado"

        // Crea la vista remota del widget
        val views = RemoteViews(context.packageName, R.layout.inventory_widget)
        views.setTextViewText(R.id.tv_inventory_label, widgetText)

        // Configura un intent para abrir la app al tocar el widget
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.tv_inventory_label, pendingIntent)

        // Actualiza el widget en pantalla
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
