package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

//COMPLETED (2): Create a RemoteViewsService class and a RemoteViewsFactory class with:
//       - onDataSetChanged querying the list of all plants in the database
//       - getViewAt creating a RemoteView using the plant_widget layout
//       - getViewAt setting the fillInIntent for widget_plant_image with the plant ID as extras

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    Cursor mCursor;

    // The constructor is needed to access the ContentResolver
    public GridRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    // This is called on start and when notifyAppWidgetViewDataChanged
    // It's the equivalent of calling notifyDataSetChanged in a cursor adapter
    @Override
    public void onDataSetChanged() {
        // Get all plat info, in order of creation time
        Uri PLANT_URI = PlantContract.PlantEntry.CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
        if (mCursor != null) mCursor.close();
        mCursor = mContext.getContentResolver().query(PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME);
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    // This is similar to the OnBindViewHolder of a typical adapter
    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);
        int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int createTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        long plantId = mCursor.getLong(idIndex);
        int plantType = mCursor.getInt(plantTypeIndex);
        long createdAt = mCursor.getLong(createTimeIndex);
        long wateredAt = mCursor.getLong(waterTimeIndex);

        long timeNow = System.currentTimeMillis();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        // Update the plant image
        int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt,
                timeNow - wateredAt, plantType);
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        // Hide the water drop in GridView mode
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        // To be able to click the views in the GridLayout and launch the PlantDetailActivity, we
        // have to use a FillInIntent, passing the specific plant id of the item clicked
        Intent fillInIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

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
        return 1; // treat all items in the GridView the same
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
