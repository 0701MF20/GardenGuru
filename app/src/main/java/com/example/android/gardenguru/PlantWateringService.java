package com.example.android.gardenguru;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.SimpleCursorAdapter;


import com.example.android.gardenguru.provider.PlantContract;
import com.example.android.gardenguru.utils.PlantUtils;

import static com.example.android.gardenguru.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.gardenguru.provider.PlantContract.PATH_PLANTS;



import androidx.annotation.Nullable;
//Deprecated So please change it
public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.garden.action.water_plants";
    public static final String ACTION_WATER_UPDATE_PLANTS = "com.example.android.garden.action.update_plants_widget";

    public PlantWateringService() {
        super("PlantWateringSystem");
    }
    public static void  startActivityUpdatePlantWidget(Context context)
    {
        Intent intent=new Intent(context,PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }
    public static void  startActivityWaterPlant(Context context)
    {
        Intent intent=new Intent(context,PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
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
    }else if(ACTION_WATER_UPDATE_PLANTS.equals(action))
    {
        handleActionUpdatePlantWidget();
    }
}
    }
//To perform the action related to watering and update te current time which is left by updating the database record time
    private void handleActionWaterPlants() {
        Uri PLANTS_URI=BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        ContentValues contentValues=new ContentValues();
        long timesNow=System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME,timesNow);
        getContentResolver().update(PLANTS_URI,contentValues, PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",new String[]{String.valueOf(timesNow-PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }
    //To update the record of widget
    private void handleActionUpdatePlantWidget()
    {
        Uri PLANTS_URI= BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor=getContentResolver().query(PLANTS_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        //ExtraCT THE CURSOR data entries
        int imgRes=R.drawable.grass;//Default image if our garden is empty
        if(cursor!=null&&cursor.getCount()>0)
        {
            cursor.moveToFirst();
            int createTimeIndex=cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
                    int waterTimeIndex=cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
                          int plantTypeIndex=cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
                          long timeNow=System.currentTimeMillis();
                          long wateredAt=cursor.getLong(waterTimeIndex);
                          long createdAt=cursor.getLong(createTimeIndex);
                          int plantType=cursor.getInt(plantTypeIndex);
                          cursor.close();
                          //Assingning the image to show in widget which get whatered
                          imgRes=PlantUtils.getPlantImageRes(this,timeNow-createdAt,timeNow-wateredAt,plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
        //Now update all widgets
        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, appWidgetIds);
    }
}
