package com.fpnn.rtvt;

import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.fpnn.rtvt.RTVTStruct.*;

import java.util.ArrayList;
import java.util.List;

public class RTVTClient extends RTVTCore {

    /**
     * 创建rtvtclient
     * @param rtvtEndpoint 连接的地址
     * @param pid 项目id
     * @param pushProcessor 翻译识别的回调类
     */
    private static RTVTClient rtvtClient = null;
    public static RTVTClient CreateClient(String rtvtEndpoint, long pid, RTVTPushProcessor pushProcessor) {
        if (rtvtClient ==null){
            rtvtClient = new RTVTClient(rtvtEndpoint, pid, pushProcessor);
        }
        return rtvtClient;
    }

    protected RTVTClient(String rtvtEndpoint, long pid, RTVTPushProcessor serverPushProcessor) {
        RTVTInit(rtvtEndpoint,pid, serverPushProcessor);
    }


    /**
     *rtvt登陆
     * @param token   计算的token
     * @param ts  生成token的时间戳
     */
    public RTVTStruct.RTVTAnswer login(String token, long ts) {
        return super.login(token, ts);
    }

    /**
     *rtvt登陆  async
     * @param token  计算的token
     * @param ts  生成token的时间戳
     * @param callback 登陆结果回调
     */
    public void login(final String token, final long ts, final RTVTUserInterface.IRTVTEmptyCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                login(callback, token, ts);
            }
        }).start();
    }

    /**
     *开始实时翻译语音流(需要先login成功)
     * @param srcLanguage 源语言(必传)
     * @param destLanguage 翻译的目标语言 (如果不需要翻译 可空)
     * @param srcAltLanguage 备选语言列表(可空 如果传了备选语言 会有3秒自动语种识别 第一句返回的识别和翻译时长会变大）
     * @param asrResult 是否需要语音识别的结果。如果设置为true 识别结果通过recognizedResult回调
     * @param tempResult 是否需要临时结果 如果设置为true 临时识别结果和翻译结果通过recognizedTempResult和translatedTempResult回调(用于长句快速的返回)
     * @param transResult 是否需要翻译结果 如果设置为true 翻译结果通过translatedResult回调
     * @param ttsResult 是否需要TTS结果，TTS仅合成最终翻译结果
     * @param ttsSpeaker tts音色
     * @param userId  后台显示便于查询 （业务端可以和返回的streamid绑定）
     * @param codec
     */
    public void startTranslate(String srcLanguage, String destLanguage, List<String> srcAltLanguage, boolean asrResult, boolean tempResult, boolean transResult, boolean ttsResult, String ttsSpeaker, String userId, RTVTStruct.Codec codec, final RTVTUserInterface.IRTVTCallback<VoiceStream> callback){
        if (srcLanguage.isEmpty()){
            callback.onError(genRTVTAnswer(RTVTerrorCode,"srcLanguage is empty"));
            return;
        }

        Quest quest = new Quest("voiceStart");
        quest.param("asrResult", asrResult);
        quest.param("asrTempResult", tempResult);
        quest.param("transResult", transResult);
        quest.param("srcLanguage", srcLanguage);
        quest.param("ttsResult", ttsResult);
        quest.param("ttsSpeaker", ttsSpeaker);
        quest.param("userId", userId);
        quest.param("codec", codec.value());

        if (destLanguage != null)
            quest.param("destLanguage", destLanguage);
        else
            quest.param("destLanguage", "");

        if (srcAltLanguage != null)
            quest.param("srcAltLanguage", srcAltLanguage);
        else
            quest.param("srcAltLanguage", new ArrayList<String>());


        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                VoiceStream ret = new VoiceStream();
                ret.errorCode = errorCode;
                if (errorCode == okRet){
                    ret.streamId = rtvtUtils.wantLong(answer,"streamId");
                    callback.onSuccess(ret);
                }else{
                    callback.onError(genRTVTAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 发送语音片段
     * @param streamId (startTranslate返回的流id）
     * @param seq   语音片段序号(尽量有序)
     * @param voicedata 语音数据（如果传入的音频是PCM格式 需要16000采样率 单声道 固定640字节）
     * @param voiceDataTs 音频帧对应时间戳(毫秒)
     * @param callback
     */
    public void sendVoice(long streamId, long seq, byte[] voicedata, long voiceDataTs, RTVTUserInterface.IRTVTEmptyCallback callback) {
/*
        if (voicedata.length != 640){
            callback.onError(genRTVTAnswer(RTVTerrorCode, "please send 640 bytes length data"));
            return;
        }

*/

        if (streamId <= 0){
            callback.onError(genRTVTAnswer(RTVTerrorCode, "streamId error:" + streamId));
            return;
        }

        Quest quest = new Quest("voiceData");
        quest.param("streamId", streamId);
        quest.param("seq", seq);
        quest.param("data", voicedata);
        quest.param("ts", voiceDataTs);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     *开始实时翻译语音流(多语种翻译)
     * @param srcLanguage 源语言(必传)
     * @param srcAltLanguage 备选语言列表(可空 如果传了备选语言 会有3秒自动语种识别 第一句返回的识别和翻译时长会变大）
     * @param asrResult 是否需要语音识别的结果。如果设置为true 识别结果通过recognizedResult回调
     * @param tempResult 是否需要临时识别结果和临时翻译结果 如果设置为true 临时识别结果通过recognizedTempResult回调 翻译临时结果通过translatedTempResult回调(用于长句快速返回)
     * @param ttsResult 是否需要TTS结果，TTS仅合成最终翻译结果
     * @param userId  后台显示便于查询 （业务端可以和返回的streamid绑定）
     */
    public void multi_startTranslate(String srcLanguage, List<String> srcAltLanguage, boolean asrResult,boolean tempResult, boolean ttsResult, String userId, final RTVTUserInterface.IRTVTCallback<VoiceStream> callback){
        if (srcLanguage.isEmpty()){
            callback.onError(genRTVTAnswer(RTVTerrorCode,"srcLanguage is empty"));
            return;
        }

        Quest quest = new Quest("voiceStart");
        quest.param("asrResult", asrResult);
        quest.param("asrTempResult", tempResult);
        quest.param("srcLanguage", srcLanguage);
        quest.param("transResult", true);
        quest.param("ttsResult", ttsResult);
        quest.param("userId", userId);
        quest.param("destLanguage", "");

        if (srcAltLanguage != null)
            quest.param("srcAltLanguage", srcAltLanguage);
        else
            quest.param("srcAltLanguage", new ArrayList<String>());


        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                VoiceStream ret = new VoiceStream();
                ret.errorCode = errorCode;
                if (errorCode == okRet){
                    ret.streamId = rtvtUtils.wantLong(answer,"streamId");
                    callback.onSuccess(ret);
                }else{
                    callback.onError(genRTVTAnswer(answer, errorCode));
                }
            }
        });
    }


    /**
     * 发送语音片段(多语种翻译)
     * @param streamId (multi_starTranslate返回的streamid）
     * @param seq  语音片段序号(尽量有序)
     * @param voicedata 语音数据（传入的pcm音频需要16000采样率 单声道 固定640字节）
     * @param voiceDataTs 音频帧对应时间戳
     * @param dstLanguageList 需要翻译的语言列表
     * @param callback
     */
    public void multi_sendVoice(long streamId, long seq, byte[] voicedata, long voiceDataTs, List<String> dstLanguageList, RTVTUserInterface.IRTVTEmptyCallback callback) {
        if (voicedata.length != 640){
            callback.onError(genRTVTAnswer(RTVTerrorCode, "please send 640 bytes length data"));
            return;
        }

        if (streamId <= 0){
            callback.onError(genRTVTAnswer(RTVTerrorCode, "streamId error:" + streamId));
            return;
        }

        Quest quest = new Quest("voiceData");
        quest.param("streamId", streamId);
        quest.param("destLangs", dstLanguageList);
        quest.param("seq", seq);
        quest.param("data", voicedata);
        quest.param("ts", voiceDataTs);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 停止本次翻译流 如需下次继续翻译需要再次调用startTranslate
     * @param streamId 翻译的流id
     */
    public void stopTranslate(long streamId){
        Quest quest = new Quest("voiceEnd");
        quest.param("streamId", streamId);
        sendQuestEmptyCallback(new RTVTUserInterface.IRTVTEmptyCallback() {
            @Override
            public void onError(RTVTAnswer answer) {

            }

            @Override
            public void onSuccess() {

            }
        },quest);
    }

    /**
     * 判断当前client的链接状态
     */
    public boolean isOnline() {
        return super.isOnline();
    }

    public void keepAlive(boolean flag){

    }

    /** 关闭rtvtclient
     * 如再次使用 需要重新调用RTVTCenter.CreateClient
     */
    public void closeRTVT(){
        realClose();
        rtvtClient = null;
    }

    public String getSdkVersion(){
        return SDKVersion;
    }
}
