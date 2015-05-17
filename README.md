Android Remote I/O Agent
========================

This tool mirrors the screen of a Android device and allows interaction with it. This can be especially useful when dealing with a broken screen.



Requirements
------------

* [Java 1.7+](http://java.com/download/)
* [Android SDK](http://developer.android.com/sdk/installing/index.html)
* [Download the latest jar](https://github.com/wue-bros/AndroidRemoteIOAgent/releases) or build it from source



How to Use
----------

### Options

| Option                             | Description |
| ---------------------------------- | ----------- |
| ADB path                           | path to the `adb` executable in the Android SDK; default works if ADB is in your PATH |
| Temporary file (device)            | where to store mirroring screenshots on your Android device; default should work for most devices |
| Temporary file (client)            | where to store mirroring screenshots on the PC; defaults to the working directory |
| Long press duration (milliseconds) | time your Android device needs to recognize a long press |
| Scroll speed                       | how fast the mouse wheel scrolls on the Android device |



How to Build
------------

    mvn clean compile assembly:single
