package org.jzeisel.app_test.jna;

import com.sun.jna.Native;
import com.sun.jna.Library;

public class SoundIoInterface {
    public interface SoundIoLib extends Library {
        SoundIoLib INSTANCE = Native.load("soundlib", SoundIoLib.class);
        void destroySession();
        int initializeEnvironment();
        void deinitializeEnvironment();
        int connectToBackend();

        /* functions for input devices */
        int getDefaultInputDeviceIndex();
        int getNumInputDevices();
        void loadInputDevices();
        String getDefaultInputDeviceName();
        String getInputDeviceName(int index);
        String getInputDeviceId(int index);
        String pickCurrentInputDevice(int index);
        int getNumChannelsOfCurrentInputDevice();
        String getNameOfChannelOfCurrentInputDevice(int index);

        /* functions for output devices */
        int getDefaultOutputDeviceIndex();
        int getNumOutputDevices();
        void loadOutputDevices();
        String getDefaultOutputDeviceName();
        String getOutputDeviceName(int index);
        String getOutputDeviceId(int index);
        String pickCurrentOutputDevice(int index);
        int getNumChannelsOfCurrentOutputDevice();
        String getNameOfChannelOfCurrentOutputDevice(int index);
    }

    public static int initializeEnvironment() {
        return SoundIoLib.INSTANCE.initializeEnvironment();
    }
}
