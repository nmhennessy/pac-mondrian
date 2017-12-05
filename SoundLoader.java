import javax.swing.*;
import java.applet.*;
import java.net.URL;
import        javax.sound.sampled.*;
import	java.io.IOException;

class SoundLoader extends Thread {
    JApplet applet;
    URL baseURL;
    String relativeURL;
    SoundManager soundManager;
    int kindOfClip;
    
    public SoundLoader(JApplet applet, 
                             String relativeURL,
                             SoundManager soundManager,
                             int kindOfClip) {

        this.soundManager = soundManager;
        this.applet = applet;
        this.baseURL = baseURL;
        this.relativeURL = relativeURL;
        this.kindOfClip = kindOfClip;
        start();
    }

    public void run() {
//        System.out.println("Getting audio clip " + relativeURL);        
        if (kindOfClip == soundManager.AUDIOCLIP) {
//        System.out.println("Getting audio clip " + relativeURL);
//            System.out.println("Getting audio clip " + relativeURL);
            AudioClip audioClip = applet.getAudioClip(applet.getClass().getResource(relativeURL));
            soundManager.putAudioClip(audioClip, relativeURL);
//        System.out.println("Finished getting " + relativeURL);
        } else if (kindOfClip == soundManager.CLIP) {
            AudioInputStream	audioInputStream = null;
            Clip clip = null;
            try
            {
                audioInputStream = AudioSystem.getAudioInputStream(applet.getClass().getResource(relativeURL));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println(relativeURL);
                soundManager.errorSound = true;
            }
            if (audioInputStream != null)
            {
                AudioFormat	format = audioInputStream.getFormat();
                DataLine.Info	info = new DataLine.Info(Clip.class, format);
                try
                {
                    clip = (Clip) AudioSystem.getLine(info);
                    clip.addLineListener(soundManager);
                    clip.open(audioInputStream);
                }
                catch (LineUnavailableException e)
                {
                    e.printStackTrace();
                    soundManager.errorSound = true;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    soundManager.errorSound = true;
                }
                soundManager.putClip(clip, relativeURL);
            }
            
        }
    }
}
