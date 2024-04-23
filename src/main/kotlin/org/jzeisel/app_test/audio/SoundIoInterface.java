package org.jzeisel.app_test.audio;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Library;
import org.jzeisel.app_test.util.Logger;

public class SoundIoInterface {
    private AudioEngineManager engineManager;
    public SoundIoInterface(AudioEngineManager manager) {
        this.engineManager = manager;
    }
    private interface SoundIoLib extends Library {
        SoundIoLib INSTANCE = Native.load("soundlib", SoundIoLib.class);
        /* this is a callback type */
        interface soundLibCallback extends Callback { void invoke(String message); }
        interface soundStreamCallback extends Callback { void invoke(String message, int deviceIndex); }

        void registerAudioPanicCallback(soundLibCallback callbackFunc);
        void registerAudioLogCallback(soundLibCallback callbackFunc);
        void registerInputStreamCallback(soundStreamCallback callbackFunc);
        void registerOutputStreamCallback(soundStreamCallback callbackFunc);

        int lib_startSession();
        int lib_initializeEnvironment();
        int lib_destroySession();
        int lib_deinitializeEnvironment();
        int lib_getCurrentBackend();
        int lib_checkEnvironmentAndBackendConnected();


        /* functions for input devices */
        int lib_loadInputDevices();
        int lib_getDefaultInputDeviceIndex();
        int lib_getNumInputDevices();
        String lib_getDefaultInputDeviceName();
        String lib_getInputDeviceName(int index);
        String lib_getInputDeviceId(int index);
        int lib_getNumChannelsOfInputDevice(int index);
        String lib_getNameOfChannelOfInputDevice(int deviceIndex, int channelIndex);


        /* functions for output devices */
        int lib_loadOutputDevices();
        int lib_getDefaultOutputDeviceIndex();
        int lib_getNumOutputDevices();
        String lib_getDefaultOutputDeviceName();
        String lib_getOutputDeviceName(int index);
        String lib_getOutputDeviceId(int index);
        int lib_getNumChannelsOfOutputDevice(int index);
        String lib_getNameOfChannelOfOutputDevice(int deviceIndex, int channelIndex);


        /* functions for starting and maintaining streams */
        int lib_createInputStream(int device_index, double microphone_latency, int sample_rate);
        int lib_createAndStartInputStream(int deviceIndex, double microphone_latency, int sample_rate);
        int lib_createAllInputStreams(double microphone_latency, int sample_rate);
        int lib_stopInputStream(int deviceIndex);
        int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate);
        int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate);
        int lib_stopOutputStream(int deviceIndex);
        double lib_getCurrentRmsVolume(int deviceIndex);
    }

    private final SoundIoLib.soundLibCallback audioPanic = message -> engineManager.audioPanic(message);
    private final SoundIoLib.soundLibCallback audioLog = message -> engineManager.audioLog(message);
    private final SoundIoLib.soundStreamCallback inputStreamCallback = (message, index) -> engineManager.inputStreamCallback(message, index);
    private final SoundIoLib.soundStreamCallback outputStreamCallback = (message, index) -> engineManager.outputStreamCallback(message, index);

    public void registerAudioPanicCallback() { SoundIoLib.INSTANCE.registerAudioPanicCallback(audioPanic); }
    public void registerAudioLogCallback() { SoundIoLib.INSTANCE.registerAudioLogCallback(audioLog); }
    public void registerInputStreamCallback() { SoundIoLib.INSTANCE.registerInputStreamCallback(inputStreamCallback); }
    public void registerOutputStreamCallback() { SoundIoLib.INSTANCE.registerOutputStreamCallback(outputStreamCallback); }


    public int lib_startSession() { return SoundIoLib.INSTANCE.lib_startSession(); }
    public int lib_initializeEnvironment() { return SoundIoLib.INSTANCE.lib_initializeEnvironment(); }
    public int lib_destroySession() { return SoundIoLib.INSTANCE.lib_destroySession(); }
    public int lib_deinitializeEnvironment() { return SoundIoLib.INSTANCE.lib_deinitializeEnvironment(); }
    public int lib_getCurrentBackend() { return SoundIoLib.INSTANCE.lib_getCurrentBackend(); }
    public int lib_checkEnvironmentAndBackendConnected() { return SoundIoLib.INSTANCE.lib_checkEnvironmentAndBackendConnected(); }


    public int lib_loadInputDevices() { return SoundIoLib.INSTANCE.lib_loadInputDevices(); }
    public int lib_getDefaultInputDeviceIndex() { return SoundIoLib.INSTANCE.lib_getDefaultInputDeviceIndex(); }
    public int lib_getNumInputDevices() { return SoundIoLib.INSTANCE.lib_getNumInputDevices(); }
    public String lib_getDefaultInputDeviceName() { return SoundIoLib.INSTANCE.lib_getDefaultInputDeviceName(); }
    public String lib_getInputDeviceName(int index) { return SoundIoLib.INSTANCE.lib_getInputDeviceName(index); }
    public String lib_getInputDeviceId(int index) { return SoundIoLib.INSTANCE.lib_getInputDeviceId(index); }
    public int lib_getNumChannelsOfInputDevice(int index) { return SoundIoLib.INSTANCE.lib_getNumChannelsOfInputDevice(index); }
    public String lib_getNameOfChannelOfInputDevice(int deviceIndex, int channelIndex) {
        return SoundIoLib.INSTANCE.lib_getNameOfChannelOfInputDevice(deviceIndex, channelIndex);
    }


    public int lib_loadOutputDevices() { return SoundIoLib.INSTANCE.lib_loadOutputDevices(); }
    public int lib_getDefaultOutputDeviceIndex() { return SoundIoLib.INSTANCE.lib_getDefaultOutputDeviceIndex(); }
    public int lib_getNumOutputDevices() { return SoundIoLib.INSTANCE.lib_getNumOutputDevices(); }
    public String lib_getDefaultOutputDeviceName() { return SoundIoLib.INSTANCE.lib_getDefaultOutputDeviceName(); }
    public String lib_getOutputDeviceName(int index) { return SoundIoLib.INSTANCE.lib_getOutputDeviceName(index); }
    public String lib_getOutputDeviceId(int index) { return SoundIoLib.INSTANCE.lib_getOutputDeviceId(index); }
    public int lib_getNumChannelsOfOutputDevice(int index) { return SoundIoLib.INSTANCE.lib_getNumChannelsOfOutputDevice(index); }
    public String lib_getNameOfChannelOfOutputDevice(int deviceIndex, int channelIndex) {
        return SoundIoLib.INSTANCE.lib_getNameOfChannelOfOutputDevice(deviceIndex, channelIndex);
    }


    public int lib_createInputStream(int device_index, double microphone_latency, int sample_rate) {
        return SoundIoLib.INSTANCE.lib_createInputStream(device_index, microphone_latency, sample_rate);
    }
    public int lib_createAndStartInputStream(int deviceIndex, double microphone_latency, int sample_rate) {
        return SoundIoLib.INSTANCE.lib_createAndStartInputStream(deviceIndex, microphone_latency, sample_rate);
    }
    public int lib_createAllInputStreams(int deviceIndex, int sample_rate) {
        return SoundIoLib.INSTANCE.lib_createAllInputStreams(deviceIndex, sample_rate);
    }
    public int lib_stopInputStream(int deviceIndex) { return SoundIoLib.INSTANCE.lib_stopInputStream(deviceIndex); }
    public int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate) {
        return SoundIoLib.INSTANCE.lib_createOutputStream(device_index, microphone_latency, sample_rate);
    }
    public int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate) {
        return SoundIoLib.INSTANCE.lib_createAndStartOutputStream(deviceIndex, microphone_latency, sample_rate);
    }
    public int lib_stopOutputStream(int deviceIndex) { return SoundIoLib.INSTANCE.lib_stopOutputStream(deviceIndex); }
    public double lib_getCurrentRmsVolume(int deviceIndex) { return SoundIoLib.INSTANCE.lib_getCurrentRmsVolume(deviceIndex); }
}
