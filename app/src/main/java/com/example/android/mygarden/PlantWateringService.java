package com.example.android.mygarden;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

// COMPLETEDß (2): Create a plant watering service that extends IntentService and supports the
//           action ACTION_WATER_PLANTS which updates last_watered timestamp for all plants still alive
public class PlantWateringService extends IntentService {
    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";

    public PlantWateringService() {
        // We have to have an empty constructor with the name of the Service,
        // which is used to name the worker thread (important only for debugging)
        super("PlantWateringService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            // We check if the intent action is the one we want
            final String action = intent.getAction();
            if (ACTION_WATER_PLANTS.equals(action)) {
                // If so, we water the plants
                handleActionWaterPlants();
            }
        }
    }

    private void handleActionWaterPlants() {
        Uri PLANTS_URI = PlantContract.PlantEntry.CONTENT_URI;
        ContentValues values = new ContentValues();
        long timeNow = System.currentTimeMillis();
        // We want to update the last watered time to the current
        values.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // We want to do this only for the plants that are "alive", which means that we have to see
        // if the difference between the current time and the last time watered is bigger than
        // the MAX_AGE_WITHOUT_WATER
        getContentResolver().update(PLANTS_URI,
                values,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }
}
