package com.example.administrator.streamingdemo.model.utils.lib.sea.rtmp.io;

import com.example.administrator.streamingdemo.model.utils.lib.sea.rtmp.packets.RtmpPacket;

/**
 * Handler interface for received RTMP packets
 * @author francois
 */
public interface PacketRxHandler {
    
    public void handleRxPacket(RtmpPacket rtmpPacket);
    
    public void notifyWindowAckRequired(final int numBytesReadThusFar);    
}
