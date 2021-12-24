package cn.kcrxorg.chengweiepms;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

public class Util {
    static  HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private static SoundPool soundPool;
    private static float volumnRatio;
    private static AudioManager am;
    public static void initSoundPool(Context context){
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        soundMap.put(1, soundPool.load(context, R.raw.beep, 1));
        soundMap.put(2,soundPool.load(context,R.raw.ok,1));
        soundMap.put(3,soundPool.load(context,R.raw.error,1));
        soundMap.put(4,soundPool.load(context,R.raw.netconnect,1));
        soundMap.put(5,soundPool.load(context,R.raw.registererror,1));

        am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);// 实例化AudioManager对象
    }
    public static void playOk()
    {
        playSound(2,0);
    }
    public static void playErr()
    {
        playSound(3,0);
    }
    public static void playconnect(){playSound(4,0);}
    public static void playbreak(){playSound(5,0);}
    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public static void playSound(int id,int num) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    num, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
