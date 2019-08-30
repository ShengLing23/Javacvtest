
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

    private String savePath = "G:\\testffmpeg\\";

    private long duration = 5*60*1000L;

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
        Integer width = (Integer) paramMap.get("width");
        Integer heigth = (Integer) paramMap.get("heigth");
        Double frameRate = (Double) paramMap.get("frameRate");
        Integer videoBitrate = (Integer) paramMap.get("videoBitrate");
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
