package com.fpnn.rtvt;

public class RTVTUserInterface {

    //返回RTVTAnswer的回调接口
    public interface IRTVTEmptyCallback {
        void onError(RTVTStruct.RTVTAnswer answer);
        void onSuccess();
    }

    //泛型接口 带有一个返回值的回调函数 (请优先判断answer的错误码 泛型值有可能为null)
    public interface IRTVTCallback<T> {
        void onError(RTVTStruct.RTVTAnswer answer);
        void onSuccess(T t);
    }

    //泛型接口 带有两个返回值的回调函数 (请优先判断answer的错误码, 泛型值有可能为null)
    public interface IRTVTDoubleValueCallback<T,V> {
        void onResult(T t, V v, RTVTStruct.RTVTAnswer answer);
    }
}
