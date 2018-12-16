package com.tina.webrtc.client;

import android.os.Handler;
import android.os.HandlerThread;

import com.tina.webrtc.interfaces.RoomConnectionParameters;
import com.tina.webrtc.interfaces.SignalingEvents;
import com.tina.webrtc.interfaces.SignalingParameters;

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
    private SignalingEvents events;
    private ConnectionState roomState;
    private RoomConnectionParameters connectionParameters;
    private String messageUrl;
    private String leaveUrl;


    public WebSocketRTCClient(SignalingEvents events){
        this.events = events;
        roomState = ConnectionState.NEW;
        final HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

    }


    public void connectToRoom(final RoomConnectionParameters roomConnectionParameters){
    //耗时
        this.connectionParameters = connectionParameters;

        handler.post(new Runnable() {
            @Override
            public void run() {
                String connectionUrl = connectionParameters.roomUrl + "/" + "join/" + connectionParameters.roomId;
                //http请求
                WebSocketChannelClient wsClient = new WebSocketChannelClient(handler, WebSocketRTCClient.this);

                RoomParametersFetcher roomParametersFetcher = new RoomParametersFetcher(connectionParameters.roomUrl,
                        null,
                        new RoomParametersFetcher.RoomParametersFetcherEvents() {
                            @Override
                            public void onSignalingParametersReady(SignalingParameters params) {
                                //回调这个接口

                            }

                            @Override
                            public void onSignalingParametersError(String description) {

                            }
                        });

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
