# RTVT Server Java SDK  API Docs

    /**
     * 创建rtvtclient(单例)
     * @param rtvtEndpoint 连接的地址
     * @param pid 项目id
     * @param pushProcessor 翻译识别的回调类
     */
    public static RTVTClient CreateClient(String rtvtEndpoint, long pid, RTVTPushProcessor pushProcessor)


    /**
     *rtvt登陆
     * @param token   计算的token
     * @param ts  生成token的时间戳(详见 examples/RTVTDemo/src/com/example/ApiSecurityExample.java)
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
    public void login(final String token, final long ts, final RTVTUserInterface.IRTVTEmptyCallback callback)

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
    public void startTranslate(String srcLanguage, String destLanguage, List<String> srcAltLanguage, boolean asrResult, boolean tempResult, boolean transResult, boolean ttsResult, String ttsSpeaker, String userId, RTVTStruct.Codec codec, final RTVTUserInterface.IRTVTCallback<VoiceStream> callback)

    /**
     * 发送语音片段
     * @param streamId (startTranslate返回的流id）
     * @param seq   语音片段序号(尽量有序)
     * @param voicedata 语音数据（如果传入的音频是PCM格式 需要16000采样率 单声道 固定640字节）
     * @param voiceDataTs 音频帧对应时间戳(毫秒)
     * @param callback
     */
    public void sendVoice(long streamId, long seq, byte[] voicedata, long voiceDataTs, RTVTUserInterface.IRTVTEmptyCallback callback) 

    /**
     *开始实时翻译语音流(多语种翻译)
     * @param srcLanguage 源语言(必传)
     * @param srcAltLanguage 备选语言列表(可空 如果传了备选语言 会有3秒自动语种识别 第一句返回的识别和翻译时长会变大）
     * @param asrResult 是否需要语音识别的结果。如果设置为true 识别结果通过recognizedResult回调
     * @param tempResult 是否需要临时识别结果和临时翻译结果 如果设置为true 临时识别结果通过recognizedTempResult回调 翻译临时结果通过translatedTempResult回调(用于长句快速返回)
     * @param ttsResult 是否需要TTS结果，TTS仅合成最终翻译结果
     * @param userId  后台显示便于查询
     */
    public void multi_startTranslate(String srcLanguage, List<String> srcAltLanguage, boolean asrResult,boolean tempResult, boolean ttsResult, String userId, final RTVTUserInterface.IRTVTCallback<VoiceStream> callback)


    /**
     * 发送语音片段(多语种翻译)
     * @param streamId (multi_starTranslate返回的streamid）
     * @param seq  语音片段序号(尽量有序)
     * @param voicedata 语音数据（传入的pcm音频需要16000采样率 单声道 固定640字节）
     * @param voiceDataTs 音频帧对应时间戳
     * @param dstLanguageList 需要翻译的语言列表
     * @param callback
     */
    public void multi_sendVoice(long streamId, long seq, byte[] voicedata, long voiceDataTs, List<String> dstLanguageList, RTVTUserInterface.IRTVTEmptyCallback callback) 


    /**
     * 停止本次翻译流 如需下次继续翻译需要再次调用startTranslate
     * @param streamId 翻译的流id
     */
    public void stopTranslate(long streamId)


    /**
     * 判断当前client的链接状态
     */
    public boolean isOnline() 


    /** 关闭rtvtclient
     * 如再次使用 需要重新调用RTVTCenter.CreateClient
     */
    public void closeRTVT(

    /** 获取版本号
     * 
     */
    public String getSdkVersion()



### RTVT PushProcessor
~~~
    public class RTVTPushProcessor{
    /**
    * RTVT链接断开(需要重新登录 调用开始翻译接口获取新的流id)
    */
    public void rtvtConnectClose(InetSocketAddress peerAddress, boolean causedByError){}

    /**
     *源语言识别结果
     * @param streamId 翻译流id
     * @param startTs  音频开始的毫秒时间戳
     * @param endTs    音频结束的毫秒时间戳
     * @param recTs    识别时间戳，毫秒
     * @param language 识别的语言
     * @param srcVoiceText 识别文本
    * @param taskId     单句对应的任务id
     */
    public void recognizedResult(long streamId, long startTs, long endTs, long recTs, String language, String srcVoiceText,long taskId){}


    /**
     *源语言临时识别结果
     * @param streamId 翻译流id
     * @param startTs  音频开始的毫秒时间戳
     * @param endTs    音频结束的毫秒时间戳
     * @param recTs    识别时间戳，毫秒
     * @param language 识别的语言
     * @param srcVoiceText 识别文本
     * @param taskId    单句对应的任务id
     */
    public void recognizedTempResult(long streamId, long startTs, long endTs, long recTs, String language, String srcVoiceText,long taskId){}


    /**
     *目标言翻译结果
     * @param streamId 翻译流id
     * @param startTs  结果开始的毫秒时间戳
     * @param endTs    结果结束的毫秒时间戳
     * @param recTs    识别时间戳，毫秒
     * @param language 识别的语言
     * @param destVoiceText 翻译文本
     * @param taskId    单句对应的任务id
     */
    public void translatedResult(long streamId, long startTs, long endTs, long recTs, String language, String destVoiceText, long taskId){}

    /**
     *tts合成语音的结果
     * @param streamId 翻译流id
     * @param text  tts对应的文字
     * @param data    tts音频数据，ogg流
     * @param language 识别的语言
     */
    public void ttsResult(long streamId, String text, byte[] data,  String language){}


    /**
     *临时翻译结果
     * @param streamId 翻译流id
     * @param startTs  音频开始的毫秒时间戳
     * @param endTs    音频结束的毫秒时间戳
     * @param recTs    识别时间戳，毫秒
     * @param language 识别的语言
     * @param destVoiceText 临时翻译文本
     * @param taskId    单句对应的任务id
     */
    public void translatedTempResult(long streamId, long startTs, long endTs, long recTs, String language, String destVoiceText,long taskId){}
}
~~~