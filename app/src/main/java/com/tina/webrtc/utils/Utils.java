package com.tina.webrtc.utils;

import android.content.Context;

import com.tina.webrtc.interfaces.RoomConnectionParameters;
import com.tina.webrtc.interfaces.SignalingParameters;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.VideoCapturer;

public class Utils {

//接口
    public static String getMessageUrl(
            RoomConnectionParameters connectionParameters, SignalingParameters signalingParameters) {
        return connectionParameters.roomUrl + "/message/" + connectionParameters.roomId
                + "/" + signalingParameters.clientId;
    }

    public static String getLeaveUrl(
            RoomConnectionParameters connectionParameters, SignalingParameters signalingParameters) {
        return connectionParameters.roomUrl + "/message/" + connectionParameters.roomId + "/"
                + signalingParameters.clientId;
    }
///VideoCapturer  Camaera
    public static VideoCapturer createVideoCaptuer(Context context) {
//
        VideoCapturer videoCapturer;
        if (Camera2Enumerator.isSupported(context)) {
            //Camera2
            videoCapturer = createCameraCapturer(new Camera2Enumerator(context));
        } else {
            //Camera
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    public static VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}
