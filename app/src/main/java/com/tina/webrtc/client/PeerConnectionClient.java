package com.tina.webrtc.client;

import android.content.Context;
import android.util.Log;

import com.tina.webrtc.interfaces.SignalingParameters;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author yxc
 * @date 2018/12/15
 */
public class PeerConnectionClient {

    private ScheduledExecutorService executor;
    private PeerConnectionFactory factory;

    private static final PeerConnectionClient instance = new PeerConnectionClient();
    PeerConnectionFactory.Options options = null;

    private final PCObserver pcObserver = new PCObserver();
    private MediaConstraints pcConstraints;

    private int videoWidth;
    private int videoHeight;
    private int videoFps;
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    private MediaStream mediaStream;
    //本地的视频源
    private VideoTrack localVideoTrack;
    //远程的视频源
    private VideoTrack remoteVideoTrack;

    private PeerConnection peerConnection;

    public static PeerConnectionClient getInstance() {
        return instance;
    }
    private PeerConnectionClient() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }


    public void createPeerConnectionFactory(final Context context, PeerConnectionParameters peerConnectionParameters,
                                            PeerConnectionEvents events      ) {
        //创建连接
        //耗时
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //创建链接通道  并且初始化  不是java的socket
                PeerConnectionFactory.initializeInternalTracer();
                PeerConnectionFactory.initializeFieldTrials("");
                //自动取消，对方挂了电话
                WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);

                WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(false);
                WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
                //初始化全局的connection
                PeerConnectionFactory.initializeAndroidGlobals(context, true, true, true);
                //初始化的工作
                factory = new PeerConnectionFactory(options);
                //java数据库连接池
            }
        });

    }



    //创建 空连接  native   scoket（“ip”）
    public void createPeerConnection(final EglBase.Context renderEGLContext,
                                     final SurfaceViewRenderer localview, final SurfaceViewRenderer remoteView,
                                     final VideoCapturer videoCapturer,
                                     final SignalingParameters signalingParameters) {


        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("david", "run: ----------------------------------->2");
                pcConstraints = new MediaConstraints();
                pcConstraints.optional.add(
                        new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

                //1280  *720
                videoWidth = 1280;
                videoHeight = 720;
                videoFps = 30;
                //Create audio constraints.
                audioConstraints = new MediaConstraints();
                sdpMediaConstraints = new MediaConstraints();
                sdpMediaConstraints.mandatory.add(
                        new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                sdpMediaConstraints.mandatory.add(
                        new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                Log.i("david", " 2  PeerConnectionFactory 初始化是否完成   "+factory);
                factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
                PeerConnection.RTCConfiguration rtcConfig =
                        new PeerConnection.RTCConfiguration(signalingParameters.iceServers);
                // TCP candidates are only useful when connecting to a server that supports
                // ICE-TCP.
                rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
                rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
                rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
                rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
                // Use ECDSA encryption.
                rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
                //不是阻塞   peerConnection--->
                peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);

                //声音    推送B
                //创建一个音频音源
                AudioSource audioSource = factory.createAudioSource(audioConstraints);
                AudioTrack audioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
                audioTrack.setEnabled(true);

                mediaStream = factory.createLocalMediaStream("ARDAMS");
                mediaStream.addTrack(audioTrack);
                //音源有了  推送B

                //创建一个视频源
                VideoSource videoSource = factory.createVideoSource(videoCapturer);
                //预览的格式
                videoCapturer.startCapture(videoWidth, videoHeight, videoFps);

                localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
                localVideoTrack.setEnabled(true);
                localVideoTrack.addRenderer(new VideoRenderer(localview));
                //远端就能够看到 摄像头的画面
                mediaStream.addTrack(localVideoTrack);
                peerConnection.addStream(mediaStream);
                //视频B端
            }
        });
    }



    private class PCObserver implements PeerConnection.Observer{

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }
    }


    public static class DataChannelParameters {

        public final boolean ordered;
        public final int maxRetransmitTimeMs;
        public final int maxRetransmits;
        public final String protocol;
        public final boolean negotiated;
        public final int id;

        public DataChannelParameters(boolean ordered, int maxRetransmitTimeMs, int maxRetransmits,
                                     String protocol, boolean negotiated, int id) {
            this.ordered = ordered;
            this.maxRetransmitTimeMs = maxRetransmitTimeMs;
            this.maxRetransmits = maxRetransmits;
            this.protocol = protocol;
            this.negotiated = negotiated;
            this.id = id;
        }
    }



    public static class PeerConnectionParameters {
        public final boolean videoCallEnabled;
        //        回拨的意思
        public final boolean loopback;

        public final boolean tracing;
        public final int videoWidth;
        public final int videoHeight;
        //帧率
        public final int videoFps;
        //        比特率   60kb
        public final int videoMaxBitrate;
        public final String videoCodec;//视频编码
        public final boolean videoCodecHwAcceleration;//硬编码
        public final boolean videoFlexfecEnabled;

        public final int audioStartBitrate;
        public final String audioCodec;
        public final boolean noAudioProcessing;
        public final boolean aecDump;
        public final boolean enableLevelControl;
        private final DataChannelParameters dataChannelParameters;

        public static PeerConnectionParameters createDefault() {
            return new PeerConnectionParameters(true, false,
                    false, 0, 0, 0,
                    0, "VP8",
                    true,
                    false,
                    0, "OPUS",
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false);
        }

        public PeerConnectionParameters(boolean videoCallEnabled, boolean loopback, boolean tracing,
                                        int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate, String videoCodec,
                                        boolean videoCodecHwAcceleration, boolean videoFlexfecEnabled, int audioStartBitrate,
                                        String audioCodec, boolean noAudioProcessing, boolean aecDump, boolean useOpenSLES,
                                        boolean disableBuiltInAEC, boolean disableBuiltInAGC, boolean disableBuiltInNS,
                                        boolean enableLevelControl) {
            this(videoCallEnabled, loopback, tracing, videoWidth, videoHeight, videoFps, videoMaxBitrate,
                    videoCodec, videoCodecHwAcceleration, videoFlexfecEnabled, audioStartBitrate, audioCodec,
                    noAudioProcessing, aecDump, useOpenSLES, disableBuiltInAEC, disableBuiltInAGC,
                    disableBuiltInNS, enableLevelControl, null);
        }

        public PeerConnectionParameters(boolean videoCallEnabled, boolean loopback, boolean tracing,
                                        int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate, String videoCodec,
                                        boolean videoCodecHwAcceleration, boolean videoFlexfecEnabled, int audioStartBitrate,
                                        String audioCodec, boolean noAudioProcessing, boolean aecDump, boolean useOpenSLES,
                                        boolean disableBuiltInAEC, boolean disableBuiltInAGC, boolean disableBuiltInNS,
                                        boolean enableLevelControl, DataChannelParameters dataChannelParameters) {
            this.videoCallEnabled = videoCallEnabled;
            this.loopback = loopback;
            this.tracing = tracing;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.videoFps = videoFps;
            this.videoMaxBitrate = videoMaxBitrate;
            this.videoCodec = videoCodec;
            this.videoFlexfecEnabled = videoFlexfecEnabled;
            this.videoCodecHwAcceleration = videoCodecHwAcceleration;
            this.audioStartBitrate = audioStartBitrate;
            this.audioCodec = audioCodec;
            this.noAudioProcessing = noAudioProcessing;
            this.aecDump = aecDump;
            this.enableLevelControl = enableLevelControl;
            this.dataChannelParameters = dataChannelParameters;

        }
    }


    //  --------------------------接口-------------- 连接回调接口---------------------------------
    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         * 网络路径   客户端----》服务器
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is closed (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);
    }


}
