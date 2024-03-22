package org.jzeisel.app_test.jna;

import com.sun.jna.Native;
import com.sun.jna.Library;

public class SoundIoInterface {
    private interface SoundIoLib extends Library {
        SoundIoLib INSTANCE = Native.load("soundlib", SoundIoLib.class);
        void destroySession();
        int initializeEnvironment();
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

    public void destroySession() {
        SoundIoLib.INSTANCE.destroySession();
    }

    public int initializeEnvironment() {
        return SoundIoLib.INSTANCE.initializeEnvironment();
    }

    public int connectToBackend() {
        return SoundIoLib.INSTANCE.connectToBackend();
    }

    public int getDefaultInputDeviceIndex() {
        return SoundIoLib.INSTANCE.getDefaultInputDeviceIndex();
    }

    public int getNumInputDevices() {
        return SoundIoLib.INSTANCE.getNumInputDevices();
    }

    public void loadInputDevices() {
        SoundIoLib.INSTANCE.loadInputDevices();
    }

    public String getDefaultInputDeviceName() {
        return SoundIoLib.INSTANCE.getDefaultInputDeviceName();
    }

    public String getInputDeviceName(int index) {
        return SoundIoLib.INSTANCE.getInputDeviceName(index);
    }

    public String getInputDeviceId(int index) {
        return SoundIoLib.INSTANCE.getInputDeviceId(index);
    }

    public String pickCurrentInputDevice(int index) {
        /* returns name */
        return SoundIoLib.INSTANCE.pickCurrentInputDevice(index);
    }

    public int getNumChannelsOfCurrentInputDevice() {
        return SoundIoLib.INSTANCE.getNumChannelsOfCurrentInputDevice();
    }

    public String getNameOfChannelOfCurrentInputDevice(int index) {
        return SoundIoLib.INSTANCE.getNameOfChannelOfCurrentInputDevice(index);
    }



    public int getDefaultOutputDeviceIndex() {
        return SoundIoLib.INSTANCE.getDefaultOutputDeviceIndex();
    }

    public int getNumOutputDevices() {
        return SoundIoLib.INSTANCE.getNumOutputDevices();
    }

    public void loadOutputDevices() {
        SoundIoLib.INSTANCE.loadOutputDevices();
    }

    public String getDefaultOutputDeviceName() {
        return SoundIoLib.INSTANCE.getDefaultOutputDeviceName();
    }

    public String getOutputDeviceName(int index) {
        return SoundIoLib.INSTANCE.getOutputDeviceName(index);
    }

    public String getOutputDeviceId(int index) {
        return SoundIoLib.INSTANCE.getOutputDeviceId(index);
    }

    public String pickCurrentOutputDevice(int index) {
        /* returns name */
        return SoundIoLib.INSTANCE.pickCurrentOutputDevice(index);
    }

    public int getNumChannelsOfCurrentOutputDevice() {
        return SoundIoLib.INSTANCE.getNumChannelsOfCurrentOutputDevice();
    }

    public String getNameOfChannelOfCurrentOutputDevice(int index) {
        return SoundIoLib.INSTANCE.getNameOfChannelOfCurrentOutputDevice(index);
    }
}
