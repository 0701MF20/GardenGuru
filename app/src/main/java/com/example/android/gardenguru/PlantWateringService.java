package com.example.android.gardenguru;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;


import com.example.android.gardenguru.provider.PlantContract;
import com.example.android.gardenguru.utils.PlantUtils;

import static com.example.android.gardenguru.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.gardenguru.provider.PlantContract.PATH_PLANTS;



import androidx.annotation.Nullable;
//Deprecated So please change it
public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.gardenguru.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.gardenguru.action.update_plant_widgets";
    public PlantWateringService() {
        super("PlantWateringSystem");
    }

    public static void  startActionWaterPlant(Context context)
    {
        Intent intent=new Intent(context,PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }
    //Starts this service to perform UpdatePlantWidgets action with the given parameters
    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
if(intent!=null)
{
    String action=intent.getAction();
    if(ACTION_WATER_PLANTS.equals(action))
    {
        handleActionWaterPlants();
    }
    else if(ACTION_UPDATE_PLANT_WIDGETS.equals(action))
    {
        handleActionUpdatePlantWidgets();
    }
}
    }

    private void handleActionWaterPlants() {
        Uri PLANTS_URI=BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        ContentValues contentValues=new ContentValues();
        long timesNow=System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME,timesNow);
        getContentResolver().update(PLANTS_URI,contentValues, PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",new String[]{String.valueOf(timesNow-PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }
    //Handle action UpdatePlantWidgets in the provided background thread
    private void handleActionUpdatePlantWidgets() {
        //Query to get the plant that's most in need for water (last watered)
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME
        );
        // Extract the plant details
        int imgRes = R.drawable.grass; // Default image in case our garden is empty
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);
            cursor.close();
            imgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
        //Now update all widgets
        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, appWidgetIds);
    }
}
