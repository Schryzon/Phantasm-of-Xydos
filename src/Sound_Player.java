import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Sound_Player {
    private static final Map<String, Clip> clips = new HashMap<>();
    private static Clip current_music = null;

    public static void play_sound(String name) {
        // Optimistic check: if file doesn't exist, ignore and return.
        try {
            File sound_file = new File("assets/" + name + ".wav");
            if (!sound_file.exists()) {
                return;
            }
            AudioInputStream audio_in = AudioSystem.getAudioInputStream(sound_file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio_in);
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
            current_music.loop(Clip.LOOP_CONTINUOUSLY);
            current_music.start();
        } catch (Exception e) {
            // let it fail silently
        }
    }

    public static void stop_music() {
        if (current_music != null) {
            current_music.stop();
        }
    }
}
