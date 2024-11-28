package com.fpnn.rtvt;

import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.net.InetSocketAddress;

public class RTVTServerQuestProcessor {
    public TCPClient client;
    public RTVTPushProcessor serverPushProcessor;

    RTVTUtils rtvtUtils = new RTVTUtils();
    public Answer recognizedResult(Quest quest, InetSocketAddress peer){
        client.sendAnswer(new Answer(quest));
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

    public Answer recognizedTempResult(Quest quest, InetSocketAddress peer){
        client.sendAnswer(new Answer(quest));
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


    public Answer ttsResult(Quest quest, InetSocketAddress peer){
        client.sendAnswer(new Answer(quest));
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

    public Answer translatedResult(Quest quest, InetSocketAddress peer){
        client.sendAnswer(new Answer(quest));
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

    public Answer translatedTempResult(Quest quest, InetSocketAddress peer){
        client.sendAnswer(new Answer(quest));
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
}
