package com.example.administrator.streamingdemo.control.core.encoder;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class H264ToFLV {
    public byte[] in_buffer;
    public byte[] out_buffer;

    private final String TAG = "H264ToFLV";

    int p; // Point to current byte in buffer
    private long timestamp = 0;
    private long compositeTime = 0;
    private int step = 40;

    private byte[] sps;
    private byte[] pps;

    final int MAX_BUFFER = 8 * 2014;
    private FileOutputStream out;

    public H264ToFLV() {
        Log.d(TAG, "H264ToFLV init");
        try {
            out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/out.flv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        try {
            Log.d(TAG, "close outstream");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readInput(byte[] bBuffers) {
        Log.d(TAG, "readInput, bSize = " + bBuffers.length);
        try {
            int p = 0;
            int size = MAX_BUFFER;
            if (size > bBuffers.length) {
                size = bBuffers.length;
            }
            while (p < bBuffers.length) {
                //Get data from buffer
                if (bBuffers.length - p < size) {
                    size = bBuffers.length - p;
                }
                byte[] buff = new byte[size];
                System.arraycopy(bBuffers, p, buff, 0, size);

                //Convert data
                System.out.println("Convert buffer:" + buff.length);
                appendInputBuffer(buff);
                int rem = convert();
                System.out.println("Remain:" + rem);
                if (out_buffer != null) {
//					if(rem >=0){
//						for (int i = 0; i < con.out_buffer.length; i++) {
//							System.out.print(" " + Integer.toHexString(con.out_buffer[i] & 0xFF));
//							if (i % 20 == 19 ) {
//								System.out.println("");
//								break;
//							}
//						}
//					}
                    out.write(out_buffer);
                    out_buffer = null; //Clear buffer
                }
                p += size;
            }
        } catch (Exception e) {
            System.out.println("Error in get byte. \n");
            e.printStackTrace();
        }
    }

    /****
     * Main Function
     ******/
    public static void main(String[] args) {

        final int MAX_BUFFER = 8 * 2014;

        // The name of the file to open.
        String fileName = "E:\\vetv\\test_jni\\video1.h264";
        String outfileName = "test.flv";

        File file = new File(fileName);

        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];


        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);

            fileInputStream.read(bFile);
            fileInputStream.close();

            FileOutputStream out = new FileOutputStream(outfileName);

            H264ToFLV con = new H264ToFLV();

            int p = 0;
            int size = MAX_BUFFER;
            if (size > bFile.length) {
                size = bFile.length;
            }
            while (p < bFile.length) {
                //Get data from buffer
                if (bFile.length - p < size) {
                    size = bFile.length - p;
                }
                byte[] buff = new byte[size];
                System.arraycopy(bFile, p, buff, 0, size);

                //Convert data
                System.out.println("Convert buffer:" + buff.length);
                con.appendInputBuffer(buff);
                int rem = con.convert();
                System.out.println("Remain:" + rem);
                if (con.out_buffer != null) {
//					if(rem >=0){
//						for (int i = 0; i < con.out_buffer.length; i++) {
//							System.out.print(" " + Integer.toHexString(con.out_buffer[i] & 0xFF));
//							if (i % 20 == 19 ) {
//								System.out.println("");
//								break;
//							}
//						}
//					}
                    out.write(con.out_buffer);
                    con.out_buffer = null; //Clear buffer
                }
                p += size;
            }
            out.close();
        } catch (Exception e) {
            System.out.println("Error in get byte. \n");
            e.printStackTrace();
        }

    }

    /*****
     * Queue process
     *******/
    int convert() {
        if (in_buffer == null) {
            System.out.println("Buffer is empty.\n");
            return -1;
        }
        byte[] nal = null;
        while ((nal = get_next_nal()) != null) {
            System.out.println("Found NAL at offset " + (p - nal.length) + " with size of " + nal.length + "\n");
            int nal_type = get_nal_type(nal);

            if (nal_type == 7) {  // SPS NAL
                //Store data
                System.out.println("Store SPS data. \n");
                sps = new byte[nal.length];
                System.arraycopy(nal, 0, sps, 0, nal.length);
            } else if (nal_type == 8) {
                //Store data
                System.out.println("Store PPS data. \n");
                pps = new byte[nal.length];
                System.arraycopy(nal, 0, pps, 0, nal.length);
                //Start write flv format
                if (sps.length > 0) {
                    timestamp = 0;
                    compositeTime = 0;

                    //Flv header
                    System.out.println("Write flv header. \n");
                    byte[] ret = flv_write_header(false, true);
                    appendOutBuffer(ret);

                    System.out.println("Write FLV sequence Header. \n");
                    byte[] ret1 = flv_write_sequence_header();
                    appendOutBuffer(ret1);
                }

            } else if (nal_type == 5) {
                if (sps.length > 0 && pps.length > 0) {
                    System.out.println("Write key frame. \n");
                    byte[] ret = flv_write_video_key_frame(nal);
                    appendOutBuffer(ret);
                    timestamp += step;
                    compositeTime = timestamp;
                }
            } else if (nal_type == 1) {
                if (sps.length > 0 && pps.length > 0) {
                    System.out.println("Write non key frame. \n");
                    byte[] ret = flv_write_video_normal_frame(nal);
                    appendOutBuffer(ret);
                    timestamp += step;
                    compositeTime = timestamp;
                }
            }
        }
        removeProcessedNal();
        if (in_buffer == null) {
            return -1;
        }
        return in_buffer.length;
    }

    void removeProcessedNal() {
        int remain_len = in_buffer.length - p;
        if (remain_len <= 0) {
            in_buffer = null;
        } else {
            byte[] remain = new byte[remain_len];
            System.arraycopy(in_buffer, p, remain, 0, remain_len);
            in_buffer = remain;
        }
        p = 0;
    }

    void appendInputBuffer(byte[] data) {
        if (in_buffer == null) {
            in_buffer = new byte[data.length];
            System.arraycopy(data, 0, in_buffer, 0, data.length);
        } else {
            in_buffer = concatenateByteArrays(in_buffer, data);
        }
        System.out.println("New input buffer: " + in_buffer.length);
    }

    void appendOutBuffer(byte[] data) {
        if (out_buffer == null) {
            out_buffer = new byte[data.length];
            System.arraycopy(data, 0, out_buffer, 0, data.length);
        } else {
            out_buffer = concatenateByteArrays(out_buffer, data);
        }
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /*****
     * Convert function
     ******/
    byte[] flv_write_video_normal_frame(byte[] nal) {
        int nal_size = nal.length;
        byte[] tag = new byte[nal_size + 4];
        int n = 0;
        //4 bytes: dataSize;
        tag[n] = (byte) ((nal_size >> 24) & 0xFF);
        n++;
        tag[n] = (byte) ((nal_size >> 16) & 0xFF);
        n++;
        tag[n] = (byte) ((nal_size >> 8) & 0xFF);
        n++;
        tag[n] = (byte) ((nal_size >> 0) & 0xFF);
        n++;
        //Copy data
        for (int i = 0; i < nal_size; i++) {
            tag[n] = nal[i];
            n++;
        }
        byte[] newtag = new byte[n];
        System.arraycopy(tag, 0, newtag, 0, n);
        return flv_tag(newtag, 9, timestamp, compositeTime, false, false);
    }

    byte[] flv_write_video_key_frame(byte[] nal) {
        int nal_size = nal.length;
        byte[] tag = new byte[nal_size + 12 + sps.length + pps.length];

        int n = 0;
        //4 bytes: SPS size;
        tag[n] = (byte) ((sps.length >> 24) & 0xFF);
        n++;
        tag[n] = (byte) ((sps.length >> 16) & 0xFF);
        n++;
        tag[n] = (byte) ((sps.length >> 8) & 0xFF);
        n++;
        tag[n] = (byte) ((sps.length >> 0) & 0xFF);
        n++;

        //Copy SPS
        for (int i = 0; i < sps.length; i++) {
            tag[n] = sps[i];
            n++;
        }

        //4 bytes : PPS size
        tag[n] = (byte) ((pps.length >> 24) & 0xFF);
        n++;
        tag[n] = (byte) ((pps.length >> 16) & 0xFF);
        n++;
        tag[n] = (byte) ((pps.length >> 8) & 0xFF);
        n++;
        tag[n] = (byte) ((pps.length >> 0) & 0xFF);
        n++;

        //Copy PPS
        for (int i = 0; i < pps.length; i++) {
            tag[n] = pps[i];
            n++;
        }
        //4 bytes: data size
        tag[n] = (byte) ((nal_size >> 24) & 0xFF);
        n++;
        tag[n] = (byte) ((nal_size >> 16) & 0xFF);
        n++;
        tag[n] = (byte) ((nal_size >> 8) & 0xFF);
        n++;
        tag[n] = (byte) ((nal_size >> 0) & 0xFF);
        n++;

        //Copy data
        for (int i = 0; i < nal_size; i++) {
            tag[n] = nal[i];
            n++;
        }
        byte[] newtag = new byte[n];
        System.arraycopy(tag, 0, newtag, 0, n);
        return flv_tag(newtag, 9, timestamp, compositeTime, true, false);
    }

    byte[] flv_write_header(boolean hasAudio, boolean hasVideo) {
        byte[] tag = {'F', // Signature byte always 'F' (0x46)
                'L', // Signature byte always 'L' (0x4C)
                'V', // Signature byte always 'V' (0x56)
                0x01, // File version (for example, 0x01 for FLV version 1)
                (byte) ((hasAudio ? 0x04 : 0x00) | (hasVideo ? 0x01 : 0x00)), // UB[5]  Must be 0 & Audio tags are present, UB[1] Audio tags are present, UB[1] Must be 0, UB[1] Video tags are present
                0x0, 0x0, 0x0, // Offset in bytes from start of file to start of body (that is, size of header)
                0x09, // Version
                0x0, 0x0, 0x0, 0x0 // PreviousTagSize0, Always 0
        };
        return tag;
    }

    byte[] flv_write_sequence_header() {
        byte[] tag = new byte[20 + sps.length + pps.length];
        int n = 0;
        tag[0] = 0x01; // version
        tag[1] = sps[1]; // profile
        tag[2] = sps[2]; // compatibility
        tag[3] = sps[3]; // level

        tag[4] = (byte) (0xFC | 3); // reserved (6 bits), NULA length size - 1
        // (2 bits)
        tag[5] = (byte) (0xE0 | 1); // reserved (3 bits), num of SPS (5 bits)
        tag[6] = (byte) (0xFF & (sps.length >> 8)); // 2 bytes for length of SPS
        tag[7] = (byte) (0xFF & (sps.length >> 0));
        n = 8;
        // data of SPS
        for (int i = 0; i < sps.length; i++) {
            tag[n] = sps[i];
            n++;
        }
        tag[n] = 0x01;
        n++;
        // 2 bytes for length of PPS
        tag[n] = (byte) (0xFF & (pps.length >> 8));
        n++;
        tag[n] = (byte) (0xFF & (pps.length >> 0));
        n++;
        // data of PPS
        for (int i = 0; i < pps.length; i++) {
            tag[n] = pps[i];
            n++;
        }
        byte[] newtag = new byte[n];
        System.arraycopy(tag, 0, newtag, 0, n);
        // Copy to buffer
        return flv_tag(newtag, 9, 0, 0, true, true);
    }

    byte[] flv_tag(byte[] data, int tagType, long timestamp, long composite_time, boolean keyframe, boolean sequenceHeader) {
        byte[] tag = new byte[data.length + 32];

        int n = 0;
        tag[0] = (byte) tagType;
        int dataSize = data.length;
        switch ((int) tagType) {
            case 9: // Video
            {

                // DataSize
                dataSize += 5; // 5 is video header size + 4 byte length of data
                tag[1] = (byte) ((dataSize >> 16) & 0xFF);
                tag[2] = (byte) ((dataSize >> 8) & 0xFF);
                tag[3] = (byte) ((dataSize >> 0) & 0xFF);
                System.out.println("Datasize: " + dataSize);
                // Timestamp
                tag[4] = (byte) ((timestamp >> 16) & 0xFF);
                tag[5] = (byte) ((timestamp >> 8) & 0xFF);
                tag[6] = (byte) ((timestamp >> 0) & 0xFF);
                // TimestampExtended
                tag[7] = (byte) ((timestamp >> 24) & 0xFF);

                // StreamID
                tag[8] = (byte) 0x00; // StreamId
                tag[9] = (byte) 0x00;
                tag[10] = (byte) 0x00;

                // Videdo data (5 bytes header)
                tag[11] = (byte) ((keyframe ? 0x10 : 0x20) | 0x07); // UB[4] FrameType + UB[4]  CodecID: AVC AVCVIDEOPACKET
                tag[12] = (byte) (sequenceHeader ? 0x00 : 0x01); // AVCPacketType:  AVC sequence  header | AVC  NALU
                composite_time = sequenceHeader ? 0 : (composite_time - timestamp); // CompositionTime
                // : if (AVCPacketType  ==  1)  {  Composition time offset  }  else  { 0 }
                tag[13] = (byte) ((composite_time >> 16) & 0xFF);
                tag[14] = (byte) ((composite_time >> 8) & 0xFF);
                tag[15] = (byte) ((composite_time >> 0) & 0xFF);
                n = 16;
                // Data UI8[n]: if (AVCPacketType == 0){  AVCDecoderConfigurationRecord }
                // else if AVCPacketType == 1  One or more NALUs (can be individual slices per FLV packets; that is, full frames are not strictly required)
                // else if AVCPacketType == 2  Empty
            }
            break;
            case 8: // Audio
            {
                // DataSize
                dataSize += 2; // a is aac header size + 4 byte of length
                tag[1] = (byte) ((dataSize >> 16) & 0xFF);
                tag[2] = (byte) ((dataSize >> 8) & 0xFF);
                tag[3] = (byte) ((dataSize >> 0) & 0xFF);

                // Timestamp
                tag[4] = (byte) ((timestamp >> 16) & 0xFF);
                tag[5] = (byte) ((timestamp >> 8) & 0xFF);
                tag[6] = (byte) ((timestamp >> 0) & 0xFF);
                // TimestampExtended
                tag[7] = (byte) ((timestamp >> 24) & 0xFF);

                // StreamID
                tag[8] = (byte) 0x00; // StreamId
                tag[8] = (byte) 0x00;
                tag[10] = (byte) 0x00;

                // Audio data (5 bytes header)
                tag[11] = (byte) (0xA0 | 0x0F); // UB[4] SoundFormat (AAC) + UB[2]  SoundRate: 44-kHz + UB[1]  SoundSize: snd16Bit + UB[1]  SoundType: sndStereo AACAUDIODATA
                tag[12] = (byte) (sequenceHeader ? 0x00 : 0x01); // AACPacketType: 0: AAC sequence header | 1: AAC raw  Data UI8[n] if (AACPacketType == 0 ) AudioSpecificConfig  else if (AACPacketType == 1) Raw AAC frame data
                n = 13;
            }
            break;
            default:
                System.out.println("Cannot find FLG tag type.\n");
                return null;
        }
        // Add NALU
        for (int i = 0; i < data.length; i++) {
            tag[n] = data[i];
            n++;
        }

        // Print data size of previoous tag
        dataSize += 11;
        System.out.println("NalU: " + dataSize);
        tag[n] = (byte) ((dataSize >> 24) & 0xFF);
        n++;
        tag[n] = (byte) ((dataSize >> 16) & 0xFF);
        n++;
        tag[n] = (byte) ((dataSize >> 8) & 0xFF);
        n++;
        tag[n] = (byte) ((dataSize >> 0) & 0xFF);
        n++;

        byte[] newtag = new byte[n];
        System.arraycopy(tag, 0, newtag, 0, n);
        return newtag;
    }

    /********
     * NaL function
     ********/
    byte[] get_next_nal() {
        // find start
        int nal_start = p;
        int nal_end = p;

        if (in_buffer == null) {
            return null;
        }

        if (p + 3 > in_buffer.length) {
            return null;
        }
        int i = p;
        while ( // ( next_bits( 24 ) != 0x000001 && next_bits( 32 ) !=
            // 0x00000001 )
                (in_buffer[i] != 0 || in_buffer[i + 1] != 0 || in_buffer[i + 2] != 0x01)
                        && (in_buffer[i] != 0 || in_buffer[i + 1] != 0
                        || in_buffer[i + 2] != 0 || in_buffer[i + 3] != 0x01)) {
            i++; // skip leading zero
            if (i + 4 >= in_buffer.length) {
                return null;
            } // did not find nal start
        }
        if (in_buffer[i] != 0 || in_buffer[i + 1] != 0
                || in_buffer[i + 2] != 0x01) // ( next_bits( 24 ) != 0x000001 )
        {
            i++;
        }

        if (in_buffer[i] != 0 || in_buffer[i + 1] != 0
                || in_buffer[i + 2] != 0x01) {
            /* error, should never happen */
            return null;
        }
        i += 3;
        nal_start = i;

        while ( // ( next_bits( 24 ) != 0x000000 && next_bits( 24 ) != 0x000001
            // )
                (in_buffer[i] != 0 || in_buffer[i + 1] != 0 || in_buffer[i + 2] != 0)
                        && (in_buffer[i] != 0 || in_buffer[i + 1] != 0 || in_buffer[i + 2] != 0x01)) {
            i++;
            // FIXME the next line fails when reading a nal that ends exactly at
            // the end of the data
            if (i + 3 >= in_buffer.length) {
                nal_end = in_buffer.length;
                return null;
            } // did not find nal end, stream ended first
        }

        nal_end = i;
        int nal_size = nal_end - nal_start;
        if (nal_size == 0) {
            return null;
        }

        byte[] nal = new byte[nal_size];
        System.arraycopy(in_buffer, nal_start, nal, 0, nal_size);
        p = nal_end;  //Move pointer to end of nal
        return nal;
    }

    /**
     **/
    byte get_nal_type(byte[] nal) {
        return (byte) (nal[0] & 0x1F);
    }

}
