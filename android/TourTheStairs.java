package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.lang.Thread;

/**
 * Created by martind on 11/12/2014.
 */
public class TourTheStairs extends Thread {

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private Boolean mShouldRun = true;
    private int mMotorCounter = 1;

    private final BluetoothGattCharacteristic uuid2characteristics(String myUuid) {
        for( ArrayList<BluetoothGattCharacteristic> myList : mGattCharacteristics )
            for( BluetoothGattCharacteristic characteristics : myList) {
                String uuid = characteristics.getUuid().toString();
                if( myUuid.equals(uuid) )
                    return characteristics;
            }
        return null;
    }

    public TourTheStairs( BluetoothLeService btService, ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics ) {
        mBluetoothLeService = btService;
        mGattCharacteristics = gattCharacteristics;
    }

    public void init() {
        // note at at least BD characteristics notification is "must have" otherwise it does not start
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fb0e-0800-9191-11e4-012d1540cb8e"), true);
        // TODO start all available notifications

        for( int i=0; i < 20; i++){
            BluetoothGattCharacteristic characteristics;
            characteristics = uuid2characteristics("9a66fa1e-0800-9191-11e4-012d1540cb8e"); // handle 0x7C
            byte[] value = new byte[3];
            value[0] = (byte) (0x1);
            value[1] = (byte) (i+1);
            value[2] = (byte) (i+1);
            characteristics.setValue(value);
            mBluetoothLeService.writeCharacteristic(characteristics);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMotorCmd( Boolean on, int forward, int turn, int up ) {
        // TODO replace by speed and direction parameters
        BluetoothGattCharacteristic characteristics;
        characteristics = uuid2characteristics("9a66fa0a-0800-9191-11e4-012d1540cb8e"); // handle 0x40
        byte[] value = new byte[15];
        value[0] = (byte) (2);
        value[1] = (byte) (mMotorCounter);
        value[2] = (byte) (2);
        value[3] = (byte) (0);
        value[4] = (byte) (2);
        value[5] = (byte) (0);
        if (on) {
            value[6] = (byte) (1);
        } else {
            value[6] = (byte) (0);
        }
        value[7] = (byte) (forward & 0xFF);
        value[8] = (byte) ((forward >> 8) & 0xFF);
        value[9] = (byte) (turn & 0xFF);
        value[10] = (byte) (up & 0xFF);
        if (on) {
            value[11] = (byte) (223);
            value[12] = (byte) (177);
            value[13] = (byte) (139);
            value[14] = (byte) (67);
        } else {
            value[11] = (byte) (0);
            value[12] = (byte) (0);
            value[13] = (byte) (0);
            value[14] = (byte) (0);
        }
        characteristics.setValue(value);
        mBluetoothLeService.writeCharacteristic(characteristics);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mMotorCounter++;
    }

    public Boolean motors( Boolean on, int forward, int turn, int up, int steps ) {
        for( int i=0; i < steps; i++) {
            sendMotorCmd( on, forward, turn, up );
            if( !mShouldRun ) {
                sendMotorCmd( false, 0, 0, 0 ); // stop it
                return false; // TODO replace by exception
            }
        }
        return true;
    }

    public void requestStop() {
        mShouldRun = false;
    }

    public Boolean completed() {
        return !mShouldRun;
    }

    public void run() {
        init();
        if( !motors( true, 8000, 0, 0, 50 ) ) return;
        if( !motors( true, 100, 0, 100, 30 ) ) return; // full power up, slightly forward
        return;
    }
}
