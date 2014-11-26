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
    private byte mBattery = -1; // unknown
    private byte mStatus = -1; // unknown

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

    public void sleep( int ms ) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String info() {
        return "Bat: " + mBattery + "% (" + mStatus +")";
    }

    public void setAllNotification( Boolean enable ) {
        for( ArrayList<BluetoothGattCharacteristic> myList : mGattCharacteristics )
            for( BluetoothGattCharacteristic characteristic : myList) {
                int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mBluetoothLeService.setCharacteristicNotification(characteristic, enable);
                    sleep(50);
                }
            }
    }

    public void init() {
        // note at at least BD characteristics notification is "must have" otherwise it does not start
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fb0f-0800-9191-11e4-012d1540cb8e"), true); // 0xCO
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fb0e-0800-9191-11e4-012d1540cb8e"), true); // 0xBD
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fb1b-0800-9191-11e4-012d1540cb8e"), true); // 0xE4
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fb1c-0800-9191-11e4-012d1540cb8e"), true); // 0xE7
        sleep( 50 );

        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fd22-0800-9191-11e4-012d1540cb8e"), true); // 0x113
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fd23-0800-9191-11e4-012d1540cb8e"), true); // 0x116
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fd24-0800-9191-11e4-012d1540cb8e"), true); // 0x119
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fd52-0800-9191-11e4-012d1540cb8e"), true); // 0x123
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fd53-0800-9191-11e4-012d1540cb8e"), true); // 0x126
        sleep( 50 );
        mBluetoothLeService.setCharacteristicNotification(uuid2characteristics("9a66fd54-0800-9191-11e4-012d1540cb8e"), true); // 0x129
        sleep(50);
        // TODO start all available notifications
//        setAllNotification(true);

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

    public void newInputs( byte[] data ) {
        if( data.length == 7 && data[3] == 5 && data[4] == 1 && data[5]==0 )
            mBattery = data[6];
        if( data.length == 10 && data[3] == 3 && data[4] == 1 && data[5] == 0 )
            mStatus = data[6];
    }

    public void ver0() {
        float scale = 279.3896179199219f;
        takeoff();
        motors( false, 0, 0, 0, 0, 0.0f, 12 ); // it has to land anyway
        if( !motors( true, 0, 100, 0, 0, scale, 6 ) ) return;
        emergencyStop();
        motors( false, 0, 0, 0, 0, 0.0f, 10 );
    }

    public void ver0ex() {
        for( int i=0; i < 3; i++ )
            ver0();
    }


        public void ver1() {
        //takeoff & land
        takeoff();
        while( mStatus == -1 || mStatus == 0 || mStatus == 1 )
            motors( false, 0, 0, 0, 0, 0.0f, 1 );
        land();
        while( mStatus == 1 )
            motors( false, 0, 0, 0, 0, 0.0f, 1 );
        motors( false, 0, 0, 0, 0, 0.0f, 20 ); // it has to land anyway
    }

    public void ver2() {
        //takeoff & land
        takeoff();
        while( mStatus == -1 || mStatus == 0 || mStatus == 1 )
            motors( false, 0, 0, 0, 0, 0.0f, 1 );
        motors( true, 0, 0, 0, -30, 0.0f, 15 );
        motors( true, 0, 0, 0, -20, 0.0f, 20 );
        for( int i=0; i < 3; i++ ) {
            motors( true, 0, 30, 0, 0, 0.0f, 10 );
            motors( false, 0, 0, 0, 0, 0.0f, 2 ); // hover=stop
        }
        motors( true, 0, 0, 0, 30, 0.0f, 10 );
        motors( true, 0, 50, 0, 0, 0.0f, 6 );
        motors( false, 0, 0, 0, 0, 0.0f, 10 ); // hover=stop

        land();
        while( mStatus == 1 )
            motors( false, 0, 0, 0, 0, 0.0f, 1 );
        motors( false, 0, 0, 0, 0, 0.0f, 20 ); // it has to land anyway
    }


    public void run() {
        init();
        ver0();
        mShouldRun = false;
    }
}
