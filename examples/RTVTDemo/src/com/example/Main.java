package com.example;

import com.fpnn.rtvt.RTVTClient;
import com.fpnn.rtvt.RTVTPushProcessor;
import com.fpnn.rtvt.RTVTStruct;
import com.fpnn.rtvt.RTVTUserInterface;
import com.fpnn.sdk.ErrorRecorder;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class Main {
    static RTVTClient rtvtClient = null;
    static final String srcLanguage = "zh";
    static final String dstLanguage = "en";
    static final String userId = "123456";
    static final String rtvtEndpoint = "";
    static final long pid = 0;
    static final String secretKey ="";
    static final int readLength = 640;
    static AtomicLong streamId = new AtomicLong(0);

    static AtomicBoolean connectStatus = new AtomicBoolean(false);
    static void debugLog(String msg){
        System.out.println(  msg);
    }
    static void debugErrLog(String msg){
        System.err.println(  msg);
    }
    static class MyErrorRecorder extends ErrorRecorder{
        @Override
        public void recordError(String message) {
            System.err.println(message);
        }

        @Override
        public void recordError(Exception e) {
            System.err.println(e);
        }

        @Override
        public void recordError(String message, Exception e) {
            System.err.println(message + e);
        }
    }
    static class DemoPushProcessor extends RTVTPushProcessor{
        @Override
        public void rtvtConnectClose(InetSocketAddress peerAddress,  boolean causedByError) {
            debugErrLog("rtvtConnectClose " + peerAddress.toString() + " causedByError:" + causedByError + connectStatus.hashCode());
            connectStatus.set(false);

            long ts = System.currentTimeMillis() / 1000;
            String realToken = ApiSecurityExample.genHMACToken(91700007, ts, "MGIyZThkM2QtNjU4Yy00NjE1LWE0ODEtYjZmNmM3MWNmYzY4");
            while (true){
                RTVTStruct.RTVTAnswer rtvtAnswer = rtvtClient.login(realToken,ts);
                if (rtvtAnswer.errorCode == 0) {
                    rtvtClient.startTranslate(srcLanguage, dstLanguage, null, true, true, true, false, "", userId, RTVTStruct.Codec.PCM, new RTVTUserInterface.IRTVTCallback<RTVTStruct.VoiceStream>() {
                                @Override
                                public void onError(RTVTStruct.RTVTAnswer answer) {
                                    String msg = "startTranslate failed " + answer.getErrInfo();
                                    debugLog(msg);
                                }

                                @Override
                                public void onSuccess(RTVTStruct.VoiceStream voiceStream) {
                                    streamId.set(voiceStream.streamId);
                                    connectStatus.set(true);
                                }
                            });
                    debugErrLog("relogin ok connectStatus set true");
                    break;
                }
                else{
                    debugErrLog("rtvt login failed: " + rtvtAnswer.getErrInfo());
                }
                try {
                    Thread.sleep(3*1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void recognizedResult(long streamId, long startTs, long endTs, long recTs, String language, String srcVoiceText, long taskId) {
            debugLog("recognizedResult streamId:" + streamId + " startTs:" + startTs + " endTs:" + endTs + " recTs:" + recTs + " language:" + language +
                    " text:" + srcVoiceText + " taskId:" + taskId);
        }

        @Override
        public void recognizedTempResult(long streamId, long startTs, long endTs, long recTs, String language, String srcVoiceText, long taskId) {
            debugLog("recognizedTempResult streamId:" + streamId + " startTs:" + startTs + " endTs:" + endTs + " recTs:" + recTs + " language:" + language +
                    " text:" + srcVoiceText + " taskId:" + taskId);
        }

        @Override
        public void translatedResult(long streamId, long startTs, long endTs, long recTs, String language, String destVoiceText, long taskId) {
            debugLog("translatedResult streamId:" + streamId + " startTs:" + startTs + " endTs:" + endTs + " recTs:" + recTs + " language:" + language +
                    " text:" + destVoiceText + " taskId:" + taskId);
        }
        @Override
        public void translatedTempResult(long streamId, long startTs, long endTs, long recTs, String language, String destVoiceText, long taskId) {
            debugLog("translatedTempResult streamId:" + streamId + " startTs:" + startTs + " endTs:" + endTs + " recTs:" + recTs + " language:" + language +
                    " text:" + destVoiceText + " taskId:" + taskId);           }
    }


    public static void main(String[] args) {
        rtvtClient = RTVTClient.CreateClient(rtvtEndpoint, pid, new DemoPushProcessor());
        rtvtClient.setErrorRecoder(new MyErrorRecorder());
        long ts = System.currentTimeMillis() / 1000;
        String realToken = ApiSecurityExample.genHMACToken(pid, ts, secretKey);

        rtvtClient.login(realToken, ts, new RTVTUserInterface.IRTVTEmptyCallback() {
            @Override
            public void onError(RTVTStruct.RTVTAnswer answer) {
                debugErrLog(" rtvtClient login error:" + answer.getErrInfo());
            }

            @Override
            public void onSuccess() {
                connectStatus.set(true);
                rtvtClient.startTranslate(srcLanguage, dstLanguage, null, true, true, true, false, "", userId, RTVTStruct.Codec.PCM, new RTVTUserInterface.IRTVTCallback<RTVTStruct.VoiceStream>() {
                    @Override
                    public void onError(RTVTStruct.RTVTAnswer answer) {
                        String msg = "startTranslate failed " + answer.getErrInfo();
                        debugLog(msg);
                    }

                    @Override
                    public void onSuccess(RTVTStruct.VoiceStream voiceStream) {
                        streamId.set(voiceStream.streamId);
                        LinkedList<byte[]> list = new LinkedList<>();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                FileOutputStream fout;
                                FileInputStream fis;
                                BufferedOutputStream bufferedOutputStream;
                                byte[] bytes = null;
                                int seq = 0;
                                try {
                                    fout = new FileOutputStream("E:\\guailingout.pcm");
                                    bytes = Files.readAllBytes(Paths.get("E:\\guailing.pcm"));
                                    while (true) {
                                        Thread.sleep(20);
                                        if (!connectStatus.get()) {
                                            continue;
                                        }
                                        byte[] outdata = new byte[readLength];
                                        System.arraycopy(bytes, seq*readLength, outdata, 0, readLength);
                                        rtvtClient.sendVoice(streamId.get(), seq++, outdata, System.currentTimeMillis(), new RTVTUserInterface.IRTVTEmptyCallback() {
                                            @Override
                                            public void onError(RTVTStruct.RTVTAnswer answer) {
                                                debugLog("send voice error:" + answer.getErrInfo());
                                            }

                                            @Override
                                            public void onSuccess() {
                                            }
                                        });
                                        if (seq * readLength + readLength>bytes.length) {
                                            break;
                                        }
                /*                        ErrorRecorder recorder = (ErrorRecorder)ErrorRecorder.getInstance();
                                        recorder.println();*/
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                    }

                    ;
                });
            }
        });

        Scanner input = new Scanner(System.in);
        String str = input.next();
    }
}