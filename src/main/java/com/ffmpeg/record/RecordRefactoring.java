package com.ffmpeg.record;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shengling23
 * @create 2019-08-30 10:39
 */
public class RecordRefactoring {

    //视频存储地址
    private String savePath = "G:\\testffmpeg\\";
    //每段视频的时长
    private long duration = 5*60*1000L;

    /**
     *
     * @param rtmpPath  rtmp流地址
     * @param outFileNamePre   录制的视频的名称
     * @throws FrameGrabber.Exception
     * @throws FrameRecorder.Exception
     */
    public void record(String rtmpPath,String outFileNamePre) throws FrameGrabber.Exception, FrameRecorder.Exception {
        boolean flag = true;
        int error_count = 0;
        FrameGrabber grabber = initFrameGrabber(rtmpPath);
        grabber.start();
        Map<String, Object> params = getParamsFromFrameGrabber(grabber);
        Frame frame = null;

        while ((frame = grabber.grabFrame())!= null && flag){
            File outFile = getOutFile(outFileNamePre, duration);
            FrameRecorder recorder  = initFrameRecorder(outFile,params);
            recorder.start();
            long now = System.currentTimeMillis();
            long startTime = now;
            Frame frame2 = null;
            for(;(now - startTime) <= duration;now = System.currentTimeMillis()){
                frame2 = grabber.grabFrame();
                if (frame2 == null){
                    error_count += 1;
                    if (error_count > 5){
                        flag = false;
                        break;
                    }
                    continue;
                }
                recorder.record(frame2);
            }
            recorder.stop();
        }
        grabber.stop();
    }

    private FrameGrabber initFrameGrabber(String rtmpPath){
        FrameGrabber grabber = new FFmpegFrameGrabber(rtmpPath);
        grabber.setOption("rw_timeout","1000000");
        return grabber;
    }

    private Map<String,Object> getParamsFromFrameGrabber(FrameGrabber grabber){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("width",grabber.getImageWidth());           // int 类型
        map.put("heigth",grabber.getImageHeight());         // int 类型
        map.put("videoBitrate",grabber.getVideoBitrate());  // int 类型
        map.put("frameRate",grabber.getFrameRate());        //double 类型
        map.put("audioChannels",grabber.getAudioChannels()); // int 类型
        map.put("audioBitrate",grabber.getAudioBitrate());   // int 类型
        map.put("sampleRate",grabber.getSampleRate());       // int 类型
        return map;
    }

    /**
     * 通过输出文件名前缀和持续的时长，生成输出的视频文件
     * @param outFileNamePre 输出文件名前缀
     * @param duration       持续的时长
     * @return               输出的文件
     */
    private File getOutFile(String outFileNamePre,long duration){
        long start = System.currentTimeMillis();
        String startStr = formatTime(start);
        String endTime = formatTime(start + duration);
        StringBuilder fileName = new StringBuilder();
        fileName.append(outFileNamePre).append("[")
                .append(startStr).append("]")
                .append("-").append("[")
                .append(endTime).append("]")
                .append(".mp4");

        File file = new File(fileName.toString());
        return file;
    }

    private String formatTime(long time){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sf.format(time);
    }

    private FrameRecorder initFrameRecorder(File outFile,Map<String,Object> paramMap){
        // 视频的长、宽
        Integer width = (Integer) paramMap.get("width");
        Integer heigth = (Integer) paramMap.get("heigth");
        // 帧率
        Double frameRate = (Double) paramMap.get("frameRate");
        // 比特率
        Integer videoBitrate = (Integer) paramMap.get("videoBitrate");

        //音频的参数
        //想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
        Integer audioChannels = (Integer) paramMap.get("audioChannels");
        Integer audioBitrate = (Integer) paramMap.get("audioBitrate");
        Integer sampleRate = (Integer) paramMap.get("sampleRate");

        FrameRecorder recorder = null;
        try {
            recorder = FrameRecorder.createDefault(outFile,width,heigth);
            recorder.setFrameRate(frameRate);
            recorder.setVideoBitrate(videoBitrate);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("mp4");
            recorder.setAudioChannels(audioChannels);
            recorder.setAudioBitrate(audioBitrate);
            recorder.setSampleRate(sampleRate);
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        return recorder;
    }

    public static void main(String[] args) {
        String rtmpPath = "rtmp://192.168.62.150/live/stream1";
        String preFileName = "stream1";
        RecordRefactoring recordRefactoring = new RecordRefactoring();
        try {
            recordRefactoring.record(rtmpPath,preFileName);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }
}
