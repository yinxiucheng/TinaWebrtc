package com.tina.webrtc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.tina.webrtc.client.PeerConnectionClient;
import com.tina.webrtc.client.WebSocketRTCClient;
import com.tina.webrtc.interfaces.RoomConnectionParameters;
import com.tina.webrtc.interfaces.SignalingEvents;
import com.tina.webrtc.interfaces.SignalingParameters;
import com.tina.webrtc.utils.Utils;
import com.tina.webrtc.view.PercentFrameLayout;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;

public class MainActivity extends AppCompatActivity implements PeerConnectionClient.PeerConnectionEvents, SignalingEvents {

    private SurfaceViewRenderer remoteView, localView;

    private PercentFrameLayout remotePercentLayout, localPercentLayout;
    private EglBase rootEglBase;

    private PeerConnectionClient peerConnectionClient;

    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

    private RoomConnectionParameters roomConnectionParamters;
    private SignalingParameters signalingParameters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initWebrtc();

        //链接房间服务器
        connectRoom();

    }

    private void connectRoom() {
        //openfire 聊天
        roomConnectionParamters = new RoomConnectionParameters("https://39.107.122.235", "88889", false);
        WebSocketRTCClient webSocketRTCClient = new WebSocketRTCClient(this);

        webSocketRTCClient.connectToRoom(roomConnectionParamters);

    }

    private void initWebrtc() {

        rootEglBase = EglBase.create();
        remoteView.init(rootEglBase.getEglBaseContext(), null);
        localView.init(rootEglBase.getEglBaseContext(), null);
        //悬浮顶端
        localView.setZOrderMediaOverlay(true);
        //硬件加速
        localView.setEnableHardwareScaler(true);

        remoteView.setEnableHardwareScaler(true);
        //宽高跟视频相匹配
        remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        //网络数据、内存
        remoteView.setMirror(true);

        //设置连接参数
        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters.createDefault();

        peerConnectionClient = PeerConnectionClient.getInstance();

        peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);

    }



    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);
        remoteView = findViewById(R.id.remote_video_view);
        localView = findViewById(R.id.local_video_view);
        remotePercentLayout = findViewById(R.id.remote_video_layout);
        localPercentLayout = findViewById(R.id.local_video_layout);
    }


    @Override
    public void onLocalDescription(SessionDescription sdp) {

    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {

    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }


    //------------房间服务器回调--------------------

    @Override
    public void onConnectedToRoom(SignalingParameters params) {
//        peerconnectionClitent
        signalingParameters = params;
        //VideoCapturer  对
        VideoCapturer videoCapturer = Utils.createVideoCaptuer(this);
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), localView
                , remoteView, videoCapturer, signalingParameters);

    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {

    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onChannelClose() {

    }

    @Override
    public void onChannelError(String description) {

    }
}
