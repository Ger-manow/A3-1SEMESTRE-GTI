package germano.sounds;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundController {

    Clip clickSoundClip;
    Clip explosionSoundClip;
//    File clickSoundFile;
//    File explosionSoundFile;
    AudioInputStream clickAudioInputStream;
    AudioInputStream explosionAudioInputStream;

    public SoundController() {
        try {
            clickSoundClip = AudioSystem.getClip();
            explosionSoundClip = AudioSystem.getClip();
//            clickSoundFile = new File(getClass().getResource("clickSound.wav").getFile());
//            explosionSoundFile = new File(getClass().getResource("explosionSound.wav").getFile());
            clickAudioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("clickSound.wav"));
            clickSoundClip.open(clickAudioInputStream);
            explosionAudioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("explosionSound.wav"));
            explosionSoundClip.open(explosionAudioInputStream);
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException lineUnavailableException) {
        }

    }

    public void playRegularClickSound() {
        if (clickSoundClip.isRunning()) {
        } else {
            clickSoundClip.start();
        }
    }

    public void playExplosionSound() {
        explosionSoundClip.start();
    }
}
