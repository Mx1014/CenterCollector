package com.xjy.entity;

import com.xjy.parms.Constants;
import com.xjy.parms.InternalMsgType;
import com.xjy.util.CheckUtil;

import java.io.UnsupportedEncodingException;

/**
 * @Author: Mr.Xu
 * @Date: Created in 9:10 2018/9/29
 * @Description: 内部协议消息实体类
 */
public class InternalMsgBody {
     InternalMsgType msgType = InternalMsgType.INVALID_PACKAGE; //3种类型，心跳包，发送数据包，接收数据包
     String deviceId; //11字节的设备号
     int[] effectiveBytes;//有效数据
     int crcCode; //crc校验码

    //发送数据时的构造方法
    public InternalMsgBody(String addr,int[] effectiveBytes){
        this.deviceId = addr;
        msgType = InternalMsgType.SEND_PACKAGE;
        this.effectiveBytes = effectiveBytes;
    }
    //接收数据时的构造方法,确定设备地址和消息类型
    public InternalMsgBody(int[] data){
        //取出设备地址，前11个字节
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0 ; i < 11 ;i ++){
            stringBuilder.append((char)data[i]);
        }
        deviceId = stringBuilder.toString();
        if(data.length == 11) msgType = InternalMsgType.HEARTBEAT_PACKAGE;
        else if(data.length > 21 ){
            boolean isRecvPack = true;
            for(int i = 11 ; i < 21; i++){
                if((byte)data[i] != Constants.INTERNAL_RECVHEAD[i-11]){
                    System.out.println("发送数据不匹配！");
                    isRecvPack = false;
                }
            }
            if(isRecvPack) {
                msgType = InternalMsgType.RECV_PACKAGE;
                //给有效数据赋值
                effectiveBytes = new int[data.length - 21 -3]; // 有效数据长度
                for(int i = 0; i < effectiveBytes.length ; i++){
                    effectiveBytes[i] = data[i+21];
                }
                crcCode = data[data.length - 3] << 8 | data[data.length-2];
                //作crc校验
                if(!CheckUtil.crcCheck(effectiveBytes,crcCode)){
                    System.out.println("crc校验失败！");
                    msgType = InternalMsgType.INVALID_PACKAGE;
                }
            }
        }
    }
    //把消息实体转为字节数组，发送前调用
    public byte[] toBytes(){
        return null;
    }
    //获取指令字，有效数据的前三个字节
    public String getInstruction(){
        if(effectiveBytes == null || effectiveBytes.length < 3){
            return null;
        }
        byte[] orders = new byte[3];
        for(int i = 0 ; i < 3; i++){
            orders[i] = (byte)effectiveBytes[i];
        }
        String instruction = null;
        try {
            instruction = new String(orders,"ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return instruction;
    }

    public InternalMsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(InternalMsgType msgType) {
        this.msgType = msgType;
    }

    public int[] getEffectiveBytes() {
        return effectiveBytes;
    }

    public void setEffectiveBytes(int[] effectiveBytes) {
        this.effectiveBytes = effectiveBytes;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
