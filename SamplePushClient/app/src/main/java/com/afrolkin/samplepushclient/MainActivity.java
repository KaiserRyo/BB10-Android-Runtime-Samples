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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import static com.afrolkin.samplepushclient.CommonUtilities.SENDER_ID;

public class MainActivity extends ActionBarActivity {
    private String TAG = "===MainActivity===";

    private ProgressDialog progressDialog;
    private Button registerButton;
    private Button unregisterButton;
    private TextView registrationStatusTextView;
    private TextView registrationIDTextView;
    private TextView senderIDTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.afrolkin.samplepushclient.R.layout.activity_main);

        // Layout element references
        registerButton = (Button) findViewById(R.id.register_button);
        unregisterButton = (Button) findViewById(R.id.unregister_button);
        registrationStatusTextView = (TextView) findViewById(R.id.registration_status);
        registrationIDTextView = (TextView) findViewById(R.id.registration_id);
        senderIDTextView = (TextView) findViewById(R.id.sender_id);

        // Register local receiver to receive broadcasts from GCMIntentService
        IntentFilter filter = new IntentFilter(getString(R.string.register_status_action));
        filter.addAction(getString(R.string.push_message_action));
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerGcm();
            }
        });

        unregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterGcm();
            }
        });

        checkSenderID(SENDER_ID);
        senderIDTextView.setText(SENDER_ID);

        // Try to register GCM on first open
        registerGcm();
    }

    // BroadcastReceiver for messages sent from GCMIntentService
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // Registration status updates
            if (intent.getAction().equals(getString(R.string.register_status_action))) {
                progressDialog.dismiss();

                // Change layout depending on GCM registration success or failure
                if (intent.getBooleanExtra(getString(R.string.register_status_extra), true)) {

                    // *************************************************************************
                    // *************************************************************************
                    // SEND THE REGISTRATION ID TO THE SERVER HERE, SO YOU CAN TRANSMIT MESSAGES
                    // FROM THE SERVER TO THE REGISTERED DEVICE
                    // *************************************************************************
                    // *************************************************************************

                    registrationStatusTextView.setText(getString(R.string.registered));
                    registrationStatusTextView.setTextColor(Color.GREEN);
                    registrationIDTextView.setText(intent.getStringExtra(
                            getString(R.string.registration_id_extra)));
                    registerButton.setEnabled(false);
                    unregisterButton.setEnabled(true);
                } else {
                    registrationStatusTextView.setTextColor(Color.RED);
                    registerButton.setEnabled(true);
                    unregisterButton.setEnabled(false);

                    // Error registering for GCM
                    if (intent.getBooleanExtra(getString(R.string.error_extra), true)) {
                        registrationStatusTextView.setText(getString(R.string.failed));
                        // Show informative error dialog
                        showErrorDialog(intent.getStringExtra(getString(
                                R.string.error_message_extra)));
                    }
                    // User unregistered with GCM intentionally
                    else {
                        registrationStatusTextView.setText(getString(R.string.unregistered));
                    }
                }
            }
            // Push Message received
            else if (intent.getAction().equals(getString(R.string.push_message_action))) {
                // Handle push message code here

                // Show a toast with the push message
                Toast.makeText(getApplicationContext(), getString(R.string.push_message)
                                + intent.getStringExtra(getString(R.string.push_message_extra)),
                        Toast.LENGTH_LONG).show();
            }

        }
    };

    // Try to register device with GCM
    private void registerGcm() {
        progressDialog = ProgressDialog.show(this, getString(R.string.progress_dialog_title),
                getString(R.string.progress_dialog__registering_message), true);

        // Check if device and manifest is properly configured for GCM
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        final String regId = GCMRegistrar.getRegistrationId(this);
        registrationIDTextView.setText(regId);

        Log.i(TAG, "Attempting to register device with GCM");

        // Check if user was previously registered with GCM
        if (regId.equals("")) {
            // User wasn't previously registered, so attempt to register
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            // User was previously registered with GCM
            progressDialog.dismiss();
            Log.i(TAG, "Device " + regId + " already registered");

            registrationStatusTextView.setText(getString(R.string.registered));
            registrationStatusTextView.setTextColor(Color.GREEN);
            registerButton.setEnabled(false);
            unregisterButton.setEnabled(true);
        }
    }

    // Try to unregister device with GCM
    private void unregisterGcm() {
        Log.i(TAG, "Attempting to unregister device with GCM");

        progressDialog = ProgressDialog.show(this, getString(R.string.progress_dialog_title),
                getString(R.string.progress_dialog__unregistering_message), true);

        GCMRegistrar.unregister(this);
    }

    // Check if SenderID is valid
    private void checkSenderID(Object reference) {
        if (reference == null) {
            showErrorDialog(getString(R.string.sender_id_invalid));
            throw new NullPointerException(
                    getString(R.string.sender_id_invalid));
        }
    }

    // Show error dialog with message
    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message).setTitle(getString(R.string.error_dialog_title))
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();
    }
}