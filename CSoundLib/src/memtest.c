#include "../inc/csoundlib.h"

int main() {

    lib_startSession();
    lib_loadInputDevices();
    lib_createAndStartInputStream(lib_getDefaultInputDeviceIndex(), .01, 44100);
    lib_loadOutputDevices();
    lib_createAndStartOutputStream(lib_getDefaultOutputDeviceIndex(), .01, 44100);

    lib_addNewTrack(0);
    lib_addNewTrack(1);
    lib_addNewTrack(2);

    while(1) {

    };

    return 0;
}
