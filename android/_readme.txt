jessica/android/_readme.txt
2014-11-13

This is a quick hack for autonomous control of Parrot MiniDrone (Rolling
Spider) written in Java and running on Android phone. The original sample was
taken from:

https://developer.android.com/samples/BluetoothLeGatt/index.html

I it necessary at first to make this sample running (Android Studio was used)
and in the second step replace some files + add TourTheStairs.java into
project.

---------------------

= SampleGattAttributes.java 

Original list of pairs UUID-name was extended by the ones used by Parrot. In
most cases the usage of service/characteristics is unknown, so first/last and
used characteristics have "name" by changing part of UUID (like A03).

---------------------

= BluetoothLeService.java

There is new methong for writing characteristics and replace code for request
notification (instead of UUID_HEART_RATE_MEASUREMENT any notification can be
requested.

---------------------

= DeviceControlActivity.java

Hacked to create and run TourTheStairs thread, when you click on any
characteristics. The plan is that here will be separate on/off row.

---------------------

= DeviceScanActivity.java

Currently without any change.

---------------------

= TourTheStairs.java

This is the main code with development for Tour the Stairs contest:
http://robotika.cz/competitions/tour-the-stairs


