package cn.kcrxorg.chengweiepms.networkutil;

public interface NetStateChangeObserver {
    void onNetDisconnected();
    void onNetConnected(NetworkType networkType);
}