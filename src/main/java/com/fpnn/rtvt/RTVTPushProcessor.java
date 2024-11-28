package com.fpnn.rtvt;

import java.net.InetSocketAddress;

public class RTVTPushProcessor
{
    /**
     * RTVT链接断开
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


