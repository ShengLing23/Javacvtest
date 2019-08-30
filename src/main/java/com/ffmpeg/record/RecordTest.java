package com.ffmpeg.record;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

/**
 * @author shengling23
 * @create 2019-08-29 17:12
 */
public class RecordTest {

    public void demo1() throws FrameRecorder.Exception, FrameGrabber.Exception {
        //File inFile = new File("G:\\testffmpeg\\001.flv");
        File outFile = new File("G:\\testffmpeg\\001.mp4");
        String rtmpPaht = "rtmp://192.168.62.150/live/stream1";
        FrameGrabber grabber = new FFmpegFrameGrabber(rtmpPaht);
        grabber.setOption("rw_timeout","1000000");
        grabber.start();

        //视频参数
        int audioCodec = grabber.getAudioCodec();
        int videoCodec = grabber.getVideoCodec();
        int videoBitrate = grabber.getVideoBitrate();    //比特率
        double frameRate = grabber.getFrameRate();      //帧率
        //音频参数
        //想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
        int audioChannels =  grabber.getAudioChannels();
        int audioBitrate = grabber.getAudioBitrate();
        int sampleRate = grabber.getSampleRate();

        int width = grabber.getImageWidth();
        int heigth = grabber.getImageHeight();


        FrameRecorder recorder = FrameRecorder.createDefault(outFile,width,heigth);
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(videoBitrate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("mp4");

        recorder.setAudioChannels(audioChannels);
        recorder.setAudioBitrate(audioBitrate);
        recorder.setSampleRate(sampleRate);

        recorder.start();

        Frame frame = null;

        while ((frame = grabber.grabFrame())!= null){
            recorder.record(frame);
        }
        System.out.println("2");

        recorder.stop();
        grabber.stop();
    }

    public static void main(String[] args) {
        RecordTest demo = new RecordTest();
        try {
            demo.demo1();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
