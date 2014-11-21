package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.Thread;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Created by martind on 11/12/2014.
 */
public class TourTheStairs extends Thread {

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private Boolean mShouldRun = true;
    private int mMotorCounter = 1;
    private int mSettingsCounter = 1;
    private int mEmergencyCounter = 1;

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

    public void takeoff() {
        BluetoothGattCharacteristic characteristics;
        characteristics = uuid2characteristics("9a66fa0b-0800-9191-11e4-012d1540cb8e"); // handle 0x43
        byte [] arr = { 4, (byte)mSettingsCounter, 2, 0, 1, 0 };
        characteristics.setValue( arr );
        mBluetoothLeService.writeCharacteristic(characteristics);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mSettingsCounter++;
    }

    public void land() {
        BluetoothGattCharacteristic characteristics;
        characteristics = uuid2characteristics("9a66fa0b-0800-9191-11e4-012d1540cb8e"); // handle 0x43
        byte [] arr = { 4, (byte)mSettingsCounter, 2, 0, 3, 0 };
        characteristics.setValue( arr );
        mBluetoothLeService.writeCharacteristic(characteristics);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mSettingsCounter++;
    }

    public void emergencyStop() {
        // dangerous - stops all motors!
        BluetoothGattCharacteristic characteristics;
        characteristics = uuid2characteristics("9a66fa0c-0800-9191-11e4-012d1540cb8e"); // handle 0x46
        byte [] arr = { 4, (byte)mEmergencyCounter, 2, 0, 4, 0 };
        characteristics.setValue( arr );
        mBluetoothLeService.writeCharacteristic(characteristics);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEmergencyCounter++;
    }

    public void sendMotorCmd( Boolean on, int tilt, int forward, int turn, int up, float scale ) {
        // TODO replace by speed and direction parameters
        BluetoothGattCharacteristic characteristics;
        characteristics = uuid2characteristics("9a66fa0a-0800-9191-11e4-012d1540cb8e"); // handle 0x40
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream packet = new DataOutputStream( stream );
        try {
            packet.writeByte( 2 );
            packet.writeByte( (byte)mMotorCounter );
            packet.writeByte( 2 );
            packet.writeByte( 0 );
            packet.writeByte( 2 );
            packet.writeByte( 0 );
            if (on) {
                packet.writeByte( 1 );
            } else {
                packet.writeByte( 0 );
            }
            // is byte casting necessary???
            packet.writeByte( (byte) (tilt & 0xFF) );
            packet.writeByte( (byte) (forward & 0xFF) );
            packet.writeByte( (byte) (turn & 0xFF) );
            packet.writeByte( (byte) (up & 0xFF) );
            packet.writeFloat( scale ); // well, but I need different endian :(
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte [] tmpArr = stream.toByteArray();
        byte tmp;
        tmp = tmpArr[11]; // temporary hack - swapping float ordering
        tmpArr[11] = tmpArr[14];
        tmpArr[14] = tmp;
        tmp = tmpArr[12];
        tmpArr[12] = tmpArr[13];
        tmpArr[13] = tmp;
        characteristics.setValue( tmpArr );
        mBluetoothLeService.writeCharacteristic(characteristics);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mMotorCounter++;
    }

    public Boolean motors( Boolean on, int tilt, int forward, int turn, int up, float scale, int steps ) {
        for( int i=0; i < steps; i++) {
            sendMotorCmd( on, tilt, forward, turn, up, scale );
            if( !mShouldRun ) {
                sendMotorCmd( false, 0, 0, 0, 0, 0.0f ); // stop it
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
        float scale = 279.3896179199219f;
        init();
        takeoff();
        motors( false, 0, 0, 0, 0, 0.0f, 15 ); // it has to land anyway
        if( !motors( true, 0, 100, 0, 0, scale, 6 ) ) return;
        emergencyStop();
        motors( false, 0, 0, 0, 0, 0.0f, 10 );
        mShouldRun = false;
    }
}
