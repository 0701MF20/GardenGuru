package com.example.android.gardenguru;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.gardenguru.provider.PlantContract;
import com.example.android.gardenguru.ui.MainActivity;
import com.example.android.gardenguru.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,long plantId,int imgRes,int appWidgetId,boolean waterYes) {
        //Getting widget bundle which have all information
        Bundle options=appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        RemoteViews views;
        if(width<300)
        {
            views=getSinglePlantRemoteView(context,imgRes,plantId,waterYes);
        }
        else
        {
            views=getGardenGridRemoteView(context);
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getGardenGridRemoteView(Context context) {
  return null;
    }

    private static RemoteViews getSinglePlantRemoteView(Context context, int imgRes, long plantId, boolean waterYes) {
        Intent intent;
        if(plantId== PlantContract.INVALID_PLANT_ID) {
            // Create an Intent to launch MainActivity when clicked
            intent = new Intent(context, MainActivity.class);
        }else
        {
            Log.d(PlantWidgetProvider.class.getSimpleName(), "plantId=" + plantId);
            intent=new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID,plantId);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget_provider);
        // Update image
        views.setImageViewResource(R.id.plant_widget_image, imgRes);
        //Update Plant ID Text
        views.setTextViewText(R.id.widget_plant_name,String.valueOf(plantId));
        //To set the visibility  of button
        if(waterYes)
        {
            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
        }
        else
        {
            views.setViewVisibility(R.id.widget_water_button,View.VISIBLE);
        } // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.plant_widget_image, pendingIntent);
        // Add the wateringservice click handler
        Intent wateringIntent = new Intent(context, PlantWateringService.class);
        wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        //setting the id of plant in which we need to water
        wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID,plantId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0, wateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);
    return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }
    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, int[] appWidgetIds,long plantId,boolean waterYes) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager,plantId,imgRes, appWidgetId,waterYes);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

    }
}