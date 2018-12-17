package com.tina.webrtc.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.tina.webrtc.MainActivity;
import com.tina.webrtc.interfaces.RoomConnectionParameters;
import com.tina.webrtc.interfaces.SignalingEvents;
import com.tina.webrtc.interfaces.SignalingParameters;
import com.tina.webrtc.utils.Utils;

/**
 * @author yxc
 * @date 2018/12/15
 */
public class WebSocketRTCClient implements WebSocketChannelClient.WebSocketChannelEvents{

    private static final String TAG = "WSRTCClient";
    private static final String ROOM_JOIN = "join";
    private static final String ROOM_MESSAGE = "message";
    private static final String ROOM_LEAVE = "leave";

    private enum ConnectionState {NEW, CONNECTED, CLOSED, ERROR}

    private enum MessageType {MESSAGE, LEAVE}

    private Handler handler;
    private boolean initiator;
    private SignalingEvents mainActivityInterface;
    private ConnectionState roomState;
    private RoomConnectionParameters connectionParameters;
    private String messageUrl;
    private String leaveUrl;


    public WebSocketRTCClient(SignalingEvents mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
        roomState = ConnectionState.NEW;
        final HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void connectToRoom(final RoomConnectionParameters roomConnectionParameters){
    //耗时
        this.connectionParameters = roomConnectionParameters;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.e(MainActivity.TAG, "Begin to WebSocketRTCClient connectToRoom!");
                String connectionUrl = connectionParameters.roomUrl + "/" + "join/" + connectionParameters.roomId;
                //http请求
                WebSocketChannelClient wsClient = new WebSocketChannelClient(handler, WebSocketRTCClient.this);

                RoomParametersFetcher.RoomParametersFetcherEvents roomParametersFetcherEvents = new RoomParametersFetcher.RoomParametersFetcherEvents() {
                    @Override
                    public void onSignalingParametersReady(SignalingParameters params) {
                        Log.e(MainActivity.TAG, "WebSocketRTCClient connectToRoom ing");
                        //回调这个接口 信号参数
                        messageUrl = Utils.getMessageUrl(connectionParameters, params);
                        roomState = ConnectionState.CONNECTED;
                        mainActivityInterface.onConnectedToRoom(params);
                    }

                    @Override
                    public void onSignalingParametersError(String description) {

                    }
                };

                RoomParametersFetcher roomParametersFetcher = new RoomParametersFetcher
                        (connectionUrl,null,roomParametersFetcherEvents );

                roomParametersFetcher.makeRequest();
            }
        });


    }

    //回调这个方法
    @Override
    public void onWebSocketMessage(String message) {

    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

}
