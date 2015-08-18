/**
 * Copyright (c) 2015 BlackBerry Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afrolkin.samplepushclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import static com.afrolkin.samplepushclient.CommonUtilities.SENDER_ID;

public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super(SENDER_ID);
    }

    private static final String TAG = "===GCMIntentService===";

    // Callback which gets fired when device is registered with GCM
    @Override
    protected void onRegistered(Context arg0, String registrationId) {
        Log.i(TAG, "Device registered: " + registrationId);

        // Send local broadcast to notify main activity of successful registration
        Intent intent = new Intent(getString(R.string.register_status_action));
        intent.putExtra(getString(R.string.register_status_extra), true);
        intent.putExtra(getString(R.string.error_extra), false);
        intent.putExtra(getString(R.string.registration_id_extra), registrationId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Callback which gets fired when device is unregistered from GCM
    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        Log.i(TAG, "Device unregistered: " + arg1);

        // Send local broadcast to notify main activity of successful unregistration
        Intent intent = new Intent(getString(R.string.register_status_action));
        intent.putExtra(getString(R.string.register_status_extra), false);
        intent.putExtra(getString(R.string.error_extra), false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Callback which gets fired when device receives a push message from a server
    @Override
    protected void onMessage(Context arg0, Intent arg1) {
        // Retrieve push message value from "message" key from JSON formatted data
        // (For BlackBerry devices) The data sent from the server MUST BE FORMATTED AS JSON
        // For example, the data sent by the server for this app must be formatted as:
        // { "message":"value", key2: value2, "key3": value3, key4 : "value 4" }
        // In the following, we are only retrieving the data from the "message" key
        // If the message appears as "null" within the app, then the data sent by the server
        // was not formatted correctly
        final String message = arg1.getStringExtra("message");

        Log.i(TAG, "New Push Message: " + message);

        Intent intent = new Intent(getString(R.string.push_message_action));
        intent.putExtra(getString(R.string.push_message_extra), message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Generate a system wide notification containing the push message
        generateNotification(arg0, message);
    }

    // Callback which gets fired when an error is caught
    @Override
    protected void onError(Context arg0, String errorId) {
        String errorMessage;

        Log.i(TAG, "Received error: " + errorId);

        // Send local broadcast to notify main activity of error
        if (errorId.equals(getString(R.string.too_many_registrations))) {
            errorMessage = getString(R.string.too_many_reg_error);
        } else if (errorId.equals(getString(R.string.service_not_available))) {
            errorMessage = getString(R.string.gcm_not_avail_error);
        } else {
            errorMessage = getString(R.string.gcm_reg_error);
        }

        Intent intent = new Intent(getString(R.string.register_status_action));
        intent.putExtra(getString(R.string.register_status_extra), false);
        intent.putExtra(getString(R.string.error_extra), true);
        intent.putExtra(getString(R.string.error_message_extra), errorMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }

    // Create a system wide notification
    private static void generateNotification(Context context, String message) {
        int icon = R.mipmap.ic_announcement_black_48dp;
        long when = System.currentTimeMillis();
        String title = context.getString(R.string.app_name);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, "Push Message", when);
        notification.sound = soundUri;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("message", message);
        // Set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
}
