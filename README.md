# RTVT-server-sdk-java
[TOC]

## Depends

* [fpnn-sdk-java](https://github.com/highras/fpnn-sdk-java)

### Language Level:

Java 8

### Notice

* Before using the SDK, please make sure the server time is correct, RTVT-Server will check whether the signature time has expired.

## Usage

### For Maven Users:
    <dependency>
        <groupId>com.github.highras</groupId>
        <artifactId>rtvt-serve</artifactId>
        <version>1.1.0</version>
    </dependency>


### Create
    RTVTClient rtvtClient = RTVTClient.CreateClient(endpoint, pid, new RTVTPushProcessor());
    *Note: you need extends RTVTPushProcessor for receive serverpush message and connect event


### Close (Optional)
    rtvtClient.closeRTVT();


### SDK Version

    System.out.println("RTVT SDK Version: " + rtvtClient.getSdkVersion);

### How to startTranslate

* startTranslate
~~~
  long ts = System.currentTimeMillis() / 1000;
  String realToken = ApiSecurityExample.genHMACToken(pid, ts, secretKey);
  rtvtClient.login(realToken, ts, new RTVTUserInterface.IRTVTEmptyCallback()
  rtvtClient.startTranslate(String srcLanguage, String destLanguage, List<String> srcAltLanguage, boolean asrResult, boolean tempResult, boolean transResult, boolean ttsResult, String ttsSpeaker, String userId, RTVTStruct.Codec codec, final RTVTUserInterface.IRTVTCallback<VoiceStream> callback)
  rtvtClient.sendVoice(long streamId, long seq, byte[] voicedata, long voiceDataTs, RTVTUserInterface.IRTVTEmptyCallback callback)
  rtvtClient.stopTranslate(streamId)
~~~



## API Docs

Please refer:

* [API docs](doc/API.md)
* [RTVT errorCode](doc/ErrorCode.md)

