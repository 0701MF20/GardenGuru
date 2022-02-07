package com.example.android.gardenguru;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;


import com.example.android.gardenguru.provider.PlantContract;
import com.example.android.gardenguru.utils.PlantUtils;

import static com.example.android.gardenguru.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.gardenguru.provider.PlantContract.PATH_PLANTS;



import androidx.annotation.Nullable;
//Deprecated So please change it
public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.garden.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";
    public PlantWateringService() {
        super("PlantWateringSystem");
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
}
