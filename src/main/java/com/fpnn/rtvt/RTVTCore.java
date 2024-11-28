package com.fpnn.rtvt;
import com.fpnn.rtvt.RTVTStruct.*;
import com.fpnn.rtvt.RTVTUserInterface.*;
import com.fpnn.sdk.*;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.HttpsURLConnection;

class RTVTCore{
    String logTag = "fpnn-rtvt";
    public class DefaultErrorRecoder extends ErrorRecorder {
        @Override
        public void recordError(String message) {
            System.err.println(logTag + message);
        }

        @Override
        public void recordError(String message, Exception e) {
            System.err.println(message + e);
        }
    }
    public enum ClientStatus {
        Closed,
        Connecting,
        Connected
    }

    public enum CloseType {
        ByUser,
        ByServer,
        Timeout,
        None
    }


    //for network change
    int globalQuestTimeoutSeconds = 30;
    int globalMaxThread = 4;
    String SDKVersion = "1.0.0";


    //-------------[ Fields ]--------------------------//
    private final Object interLocker =  new Object();
    private long pid;
    private String uid;
    private String loginToken;
    private long loginTs = 0;
    private String rtvtEndpoint;
    ErrorRecorder errorRecorder = new DefaultErrorRecoder();

    private Map<String, String>  loginAttrs = new HashMap<>();
    private ClientStatus rttGateStatus = ClientStatus.Closed;
    private CloseType closedCase = CloseType.None;
    private AtomicBoolean running = new AtomicBoolean(true);

    private RTVTServerQuestProcessor processor;
//    private TCPClient dispatch;
    private TCPClient rttGate;
    private AtomicLong connectionId = new AtomicLong(0);
    private AtomicBoolean noNetWorkNotify = new AtomicBoolean(false);
    private RTVTStruct.RTVTAnswer lastReloginAnswer = new RTVTStruct.RTVTAnswer();
    private RTVTPushProcessor serverPushProcessor;
    RTVTUtils rtvtUtils = new RTVTUtils();

    final int okRet = ErrorCode.FPNN_EC_OK.value();
    final int RTVTerrorCode = 200099;

    //voice
    //video
    public enum RTVTModel{
        Normal,
        VOICE,
        VIDEO
    }

    
/*    class RTVTQuestProcessor{
        Answer recognizedResult(Quest quest, InetSocketAddress peer){
            rttGate.sendAnswer(new Answer(quest));
            String text = rtvtUtils.wantString(quest, "asr");
            long streamId = rtvtUtils.wantLong(quest, "streamId");
            long startTs = rtvtUtils.wantLong(quest, "startTs");
            long endTs = rtvtUtils.wantLong(quest, "endTs");
            long recTs = rtvtUtils.wantLong(quest, "recTs");
            String lan = rtvtUtils.wantString(quest, "lang");
            long taskId = rtvtUtils.wantLong(quest, "taskId");


            serverPushProcessor.recognizedResult(streamId, startTs,endTs , recTs,lan,text,taskId);
            return null;
        }

        Answer recognizedTempResult(Quest quest, InetSocketAddress peer){
            rttGate.sendAnswer(new Answer(quest));
            String text = rtvtUtils.wantString(quest, "asr");
            long streamId = rtvtUtils.wantLong(quest, "streamId");
            long startTs = rtvtUtils.wantLong(quest, "startTs");
            long endTs = rtvtUtils.wantLong(quest, "endTs");
            long recTs = rtvtUtils.wantLong(quest, "recTs");
            String lan = rtvtUtils.wantString(quest, "lang");
            long taskId = rtvtUtils.wantLong(quest, "taskId");

            serverPushProcessor.recognizedTempResult(streamId, startTs,endTs , recTs,lan,text,taskId);
            return null;
        }


        Answer ttsResult(Quest quest, InetSocketAddress peer){
            rttGate.sendAnswer(new Answer(quest));
            String text = rtvtUtils.wantString(quest, "text");
            Object obj = quest.want("data");
            byte[] ttsdata = null;
            if (obj instanceof byte[]) {
                ttsdata  = (byte[])obj;
            } else{
                ttsdata = new byte[1];
            }

            long streamId = rtvtUtils.wantLong(quest, "streamId");
            String lan = rtvtUtils.wantString(quest, "lang");

            serverPushProcessor.ttsResult(streamId, text,ttsdata , lan);
            return null;
        }

        Answer translatedResult(Quest quest, InetSocketAddress peer){
            rttGate.sendAnswer(new Answer(quest));
            String text = rtvtUtils.wantString(quest, "trans");
            long streamId = rtvtUtils.wantLong(quest, "streamId");
            long startTs = rtvtUtils.wantLong(quest, "startTs");
            long endTs = rtvtUtils.wantLong(quest, "endTs");
            long recTs = rtvtUtils.wantLong(quest, "recTs");
            String lan = rtvtUtils.wantString(quest, "lang");
            long taskId = rtvtUtils.wantLong(quest, "taskId");

            serverPushProcessor.translatedResult(streamId, startTs,endTs , recTs, lan,text,taskId);
            return null;
        }

        Answer translatedTempResult(Quest quest, InetSocketAddress peer){
            rttGate.sendAnswer(new Answer(quest));
            String text = rtvtUtils.wantString(quest, "trans");
            long streamId = rtvtUtils.wantLong(quest, "streamId");
            long startTs = rtvtUtils.wantLong(quest, "startTs");
            long endTs = rtvtUtils.wantLong(quest, "endTs");
            long recTs = rtvtUtils.wantLong(quest, "recTs");
            String lan = rtvtUtils.wantString(quest, "lang");
            long taskId = rtvtUtils.wantLong(quest, "taskId");


            serverPushProcessor.translatedTempResult(streamId, startTs,endTs , recTs, lan,text,taskId);
            return null;
        }
    }*/


    public  void setServerPushProcessor(RTVTPushProcessor processor){
        this.serverPushProcessor = processor;
    }


    void RTVTInit(String rtvtendpoint, long pid, RTVTPushProcessor serverPushProcessor) {

        rtvtUtils.errorRecorder = errorRecorder;
        this.rtvtEndpoint = rtvtendpoint;

        this.pid = pid;
        processor = new RTVTServerQuestProcessor();

        this.serverPushProcessor = serverPushProcessor;

        ClientEngine.setMaxThreadInTaskPool(globalMaxThread);
    }

    long getPid() {
        return pid;
    }

    String getUid() {
        return uid;
    }


    synchronized protected ClientStatus getClientStatus() {
        synchronized (interLocker) {
            return rttGateStatus;
        }
    }


    RTVTAnswer genRTVTAnswer(int errCode){
        return genRTVTAnswer(errCode,"");
    }

    RTVTAnswer genRTVTAnswer(int errCode,String msg)
    {
        RTVTAnswer tt = new RTVTAnswer();
        tt.errorCode = errCode;
        tt.errorMsg = msg;
        return tt;
    }

    public RTVTAnswer genRTVTAnswer(Answer answer, int errcode) {
        if (answer == null && errcode !=0) {
            if (errcode == ErrorCode.FPNN_EC_CORE_TIMEOUT.value())
                return new RTVTAnswer(errcode, "FPNN_EC_CORE_TIMEOUT");
            else
                return new RTVTAnswer(errcode,"fpnn unknown error");
        } else{
            return new RTVTAnswer(errcode,answer.getErrorMessage());
        }
    }


    protected boolean isOnline() {
        return this.getClientStatus() == ClientStatus.Connected;
    }


    private TCPClient getCoreClient() {
        synchronized (interLocker) {
            if (rttGateStatus == ClientStatus.Connected)
                return rttGate;
            else
                return null;
        }
    }

    void setCloseType(CloseType type)
    {
        closedCase = type;
    }



    void realClose(){
        closedCase = CloseType.ByUser;
        connectionId.set(0);
        close();
    }


    void sendQuest(Quest quest, final FunctionalAnswerCallback callback) {
        sendQuest(quest, callback, globalQuestTimeoutSeconds);
    }

    Answer sendQuest(Quest quest) {
        return sendQuest(quest,globalQuestTimeoutSeconds);
    }

    Answer sendQuest(Quest quest, int timeout) {
        Answer answer = new Answer(new Quest(""));
        TCPClient client = getCoreClient();
        if (client == null) {
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), quest.method() + " getCoreClient invalid connection");
        }else {
            try {
                answer = client.sendQuest(quest, timeout);
            } catch (Exception e) {
                if (errorRecorder != null)
                    errorRecorder.recordError(e);
                answer = new Answer(quest);
                String errmsg = "sendquest error:" + e;
                answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(), errmsg);
                Thread.currentThread().interrupt();
            }
        }
        return answer;
    }

    void sendQuest(Quest quest, final FunctionalAnswerCallback callback, int timeout) {
        TCPClient client = getCoreClient();
        final Answer answer = new Answer(quest);
        if (client == null) {
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), quest.method() + " getCoreClient invalid connection");
            callback.onAnswer(answer,answer.getErrorCode());//当前线程
            return;
        }
        if (timeout <= 0)
            timeout = globalQuestTimeoutSeconds;
        try {
            client.sendQuest(quest, callback, timeout);
        }
        catch (Exception e){
            String errmsg = "sendquest err:" + e;
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),errmsg);
            callback.onAnswer(answer, answer.getErrorCode());
        }
    }

     byte[] shortArr2byteArr(short[] shortArr, int shortArrLen){
        byte[] byteArr = new byte[shortArrLen * 2];
        ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArr);
        return byteArr;
    }


    void sendQuestEmptyCallback(final RTVTUserInterface.IRTVTEmptyCallback callback, Quest quest) {
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet){
                    callback.onSuccess();
                }else{
                    callback.onError(genRTVTAnswer(answer,errorCode));
                }
            }
        }, globalQuestTimeoutSeconds);
    }

    public void setErrorRecoder(ErrorRecorder value){
        if (value == null)
            return;
        errorRecorder = value;
    }

    public void printLog(String msg){
        errorRecorder.recordError(msg);
    }

    //-------------[ Auth(Login) utilies functions ]--------------------------//
    private void ConfigRtmGateClient(final TCPClient client) {
        client.setKeepAlive(true);
        client.setQuestTimeout(globalQuestTimeoutSeconds);
        processor.client = client;
        processor.serverPushProcessor = serverPushProcessor;

//        if (errorRecorder != null)
//            client.setErrorRecorder(errorRecorder);

//        client.setQuestProcessor(processor, "com.fpnn.rtvt.RTVTCore$RTVTQuestProcessor");
        client.setQuestProcessor(processor, "com.fpnn.rtvt.RTVTServerQuestProcessor");
/*        client.setConnectedCallback(new ConnectionConnectedCallback() {
            @Override
            public void connectResult(InetSocketAddress peerAddress,  boolean connected) {
                serverPushProcessor.connectResult(peerAddress, connected);
            }
        });*/

        client.setWillCloseCallback(new ConnectionWillCloseCallback() {
            @Override
            public void connectionWillClose(InetSocketAddress peerAddress, boolean causedByError) {
                close();
                if (closedCase != CloseType.ByUser)
                    serverPushProcessor.rtvtConnectClose(peerAddress,causedByError);
            }
        });
    }

    //------------voice add---------------//
    private RTVTAnswer auth(String token , long ts) {
        String sharedip = "";

        Quest qt = new Quest("login");
        qt.param("pid", pid);
        qt.param("token", token);
        qt.param("ts", ts);
        qt.param("version", "javaRTVT-" + SDKVersion);

        try {
            Answer answer = rttGate.sendQuest(qt, globalQuestTimeoutSeconds);
//            Answer answer = new Answer(qt);
//            answer.fillErrorCode(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value());
            if (answer.getErrorCode() != ErrorCode.FPNN_EC_OK.value()) {
                closeStatus();
                return genRTVTAnswer(answer.getErrorCode(), "when send sync auth ");
            }
            boolean success = rtvtUtils.wantBoolean(answer,"successed");
            if (!success){
                closeStatus();
                return genRTVTAnswer( 800005, "token is invalid " );
            }

            synchronized (interLocker) {
                rttGateStatus = ClientStatus.Connected;
            }
//            checkRoutineInit();
//            connectionId.set(rttGate.getConnectionId());
            return genRTVTAnswer(okRet);
        }
        catch (Exception  ex){
            closeStatus();
            String errmsg = ex + "";
            return genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),errmsg);
        }
    }

    private void auth(final RTVTUserInterface.IRTVTEmptyCallback callback, final String token, final long ts) {
        final Quest qt = new Quest("login");
        qt.param("pid", pid);
        qt.param("token", token);
        qt.param("ts", ts);
        qt.param("version", "javaRTVT-" + SDKVersion);

        rttGate.sendQuest(qt, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                    String sharedip = "";
                    if (errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        closeStatus();
                        if (answer != null)
                            callback.onError(genRTVTAnswer( errorCode, "when send async auth " + answer.getErrorMessage()));
                        else
                            callback.onError(genRTVTAnswer( errorCode, "when send async auth"));
                        return;
                    } else {
                        boolean success = rtvtUtils.wantBoolean(answer,"successed");
                        if (!success){
                            closeStatus();
                            callback.onError(genRTVTAnswer( 800005, "token is invalid" ));
                            return;
                        }
                        synchronized (interLocker) {
                            rttGateStatus = ClientStatus.Connected;
                        }

//                        checkRoutineInit();
//                        connectionId.set(rttGate.getConnectionId());
                        callback.onSuccess();
                    }
            }
        }, globalQuestTimeoutSeconds);
    }


    void login(final IRTVTEmptyCallback callback, final String token, long ts) {
        if (token ==null || token.isEmpty()){
            callback.onError(genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),"login failed token  is null or empty"));
            return;
        }

        String errDesc = "";
        if (rtvtEndpoint == null || rtvtEndpoint.isEmpty() || rtvtEndpoint.lastIndexOf(':') == -1)
            errDesc = "login failed invalid rtvtEndpoint:" + rtvtEndpoint;
        if (pid <= 0)
            errDesc += "login failed pid is invalid:" + pid;
        if (serverPushProcessor == null)
            errDesc += "login failed RTVTMPushProcessor is null";

        if (!errDesc.equals("")) {
            callback.onError(genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(), errDesc));
            return;
        }

        synchronized (interLocker) {
            if (rttGateStatus == ClientStatus.Connected) {
                callback.onSuccess();
                return;
            }

            if (rttGateStatus == ClientStatus.Connecting){
                callback.onError(genRTVTAnswer(RTVTerrorCode, "last login not finish please wait"));
                return;
            }

            rttGateStatus = ClientStatus.Connecting;
        }

        this.loginToken = token;
        this.loginTs = ts;

        if (rttGate != null) {
            rttGate.close();
            auth(callback, token,ts);
        } else {
            try {
                rttGate = TCPClient.create(rtvtEndpoint);
            }
            catch (IllegalArgumentException ex){
                ex.printStackTrace();
                rttGateStatus = ClientStatus.Closed;
                callback.onError(genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),"create rtvtgate error endpoint Illegal:" +ex + " :" +  rtvtEndpoint ));
                return;
            }
            catch (Exception e){
                rttGateStatus = ClientStatus.Closed;
                e.printStackTrace();
                String msg = "create rtvtgate error orginal error:" + e + " endpoint: " + rtvtEndpoint;
                callback.onError(genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),msg ));
                return;
            }

            closedCase = CloseType.None;
            ConfigRtmGateClient(rttGate);
            auth(callback, token, ts);
        }
    }

    private  void closeStatus() {
        synchronized (interLocker) {
            rttGateStatus = ClientStatus.Closed;
        }
    }


    RTVTAnswer login(String token, long ts) {
        if (token == null || token.isEmpty())
            return genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(), "login failed secretKey  is null or empty");

        String errDesc = "";
        if (rtvtEndpoint == null || rtvtEndpoint.isEmpty() || rtvtEndpoint.lastIndexOf(':') == -1)
            errDesc = " login failed invalid rtvtEndpoint:" + rtvtEndpoint;
        if (pid <= 0)
            errDesc += " login failed pid is invalid:" + pid;
        if (serverPushProcessor == null)
            errDesc += " login failed RTVTMPushProcessor is null";

        if (!errDesc.equals("")) {
            return genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(), errDesc);
        }

        synchronized (interLocker) {
            if (rttGateStatus == ClientStatus.Connected) {
                return genRTVTAnswer(ErrorCode.FPNN_EC_OK.value());
            }

            if (rttGateStatus == ClientStatus.Connecting){
                return genRTVTAnswer(RTVTerrorCode, "last login not finish please wait");
            }

            rttGateStatus = ClientStatus.Connecting;
        }

        this.loginToken = token;
        this.loginTs = ts;

        if (rttGate != null) {
            rttGate.close();
            return auth(loginToken, ts);
        } else {
            try {
                rttGate = TCPClient.create(rtvtEndpoint);
            }
            catch (IllegalArgumentException ex){
                rttGateStatus = ClientStatus.Closed;
                return genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),"create rtvtgate error endpoint Illegal:" +ex + " :" +  rtvtEndpoint );
            }
            catch (Exception e){
                rttGateStatus = ClientStatus.Closed;
                String msg = "create rtvtgate error orginal error:" + e + " endpoint: " + rtvtEndpoint;
                if (rttGate != null)
                    msg = msg + " parse endpoint " + rttGate.endpoint();
                return genRTVTAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),msg );
            }

            closedCase = CloseType.None;
            ConfigRtmGateClient(rttGate);
            return auth(loginToken, ts);
        }
    }



    private void close() {
        synchronized (interLocker) {
            running.set(false);
            if (rttGateStatus == ClientStatus.Closed) {
                return;
            }
            rttGateStatus = ClientStatus.Closed;
        }
        if (rttGate !=null)
            rttGate.close();
    }
}
