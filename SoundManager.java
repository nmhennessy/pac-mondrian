import java.applet.*;
import java.net.URL;
import        javax.sound.sampled.*;
import	java.io.IOException;

//Loads and holds a bunch of audio files whose locations are specified
//relative to a fixed base URL.
public class SoundManager extends java.util.Hashtable implements LineListener
{
   // Pacman Sounds
   
   static final int  SOUND_PACMANDIES_LENGTH     = 1605;
   static final int  SOUND_CHOMP_LENGTH          = 243;
   static final int  SOUND_RETURNGHOST_LENGTH    = 705;
   static final int  SOUND_START_LENGTH          = 5872;

    static final int CLIP = 0;
   static final int AUDIOCLIP = 1;

   static final int  SOUND_CHOMP          = 1;
   static final int  SOUND_EATGHOST       = 2;
   static final int  SOUND_PACMANDIES     = 3;
   static final int  SOUND_RETURNGHOST    = 4;
   static final int  SOUND_SIREN          = 5;
   static final int  SOUND_START          = 6;
   static final int  SOUND_GHOSTBLUE      = 7;
   static final int  SOUND_EXTRAPAC       = 8;
   static final int  SOUND_EATFRUIT       = 9;

   static final int  SOUND_REDCHOMP          = 10;
   static final int  SOUND_GREYCHOMP         = 11;
   static final int  SOUND_BLUECHOMP         = 12;
   static final int  SOUND_WARP              = 13;
 
   //Toronto Techno sounds
   static final int  SOUND_EATPOWERPILL      = 14;
   static final int  SOUND_GHOSTCHASETIMERUNNINGOUT = 15;
   static final int  SOUND_GAMEOVER         = 16;


   boolean startSoundFinished = false;
   boolean GhostChaseTimeRunningOutSoundOn = false;
   
   PacMondrian   m_pacMan;
   URL      m_baseURL;
   boolean  m_bLoaded = false;
   int      m_nChompTicks = 0;
   boolean  m_bChompLooping = false;
   boolean  m_bSirenLooping = false;
   int      m_nReturnGhostTicks = 0;
   boolean  m_bEnabled  = true;

   boolean errorSound = false;

//String      m_chompFile = "gs_chomp.au";
//String      m_eatGhostFile = "gs_eatghost.au";
String      m_pacmanDiesFile = "dead.au";
String      m_returnGhostFile = "chomp_ghost.au";
String      m_sirenFile = "regular_play.au";
String      m_startFile = "start.au";
String      m_ghostBlueFile = "powerup_play.au";
String      m_extraPacFile = "extra_life.au";
String      m_eatFruitFile = "chomp_fruit.au";
String      m_chompRedFile = "chomp_red.au";
String      m_chompBlueFile = "chomp_blue.au";
String      m_chompGreyFile = "chomp_grey.au";
String      m_warpFile = "warp.au";
String      m_eatPowerPillFile = "power_up.au";
String      m_ghostChaseTimeRunningOutFile = "ghost_chase_time_running_out.au";
String      m_gameOverFile = "game_over.au";

final static int BBW = 1; //load boogie woogie sounds
final static int DetroitTechno = 2; //load anonym detroit techno
final static int TorontoTechno = 3; //load algorithm toronto techno

int         numSounds = 14;

boolean[] m_soundsLoaded;

   public SoundManager (PacMondrian pacMan)
   {
      super(14); //Initialize Hashtable with capacity of 8 entries.
      m_pacMan = pacMan;
   }

   public SoundManager (PacMondrian pacMan, URL baseURL)
   {
      super(14); //Initialize Hashtable with capacity of 8 entries.
      m_pacMan = pacMan;
      m_baseURL = baseURL;
   }

   boolean allSoundsLoaded()
   {
     if (errorSound)
        return true;
     return (size() >= 11);
   }

   public void loadSoundClips (int type)
   {
/**
      long beginLoadTime = System.currentTimeMillis ();
      
      m_chompClip       = m_pacMan.getAudioClip (m_baseURL, "gs_chomp.au");
      m_eatGhostClip    = m_pacMan.getAudioClip (m_baseURL, "gs_eatghost.au");
      m_pacmanDiesClip  = m_pacMan.getAudioClip (m_baseURL, "gs_pacmandies.au");
      m_returnGhostClip = m_pacMan.getAudioClip (m_baseURL, "gs_returnghost.au");
      m_sirenClip       = m_pacMan.getAudioClip (m_baseURL, "gs_siren_soft.au");
      m_startClip       = m_pacMan.getAudioClip (m_baseURL, "gs_start.au");
//      m_ghostBlueClip   = m_pacMan.getAudioClip (m_baseURL, "gs_ghostblue.au");
m_ghostBlueClip   = m_pacMan.getAudioClip (m_baseURL, "bb_powerup.au");
      m_extraPacClip    = m_pacMan.getAudioClip (m_baseURL, "gs_extrapac.au");
      m_eatFruitClip    = m_pacMan.getAudioClip (m_baseURL, "gs_eatfruit.au");
      m_bLoaded = true;
      
      long endLoadTime = System.currentTimeMillis ();
      System.out.println (endLoadTime - beginLoadTime);
**/

//Start asynchronous sound loading.
//startLoading(m_chompFile);
//startLoading(m_eatGhostFile);

         switch (type)
         {
           case BBW:
      m_pacmanDiesFile = "bbw/dead.au";
      m_returnGhostFile = "bbw/chomp_ghost.au";
      m_sirenFile = "bbw/regular_play.au";
      m_startFile = "bbw/start.au";
      m_ghostBlueFile = "bbw/powerup_play.au";
      m_extraPacFile = "bbw/extra_life.au";
      m_eatFruitFile = "bbw/chomp_fruit.au";
      m_chompRedFile = "bbw/chomp_red.au";
      m_chompBlueFile = "bbw/chomp_blue.au";
      m_chompGreyFile = "bbw/chomp_grey.au";
      m_warpFile = "bbw/warp.au";
              break;
           case DetroitTechno:
      m_pacmanDiesFile = "detroittechno/dead.au";
      m_returnGhostFile = "detroittechno/chomp_ghost.au";
      m_sirenFile = "detroittechno/regular_play.au";
      m_startFile = "detroittechno/start.au";
      m_ghostBlueFile = "detroittechno/powerup_play.au";
      m_extraPacFile = "detroittechno/extra_life.au";
      m_eatFruitFile = "detroittechno/chomp_fruit.au";
      m_chompRedFile = "detroittechno/chomp_red.au";
      m_chompBlueFile = "detroittechno/chomp_blue.au";
      m_chompGreyFile = "detroittechno/chomp_grey.au";
      m_warpFile = "detroittechno/warp.au";
              break;

           case TorontoTechno:
      m_pacmanDiesFile = "torontotechno/dead.au";
      m_returnGhostFile = "torontotechno/chomp_ghost.au";
      m_sirenFile = "torontotechno/regular_play.au";
      m_startFile = "torontotechno/start.au";
      m_ghostBlueFile = "torontotechno/powerup_play.au";
      m_extraPacFile = "torontotechno/extra_life.au";
      m_eatFruitFile = "torontotechno/chomp_fruit.au";
      m_chompRedFile = "torontotechno/chomp_red.au";
      m_chompBlueFile = "torontotechno/chomp_blue.au";
      m_chompGreyFile = "torontotechno/chomp_grey.au";
      m_warpFile = "torontotechno/warp.au";
      m_eatPowerPillFile = "torontotechno/power_up.au";
      startLoading(m_eatPowerPillFile, CLIP);
      m_ghostChaseTimeRunningOutFile = "torontotechno/ghost_chase_time_running_out.au";
      startLoading(m_ghostChaseTimeRunningOutFile, AUDIOCLIP);
      m_gameOverFile = "torontotechno/game_over.au";
      startLoading(m_gameOverFile, AUDIOCLIP);
              break;



         }



//Load start sound as a clip, because we need to load
//it as a Clip to get a stop event to start the game
//The other clips are loaded as AudioClips because
//when the clip has to play while it's already playing
//anything I tried with Clip sounded crappy
//with AudioClip it sounds good just calling play again
startLoading(m_pacmanDiesFile, AUDIOCLIP);
startLoading(m_returnGhostFile, AUDIOCLIP);
startLoading(m_sirenFile, AUDIOCLIP);
startLoading(m_ghostBlueFile, AUDIOCLIP);
startLoading(m_extraPacFile, AUDIOCLIP);
startLoading(m_eatFruitFile, AUDIOCLIP);
startLoading(m_chompRedFile, AUDIOCLIP);
startLoading(m_chompBlueFile, AUDIOCLIP);
startLoading(m_chompGreyFile, AUDIOCLIP);
startLoading(m_warpFile, AUDIOCLIP);
startLoading(m_startFile, CLIP);

   }

    public void startLoading(String relativeURL, int ClipType) {
        new SoundLoader(m_pacMan,
                              relativeURL, this, ClipType);
 }

    public AudioClip getClip(String relativeURL) {
        return (AudioClip)get(relativeURL);
    }

    public void putAudioClip(AudioClip clip, String relativeURL) {
        put(relativeURL, clip);
    }

    public void putClip(Clip clip, String relativeURL) {
        put(relativeURL, clip);
    }

   public void update(LineEvent e)
   {
        if (e.getType() == LineEvent.Type.OPEN || e.getType() == LineEvent.Type.CLOSE || e.getType() == LineEvent.Type.START)
            return;
        String clip = e.getLine().toString();
        if (clip.compareTo(((Clip)get(m_startFile)).toString()) == 0) {
            ((Clip)get(m_startFile)).stop();
            ((Clip)get(m_startFile)).setFramePosition(0);
            startSoundFinished = true;
        }
        if (get(m_eatPowerPillFile) != null) {
            if (clip.compareTo(((Clip)get(m_eatPowerPillFile)).toString()) == 0) {
                ((Clip)get(m_eatPowerPillFile)).stop();
                ((Clip)get(m_eatPowerPillFile)).setFramePosition(0);
 	            getClip(m_ghostBlueFile).loop();
            }
        }
   }

   public void tickSound ()
   {
/**
      if (m_nChompTicks > 0)
      {
         m_nChompTicks--;
         if (m_nChompTicks == 0)
         {
            m_bChompLooping = false;
            stopSound (SOUND_CHOMP);
         }
      }
      if (m_nReturnGhostTicks > 0)
      {
         m_nReturnGhostTicks--;
         if (m_nReturnGhostTicks == 0)
         {
            stopSound (SOUND_RETURNGHOST);
         }
      }
**/
   }
   
   // Public method exposed for other classes to play
   // various sounds. 
   public void playSound (int soundEnum)
   {
      if (size() < 11 || !m_bEnabled || errorSound)
         return;
      
      switch (soundEnum)
      {
      case SOUND_CHOMP:
         m_nChompTicks = SOUND_CHOMP_LENGTH / m_pacMan.m_delay; // Length of this clip in Ticks
         if (!m_bChompLooping)
         {
//            getClip(m_chompFile).loop();
            m_bChompLooping = true;
         }
         break;
   
      case SOUND_REDCHOMP:
         getClip(m_chompRedFile).play ();
         break;

      case SOUND_BLUECHOMP:
         getClip(m_chompBlueFile).play ();
         break;

      case SOUND_GREYCHOMP:
         getClip(m_chompGreyFile).play ();
         break;
      
      case SOUND_PACMANDIES:
         getClip(m_pacmanDiesFile).play ();
         break;
      
      case SOUND_RETURNGHOST:
         m_nReturnGhostTicks = (SOUND_RETURNGHOST_LENGTH / m_pacMan.m_delay) * 2;
//         getClip(m_returnGhostFile).loop ();
         getClip(m_returnGhostFile).play ();
         break;
      
      case SOUND_SIREN:
         if (!m_bSirenLooping)
         {
            getClip(m_sirenFile).loop();
            m_bSirenLooping = true;
         }
         break;
         
      case SOUND_START:
          ((Clip)get(m_startFile)).start ();
          break;
          
      case SOUND_GHOSTBLUE:
          getClip(m_ghostBlueFile).loop();
          break;
          
      case SOUND_EXTRAPAC:
          getClip(m_extraPacFile).play ();
          break;
          
      case SOUND_EATFRUIT:
          getClip(m_eatFruitFile).play ();
          break;

      case SOUND_WARP:
          getClip(m_warpFile).play ();
          break;

      case SOUND_EATPOWERPILL:
          stopSound(SOUND_GHOSTCHASETIMERUNNINGOUT);
          ((Clip)get(m_eatPowerPillFile)).start ();
          break;

      case SOUND_GHOSTCHASETIMERUNNINGOUT:
         getClip(m_ghostChaseTimeRunningOutFile).loop();
         break;

      case SOUND_GAMEOVER:
          getClip(m_gameOverFile).play ();
          break;

      }
   }
   
   // Public method exposed for other classes to stop 
   // any sound whether it is playing or looping
   public void stopSound (int soundEnum)
   {
      if (size() < 11)
         return;
      
      switch (soundEnum)
      {
      case SOUND_CHOMP:
//         getClip(m_chompFile).stop ();
         m_nChompTicks = 0;
         m_bChompLooping = false;
         break;
   
      case SOUND_EATGHOST:
//         getClip(m_eatGhostFile).stop ();
         break;
      
      case SOUND_PACMANDIES:
         getClip(m_pacmanDiesFile).stop ();
         break;
      
      case SOUND_RETURNGHOST:
         getClip(m_returnGhostFile).stop ();
         m_nReturnGhostTicks = 0;
         break;
      
      case SOUND_SIREN:
         getClip(m_sirenFile).stop ();
         m_bSirenLooping = false;
         break;
      
      case SOUND_START:
         ((Clip)get(m_startFile)).stop ();
         break;
         
      case SOUND_GHOSTBLUE:
         getClip(m_ghostBlueFile).stop ();
         break;
         
      case SOUND_EXTRAPAC:
         getClip(m_extraPacFile).stop ();
         break;
         
      case SOUND_EATFRUIT:
         getClip(m_eatFruitFile).stop ();
         break;

      case SOUND_REDCHOMP:
         getClip(m_eatFruitFile).stop ();
         break;

      case SOUND_BLUECHOMP:
         getClip(m_eatFruitFile).stop ();
         break;

      case SOUND_GREYCHOMP:
         getClip(m_eatFruitFile).stop ();
         break;

      case SOUND_WARP:
         getClip(m_warpFile).stop ();
         break;

      case SOUND_GHOSTCHASETIMERUNNINGOUT:
         getClip(m_ghostChaseTimeRunningOutFile).stop();
         GhostChaseTimeRunningOutSoundOn = false;
	 break;

      }
   }
   
   public void stop ()
   {
      if (size() < 11)
         return;

//      getClip(m_chompFile).stop ();
//      getClip(m_eatGhostFile).stop ();
      getClip(m_pacmanDiesFile).stop ();
      getClip(m_returnGhostFile).stop ();
      getClip(m_sirenFile).stop ();
      ((Clip)get(m_startFile)).stop ();
      getClip(m_ghostBlueFile).stop ();
      getClip(m_extraPacFile).stop ();
      getClip(m_eatFruitFile).stop ();
      getClip(m_chompBlueFile).stop ();
      getClip(m_chompRedFile).stop ();
      getClip(m_chompGreyFile).stop ();
      m_nChompTicks = 0;
      m_bChompLooping = false;
      m_bSirenLooping = false;
      m_nReturnGhostTicks = 0;
   }
}
