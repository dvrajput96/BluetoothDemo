package com.example.pc.bluetoothdemo;

/**
 * Created by pc on 2/9/18.
 */

public class DeviceData {

    private String name;
    private String macID;
    private boolean isHide = false;
    private int isConnect = 0;
    private String msg;
    private boolean isReceiver;

    public boolean isReceiver() {
        return isReceiver;
    }

    public void setReceiver(boolean receiver) {
        isReceiver = receiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getIsConnect() {
        return isConnect;
    }

    public void setIsConnect(int isConnect) {
        this.isConnect = isConnect;
    }

    public DeviceData(String name, String macID) {
        this.name = name;
        this.macID = macID;
    }

    public DeviceData() {
    }

    public boolean isHide() {
        return isHide;
    }

    public void setHide(boolean hide) {
        isHide = hide;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacID() {
        return macID;
    }

    public void setMacID(String macID) {
        this.macID = macID;
    }
}
