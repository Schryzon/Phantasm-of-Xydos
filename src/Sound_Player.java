import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Sound_Player {
    private static final Map<String, Clip> clips = new HashMap<>();
    private static Clip current_music = null;

    private static void set_volume(Clip clip, int volume_level) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain_control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (volume_level <= 0) {
                    gain_control.setValue(gain_control.getMinimum()); // Mute
                } else {
                    // Map 1-100 to decibel gain (e.g. -30dB to 0dB)
                    float vol = volume_level / 100.0f;
                    float db = (float) (Math.log10(vol) * 20.0);
                    if (db < gain_control.getMinimum()) {
                        db = gain_control.getMinimum();
                    } else if (db > gain_control.getMaximum()) {
                        db = gain_control.getMaximum();
                    }
                    gain_control.setValue(db);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void play_sound(String name) {
        try {
            File sound_file = new File("assets/" + name + ".wav");
            if (!sound_file.exists()) {
                return;
            }
            AudioInputStream audio_in = AudioSystem.getAudioInputStream(sound_file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio_in);
            set_volume(clip, Config_Manager.sfx_volume);
            clip.start();
        } catch (Exception e) {
            // let it fail silently
        }
    }

    public static void play_music(String type) {
        try {
            if (current_music != null) {
                current_music.stop();
                current_music.close();
            }

            File music_file = new File("assets/" + type + "_bgm.wav");
            if (!music_file.exists()) {
                return;
            }

            AudioInputStream audio_in = AudioSystem.getAudioInputStream(music_file);
            current_music = AudioSystem.getClip();
            current_music.open(audio_in);
            set_volume(current_music, Config_Manager.music_volume);
            current_music.loop(Clip.LOOP_CONTINUOUSLY);
            current_music.start();
        } catch (Exception e) {
            // let it fail silently
        }
    }

    public static void update_music_volume() {
        if (current_music != null && current_music.isOpen()) {
            set_volume(current_music, Config_Manager.music_volume);
        }
    }

    public static void stop_music() {
        if (current_music != null) {
            current_music.stop();
        }
    }
}
