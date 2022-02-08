package com.example.android.gardenguru;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;


import com.example.android.gardenguru.provider.PlantContract;
import com.example.android.gardenguru.utils.PlantUtils;

import static com.example.android.gardenguru.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.gardenguru.provider.PlantContract.INVALID_PLANT_ID;
import static com.example.android.gardenguru.provider.PlantContract.PATH_PLANTS;



import androidx.annotation.Nullable;
//Deprecated So please change it
public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANT = "com.example.android.gardenguru.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.gardenguru.action.update_plant_widgets";
    public static final String EXTRA_PLANT_ID = "com.example.android.gardenguru.extra.PLANT_ID";
    public PlantWateringService() {
        super("PlantWateringSystem");
    }

    public static void  startActionWaterPlant(Context context,long plantId)
    {
        Intent intent=new Intent(context,PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANT);
        intent.putExtra(EXTRA_PLANT_ID,plantId);
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
    if(ACTION_WATER_PLANT.equals(action))
    {
        long plantId=intent.getLongExtra(EXTRA_PLANT_ID, INVALID_PLANT_ID);
        handleActionWaterPlant(plantId);
    }
    else if(ACTION_UPDATE_PLANT_WIDGETS.equals(action))
    {
        handleActionUpdatePlantWidgets();
    }
}
    }

    private void handleActionWaterPlant(long plantId) {
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), plantId);
        ContentValues contentValues=new ContentValues();
        long timesNow=System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME,timesNow);
        getContentResolver().update(SINGLE_PLANT_URI,contentValues, PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",new String[]{String.valueOf(timesNow-PlantUtils.MAX_AGE_WITHOUT_WATER)});
   startActionUpdatePlantWidgets(this);
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
        boolean canWater = false; // Default to hide the water drop button
        long plantId = INVALID_PLANT_ID;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex(PlantContract.PlantEntry._ID);
            plantId=cursor.getLong(idIndex);
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);
            cursor.close();
            canWater = (timeNow - wateredAt) > PlantUtils.MIN_AGE_BETWEEN_WATER &&
                    (timeNow - wateredAt) < PlantUtils.MAX_AGE_WITHOUT_WATER;
            imgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
        //Now update all widgets
        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager,imgRes,appWidgetIds,plantId,canWater);
    }
}
