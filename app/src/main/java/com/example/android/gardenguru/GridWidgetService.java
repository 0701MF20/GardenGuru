package com.example.android.gardenguru;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.gardenguru.provider.PlantContract;
import com.example.android.gardenguru.ui.PlantDetailActivity;
import com.example.android.gardenguru.utils.PlantUtils;

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return null;
    }
    public static class GridViewFactory implements RemoteViewsFactory {
Context mContext;
Cursor mCursor;
        @Override
        public void onCreate() {

        }
//THIS WILL CALL WHEN NOTIFIED BY notifyAppWidgetViewChanged
        @Override
        public void onDataSetChanged() {
            Uri PLANT_URI= PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
            if(mCursor!=null)
                mCursor.close();
            mCursor=mContext.getContentResolver().query(PLANT_URI,null,null,null,PlantContract.PlantEntry.COLUMN_CREATION_TIME);

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
           if(mCursor==null||mCursor.getCount()==0)
            return 0;
           return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
          if(mCursor==null||mCursor.getCount()==0)
              return null;
          mCursor.moveToPosition(position);
          int idIndex=mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
          int createTimeIndex=mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
          int waterTimeIndex=mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
          int plantTypeIndex=mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
       long plantId=mCursor.getLong(idIndex);
       int plantType=mCursor.getInt(plantTypeIndex);
       long createdId=mCursor.getLong(createTimeIndex);
       long wateredAt=mCursor.getLong(waterTimeIndex);
       long timeNow=System.currentTimeMillis();
       RemoteViews views=new RemoteViews(mContext.getPackageName(),R.layout.widget_grid_view);
       //For getting the image
       int imgRes= PlantUtils.getPlantImageRes(mContext,timeNow-createdId,timeNow-wateredAt,plantType);
      views.setImageViewResource(R.id.plant_widget_image,imgRes);
       views.setTextViewText(R.id.widget_plant_name,String.valueOf(plantId));
        views.setViewVisibility(R.id.widget_water_button, View.GONE);
            // Fill in the onClick PendingIntent Template using the specific plant Id for each item individually
            Bundle extras = new Bundle();
            extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.widget_grid_view, fillInIntent);
       return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
