/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.audiocontrol.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.audiocontrol.AudioType;
import com.example.audiocontrol.logger.Log;
import com.example.audiocontrol.R;

import java.util.List;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView mDeviceStatus;
    private TextView mDeviceLevels;
    private String receiveBuffer = "";

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the bluetooth service
     */
    private com.example.audiocontrol.bluetooth.BluetoothService mBluetoothService = null;

    /**
     * Member object for the main handler
     */
    private Handler mMainHandler;

    /**
     * Constructor.
     *
     * @param mainHandler A Handler to send messages back to the Main Activity
     */
    public BluetoothFragment(Handler mainHandler) {
         mMainHandler = mainHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        FragmentActivity activity = getActivity();
        if (mBluetoothAdapter == null && activity != null) {
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothAdapter == null) {
            return;
        }
        // If BT is not on, request that it be enabled.
        // setupBluetooth() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBluetoothService == null) {
            setupBluetooth();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == com.example.audiocontrol.bluetooth.BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mDeviceStatus = view.findViewById(R.id.device_status);
        mDeviceLevels = view.findViewById(R.id.device_levels);
        ImageButton button_secure_connect_scan = view.findViewById(R.id.button_secure_connect_scan);

        button_secure_connect_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), com.example.audiocontrol.bluetooth.DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupBluetooth() {
        Log.d(TAG, "setupBluetooth()");

        // Initialize the array adapter for the conversation thread
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        // Initialize the BluetoothService to perform bluetooth connections
        mBluetoothService = new com.example.audiocontrol.bluetooth.BluetoothService(activity, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public Boolean IsBluetoothServiceConnected() {
        return mBluetoothService != null && mBluetoothService.getState() == com.example.audiocontrol.bluetooth.BluetoothService.STATE_CONNECTED;
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != com.example.audiocontrol.bluetooth.BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setDeviceStatus(int resId) {
        mDeviceStatus.setText(resId);
    }

    /**
     * Send all levels to device.
     *
     * @param subTitle status
     */

    private void setDeviceStatus(CharSequence subTitle) {
        mDeviceStatus.setText(subTitle);
    }

    /**
     * Updates device levels.
     *
     * @param subTitle status
     */
    private void setDeviceLevels(CharSequence subTitle) {
        mDeviceLevels.setText(subTitle);
    }

    /**
     * Updates device levels.
     *
     * @param resId a string resource ID
     */
    private void setDeviceLevels(int resId) {
        mDeviceLevels.setText(resId);
    }

    private void messageHandler()
    {
        if (receiveBuffer != null) {
            mDeviceLevels.setText(receiveBuffer);
        }
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case com.example.audiocontrol.bluetooth.Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case com.example.audiocontrol.bluetooth.BluetoothService.STATE_CONNECTED:
                            setDeviceStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            requestSendLevelsToDevice();
                             break;
                        case com.example.audiocontrol.bluetooth.BluetoothService.STATE_CONNECTING:
                            setDeviceStatus(R.string.title_connecting);
                            break;
                        case com.example.audiocontrol.bluetooth.BluetoothService.STATE_LISTEN:
                        case com.example.audiocontrol.bluetooth.BluetoothService.STATE_NONE:
                            setDeviceStatus(R.string.title_not_connected);
                            setDeviceLevels(R.string.device_levels_unavailable);
                            break;
                    }
                    break;
                case com.example.audiocontrol.bluetooth.Constants.MESSAGE_WRITE:
                    break;
                case com.example.audiocontrol.bluetooth.Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    receiveBuffer += readMessage;
                    if(receiveBuffer.contains("\n")) {
                        receiveBuffer = receiveBuffer.substring(0, receiveBuffer.length() - 1);
                        messageHandler();
                        receiveBuffer = "";
                    }
                    break;
                case com.example.audiocontrol.bluetooth.Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(com.example.audiocontrol.bluetooth.Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case com.example.audiocontrol.bluetooth.Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(com.example.audiocontrol.bluetooth.Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBluetooth();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, R.string.bt_not_enabled_leaving,
                                Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                }
        }
    }

    private synchronized void requestSendLevelsToDevice() {
        mMainHandler.obtainMessage(Constants.MESSAGE_DEVICE_UPDATE_LEVELS).sendToTarget();
    }


    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(com.example.audiocontrol.bluetooth.DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device, secure);
    }
}
