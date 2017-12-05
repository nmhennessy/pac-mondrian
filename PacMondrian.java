import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.Math;
import javax.swing.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.awt.image.BufferedImage;

public class PacMondrian extends JApplet// implements Runnable
{
   Model      m_gameModel;
   TopCanvas      m_topCanvas;
   BottomCanvas   m_bottomCanvas;
   ArcadeWriter     m_arcadeWriter;
   UI         m_gameUI;
   GameClock         m_ticker;      // Used to update the game state and UI
   SoundManager   m_soundMgr;
   int            m_globalTickCount = 0;
   
   int            m_ticksPerSec;    // These two variables control game speed 
   int            m_delay;          // Milliseconds between ticks

   int        credits;

   
   public void init ()
   {
      credits = 0;

      setTicksPerSec (35);

      m_soundMgr = new SoundManager (this);
      m_soundMgr.loadSoundClips (SoundManager.BBW);
      
      // Create canvases and layout
	  this.setBackground(Color.black);
      m_gameModel = new Model (this);
      m_arcadeWriter = new ArcadeWriter(getCodeBase(), this);

      m_topCanvas = new TopCanvas (m_gameModel, 658, 60, m_arcadeWriter);
      m_gameUI = new UI (this, m_gameModel, 658, 654, m_arcadeWriter); // original size (774, 770)
      m_bottomCanvas = new BottomCanvas (this, m_gameModel, 658, 54, m_arcadeWriter);
//    this.setSize(943,654);
      this.setSize(1024,768);
      
	  BufferedImage bim = new BufferedImage(1,1,BufferedImage.TYPE_4BYTE_ABGR);
	  setCursor(getToolkit().createCustomCursor(bim,(new Point(0,0)),"HiddenM")); 
       
      GridBagLayout gridBag = new GridBagLayout ();
      GridBagConstraints c = new GridBagConstraints ();
      
      getContentPane().setLayout(gridBag);
      getContentPane().setBackground(Color.black);
	  c.gridheight = 3;
	  c.weighty = 1.0;
	  c.gridx = 0;
	  c.gridy = 0;
	  Canvas canvas = new Canvas();
	  canvas.setSize(184,782);
	  gridBag.setConstraints (canvas, c);
	  getContentPane().add (canvas);

      c.gridx = 2;
      c.gridy = 0;	  
 	  Canvas rcanvas = new Canvas();
	  rcanvas.setSize(184,782);
	  gridBag.setConstraints (rcanvas, c);
	  getContentPane().add (rcanvas);

/**   	  c.weighty = 0.0;		   //reset to the default
      c.gridwidth = GridBagConstraints.REMAINDER; //end row
      c.gridheight = 1;		   //reset to the default
**/
      c.weighty = 0.0;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 0;
      gridBag.setConstraints (m_topCanvas, c);
      getContentPane().add (m_topCanvas);
      
      c.gridx = 1;
      c.gridy = 1;
      gridBag.setConstraints (m_gameUI, c);
      getContentPane().add (m_gameUI);
      
	  c.gridx = 1;
	  c.gridy = 2;
      gridBag.setConstraints (m_bottomCanvas, c);
      getContentPane().add (m_bottomCanvas);
      
      requestFocus ();
      // Add event subscribers
      addKeyListener (new KeyControls(this));
                
      validate ();
      
      
   }
   
   // Master ticker that runs various parts of the game
   // based on the Model's STATE
	public void tick ()
   {
      //long temp = System.currentTimeMillis ();
      m_globalTickCount++;
      
      if (m_gameModel.m_state == Model.STATE_ABOUT)
      {
         m_soundMgr.stop ();
         m_gameModel.m_bIntroInited = false;
         m_gameUI.m_bShowIntro = false;
         m_gameUI.m_bShowAbout = true;
         m_gameUI.m_bRedrawAll = true;
         m_gameModel.m_nTicks2AboutShow++;
         if (m_gameModel.m_nTicks2AboutShow == 15000 / m_delay)
         {
            m_gameModel.m_state = Model.STATE_INTRO;
            m_gameModel.m_nTicks2AboutShow = 0;
         }
         
      } else if (m_gameModel.m_state == Model.STATE_INTRO)
      {
         m_gameUI.m_bShowIntro = true;
         m_bottomCanvas.repaint();
         if (m_soundMgr.errorSound || m_soundMgr.allSoundsLoaded()) {
            m_gameModel.m_nTicksTilIntroDone++;
         }
         if (m_gameModel.m_nTicksTilIntroDone >= 15000 / m_delay && m_soundMgr.allSoundsLoaded())
         {
            m_gameModel.m_state = Model.STATE_NEWGAME;
            
         }
      } else if (m_gameModel.m_state == Model.STATE_PAUSED)
      {
         m_gameUI.m_bDrawPaused = true;
         m_gameUI.m_bRedrawAll = true;
         m_gameUI.repaint ();
         return;
      
      } else if (m_gameModel.m_state == Model.STATE_NEWGAME)
      {
         m_gameModel.m_nTicksTilIntroDone = 0;
         m_soundMgr.stop ();
         m_gameModel.newGame ();
         m_gameModel.m_state = Model.STATE_BEGIN_PLAY;
         m_gameModel.m_nTicks2BeginPlay = 0;
         m_gameModel.m_bIntroInited = false;
         m_gameUI.m_bShowIntro = false;
         m_gameUI.m_bShowAbout = false;
         m_gameUI.m_bRedrawAll = true;
         
      } else if (m_gameModel.m_state == Model.STATE_GAMEOVER)
      {
         if (m_gameModel.m_nTicks2GameOver == 0)
         {
            if (m_gameModel.m_stage % 3 == 0)
                 m_soundMgr.playSound(SoundManager.SOUND_GAMEOVER);
            m_soundMgr = new SoundManager (this);
            m_soundMgr.loadSoundClips (SoundManager.BBW);
            if (m_gameModel.m_player.m_score > m_gameModel.m_highScore)
            {
               m_gameModel.m_highScore = m_gameModel.m_player.m_score;
               m_topCanvas.repaint ();  
            }   
         }
         
         m_gameModel.m_nTicks2GameOver++;
         
         // After 3 seconds go to the intro page
         if (m_gameModel.m_nTicks2GameOver == 5500 / m_delay)
         {
            m_gameModel.m_state = Model.STATE_INTRO;
            m_gameModel.m_nTicks2GameOver = 0;
            m_gameModel.m_pacMan.m_gameModel.m_player.m_demoMode = true;
         }
         
         m_gameUI.m_bDrawGameOver = true;
         m_gameUI.m_bRedrawAll = true;
         m_gameUI.repaint ();
         return;
         
      } else if (m_gameModel.m_state == Model.STATE_LEVELCOMPLETE)
      {
         m_soundMgr.stop ();
   	 m_gameModel.m_bPlayStartClip = true;
         m_soundMgr.startSoundFinished = false;
         m_gameModel.m_bStartClipPlayed = false;
         tickLevelComplete ();
      
      } else if (m_gameModel.m_state == Model.STATE_DEADPACMAN)
      {
         m_soundMgr.stop ();
         if (m_gameModel.m_nLives == 0)
         {
            m_gameModel.m_state = Model.STATE_GAMEOVER;
            m_gameModel.m_nTicks2GameOver = 0;
            
         } else {
            m_gameModel.restartGame ();
   	    m_gameModel.m_bPlayStartClip = false;
            m_soundMgr.startSoundFinished = true;
            m_gameModel.m_bStartClipPlayed = true;
            m_gameModel.m_state = Model.STATE_BEGIN_PLAY;
            m_bottomCanvas.repaint ();
         
         }
            
      } else if (m_gameModel.m_state == Model.STATE_BEGIN_PLAY)
      {
         tickBeginPlay ();
      
      } else if (m_gameModel.m_state == Model.STATE_PLAYING)
      {
         tickGamePlay ();
      } else if (m_gameModel.m_state == Model.STATE_DEAD_PLAY)
      {
         tickDeadPlay ();
      } else if (m_gameModel.m_state == Model.STATE_BBWIntro)
      {
         m_bottomCanvas.repaint();
         if (m_soundMgr.errorSound || m_soundMgr.allSoundsLoaded()) {
            m_gameModel.m_nTicksTilIntroDone++;
         }
         if (m_gameModel.m_nTicksTilIntroDone >= 7500 / m_delay && m_soundMgr.allSoundsLoaded())
         {
            m_gameModel.m_state = Model.STATE_BEGIN_PLAY;
         }
      } else if (m_gameModel.m_state == Model.STATE_DetroitTechnoIntro)
      {
         m_bottomCanvas.repaint();
         if (m_soundMgr.errorSound || m_soundMgr.allSoundsLoaded()) {
            m_gameModel.m_nTicksTilIntroDone++;
         }
         if (m_gameModel.m_nTicksTilIntroDone >= 7500 / m_delay && m_soundMgr.allSoundsLoaded())
         {
            m_gameModel.m_state = Model.STATE_BEGIN_PLAY;
         }
      } else if (m_gameModel.m_state == Model.STATE_TorontoTechnoIntro)
      {
         m_bottomCanvas.repaint();
         if (m_soundMgr.errorSound || m_soundMgr.allSoundsLoaded()) {
            m_gameModel.m_nTicksTilIntroDone++;
         }
         if (m_gameModel.m_nTicksTilIntroDone >= 7500 / m_delay && m_soundMgr.allSoundsLoaded())
         {
            m_gameModel.m_state = Model.STATE_BEGIN_PLAY;
         }
      }           
      m_gameUI.repaint();  
      m_topCanvas.repaint ();
     
	}
   
   // Ticked when level has completed
   public void tickLevelComplete ()
   {
      if (m_gameModel.m_nTicks2LevelComp == 0)
      {
         m_gameModel.setPausedGame (true);
         m_gameUI.m_bRedrawAll = true;
      }
      
      m_gameModel.m_nTicks2LevelComp++;
      
      // One second later, hide things and start flashing the board
      if (m_gameModel.m_nTicks2LevelComp == 600 / m_delay)
      {
         m_gameModel.setVisibleSprites (false);
         m_gameModel.m_player.m_bVisible = true;
         m_gameUI.m_bFlipWallColor = true;
         m_gameUI.m_bRedrawAll = true;
         
      } else if (m_gameModel.m_nTicks2LevelComp > 600 / m_delay &&
                (m_gameModel.m_nTicks2LevelComp % (200 / m_delay)) == 0)
      {
         m_gameUI.m_bFlipWallColor = !m_gameUI.m_bFlipWallColor;
         m_gameUI.m_bRedrawAll = true;
      }
      
      if (m_gameModel.m_nTicks2LevelComp == 1900 / m_delay)
      {
         // This will advance the level and set the State to STATE_BEGIN_PLAY
         m_gameModel.loadNextLevel();
         m_soundMgr = new SoundManager (this);
         switch (m_gameModel.m_stage % 3)
         {
           case 1:
              m_soundMgr.loadSoundClips (SoundManager.BBW);
              m_gameModel.m_state = Model.STATE_BBWIntro;
              m_gameUI.m_bShowIntro = true;
           break;
           case 0:
              m_soundMgr.loadSoundClips (SoundManager.TorontoTechno);
              m_gameModel.m_state = Model.STATE_TorontoTechnoIntro;
              m_gameUI.m_bShowTorontoTechnoIntro = true;
           break;
           case 2:
              m_soundMgr.loadSoundClips (SoundManager.DetroitTechno);
              m_gameModel.m_state = Model.STATE_DetroitTechnoIntro;
              m_gameUI.m_bShowDetroitTechnoIntro = true;
          break;
         }
                  
         m_gameModel.m_nTicks2LevelComp = 0;
         m_gameUI.m_bFlipWallColor = false;
         m_gameUI.m_bRedrawAll = true;
         m_bottomCanvas.repaint ();
         m_gameModel.m_nTicksTilIntroDone = 0;
      }
   }
   
   // Ticked when Pacman has died
   public void tickDeadPlay ()
   {
      if (m_gameModel.m_nTicks2DeadPlay == 0)
      {
         m_gameModel.setPausedGame (true);
         m_gameModel.m_player.m_rotationDying = 0;
         m_gameModel.m_player.m_mouthDegreeDying = 45;
         m_gameModel.m_player.m_mouthArcDying = 135;
         m_gameUI.m_bRedrawAll = true;
         m_gameModel.m_nOrigTicksPerSecond = m_ticksPerSec;
         setTicksPerSec (35);
         m_soundMgr.stop ();
      }
      
      m_gameModel.m_nTicks2DeadPlay++;
      
      if (m_gameModel.m_nTicks2DeadPlay == 1000 / m_delay)
      {
         m_gameModel.m_player.m_bDrawDead = true;
         for (int i = 0; i < m_gameModel.m_ghosts.length; i++)
         {
            m_gameModel.m_ghosts[i].setVisible (false); 
         }
         m_gameModel.m_fruit.setVisible (false);
         m_gameUI.m_bRedrawAll = true;
         m_soundMgr.playSound (SoundManager.SOUND_PACMANDIES);
      }
      
      if (m_gameModel.m_nTicks2DeadPlay == (SoundManager.SOUND_PACMANDIES_LENGTH + 1000) / m_delay)
      {
         m_gameModel.m_state = Model.STATE_DEADPACMAN;  //STATE_LEVELCOMPLETE
         m_gameModel.m_nTicks2DeadPlay = 0;
         setTicksPerSec (m_gameModel.m_nOrigTicksPerSecond);
         m_gameUI.m_bRedrawAll = true;
      }
   }
      
   // Ticked when the game is about to begin play
   public void tickBeginPlay ()
   {
    m_gameUI.m_bShowIntro = false;
    m_gameUI.m_bShowDetroitTechnoIntro = false;
    m_gameUI.m_bShowTorontoTechnoIntro = false;
    if (m_gameModel.m_nTicks2BeginPlay == 0)
      {
         m_gameModel.setVisibleSprites (false);
         m_gameModel.setPausedGame (false);
         m_gameUI.m_bDrawReady = true;
         m_gameUI.m_bDrawGameOver = false;
         m_gameUI.m_bRedrawAll = true;
         m_gameUI.m_bFlipWallColor = false;
         m_gameUI.refreshRedrawHash ();
         if (m_gameModel.m_bPlayStartClip)
         {
            m_soundMgr.playSound (SoundManager.SOUND_START);
            m_gameModel.m_bPlayStartClip = false;
         } 
         m_bottomCanvas.repaint (); 
      }
      
      m_gameModel.m_nTicks2BeginPlay++;
      
      if (m_gameModel.m_nTicks2BeginPlay == 500 / m_delay)
      {
         m_gameModel.setVisibleSprites (true);
         m_gameModel.m_fruit.setVisible (false);
      }
      
      if ((m_soundMgr.startSoundFinished && !m_gameModel.m_bStartClipPlayed) ||
          (m_gameModel.m_nTicks2BeginPlay == 1000 / m_delay && (m_gameModel.m_bStartClipPlayed || m_soundMgr.errorSound)))
      {
         m_soundMgr.startSoundFinished = false;
         m_gameModel.m_state = Model.STATE_PLAYING;
         m_gameModel.setVisibleSprites (true);
         m_gameModel.m_fruit.setVisible (false);
         m_gameModel.setPausedGame (false);
         m_gameUI.m_bDrawReady = false;
         m_gameUI.m_bRedrawAll = true;
         m_gameModel.m_nTicks2BeginPlay = 0;
         m_gameModel.m_bStartClipPlayed = false;
         m_soundMgr.playSound (SoundManager.SOUND_SIREN);
      }
   }
   
   // Ticked when the game is playing normally
   public void tickGamePlay ()
   {
      boolean  bFleeing = false;
      int      nCollisionCode;
      
      // Check if player has earned free life
      if (m_gameModel.m_player.m_score >= m_gameModel.m_nextFreeUp)
      {
         m_soundMgr.playSound (SoundManager.SOUND_EXTRAPAC);
         m_gameModel.m_nLives += 1;
         m_gameModel.m_nextFreeUp += 10000;
         m_bottomCanvas.repaint ();
      }
      
      // Check for collisions between Sprites and Pacman
      for (int i =0; i < m_gameModel.m_things.length; i++)
      {
         nCollisionCode = m_gameModel.m_things[i].checkCollision (m_gameModel.m_player);
         
         if (nCollisionCode == 1) // Ghost was eaten
         {
//            m_soundMgr.playSound (SoundManager.SOUND_EATGHOST);
            m_soundMgr.playSound (SoundManager.SOUND_RETURNGHOST);
            break; // Must be eaten one tick at a time
         } else if (nCollisionCode == 2) // Pacman was caught.
         {
            m_gameModel.m_state = Model.STATE_DEAD_PLAY; //STATE_LEVELCOMPLETE;
            m_gameModel.m_player.m_direction = Sprite.STILL;
            m_gameModel.m_nTicks2DeadPlay = 0;
            return;
         } else if (nCollisionCode == 3) // Pacman ate a Fruit
         {
            m_soundMgr.playSound (SoundManager.SOUND_EATFRUIT);
            break; // Must be eaten one tick at a time // TODO
         }
      }
      
      // Tick and then Move each Sprite (includes Pacman and Ghosts)
      for (int i = 0; i < m_gameModel.m_things.length; i++)
      {
         m_gameModel.m_things[i].tickSprite ();     
         if (m_gameModel.m_things[i].canMove ())
            Move (m_gameModel.m_things[i]);    
      }
      
      // Check to see if there are any fleeing Ghosts left
      // because of a power up pacman ate.
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
      {
         bFleeing |= m_gameModel.m_ghosts[i].m_nTicks2Flee > 0;
      }
      // If no fleeing ghosts, then reset the Power Up eat ghost score back to 200
      // and kill the BlueGhost loop
      if (bFleeing != true)
      {
         m_gameModel.m_eatGhostPoints = 200;
	 if (m_soundMgr.GhostChaseTimeRunningOutSoundOn)
             m_soundMgr.stopSound (SoundManager.SOUND_GHOSTCHASETIMERUNNINGOUT);
	 else
             m_soundMgr.stopSound (SoundManager.SOUND_GHOSTBLUE);

         m_soundMgr.playSound (SoundManager.SOUND_SIREN);
      }
         
      if (m_gameModel.m_currentFoodCount == m_gameModel.m_totalFoodCount)
      {
         m_gameModel.m_state = Model.STATE_LEVELCOMPLETE;
         m_gameModel.m_nTicks2LevelComp = 0;
      }
      // Tick the sound manager (mainly to check if the Chomping loop needs to be stopped)
      m_soundMgr.tickSound ();
      if (!m_gameModel.m_player.m_demoMode && m_gameModel.m_highScore < m_gameModel.m_player.m_score) {
          m_gameModel.m_highScore = m_gameModel.m_player.m_score;
      }
   }
   
   // Ticked when the game is running the intro
   public void tickIntro ()
   {
      boolean  bFleeing = false;
      int      nCollisionCode;
      
      if (!m_gameModel.m_bIntroInited)
      {
         m_gameModel.initIntro ();
         setTicksPerSec (35);
         m_gameModel.m_bIntroInited = true;
         m_gameUI.m_bRedrawAll = true;
      }
      
      // Check if Ghost has run to the left of the Runway
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
      {
         if (m_gameModel.m_ghosts[i].m_locX == 19)
            m_gameModel.m_ghosts[i].m_bPaused = true;
      }
      
      if (!m_gameModel.m_ghosts[0].m_bVisible)
      {
         m_gameModel.m_ghosts[0].m_bVisible = true;
         m_gameModel.m_ghosts[0].m_bPaused = false;
      }
      
      if (!m_gameModel.m_ghosts[1].m_bVisible && m_gameModel.m_ghosts[0].m_locX == 19)
      {
         m_gameModel.m_ghosts[1].m_bVisible = true;
         m_gameModel.m_ghosts[1].m_bPaused = false;
      }
      
      if (!m_gameModel.m_ghosts[2].m_bVisible && m_gameModel.m_ghosts[1].m_locX == 19)
      {
         m_gameModel.m_ghosts[2].m_bVisible = true;
         m_gameModel.m_ghosts[2].m_bPaused = false;
      }
      
      if (!m_gameModel.m_ghosts[3].m_bVisible && m_gameModel.m_ghosts[2].m_locX == 19)
      {
         m_gameModel.m_ghosts[3].m_bVisible = true;
         m_gameModel.m_ghosts[3].m_bPaused = false;
      }
      
      if (!m_gameModel.m_player.m_bVisible && m_gameModel.m_ghosts[3].m_locX == 19)
      {
         m_gameModel.m_player.m_bVisible = true;
         m_gameModel.m_player.m_bPaused = false;
      }
      
      if (m_gameModel.m_player.m_locX == 23)
         m_gameModel.m_player.m_requestedDirection = Sprite.LEFT;
      
      if (m_gameModel.m_player.m_locX == 5)
         m_gameModel.m_player.m_requestedDirection = Sprite.RIGHT;
      
      // Tick and then Move each Sprite (includes Pacman and Ghosts)
      for (int i = 0; i < m_gameModel.m_things.length; i++)
      {
         m_gameModel.m_things[i].tickSprite ();     
         if (m_gameModel.m_things[i].canMove ())
            Move (m_gameModel.m_things[i]);    
      }
   }
   
   // This method is called to update the Sprite's Location and delta Locations
   // based on Sprite's m_direction.  The ONLY update to Sprite's m_direction is
   // if Sprite hits a wall and m_direction is set to STILL.  Otherwise, all 
   // m_direction changes occur in the Sprite's virtual method tickSprite ().
	public void Move (Sprite thing)
   {
      if (thing.m_direction == Sprite.STILL)
         return;
      
      boolean bMoved = false;
      
      thing.m_lastLocX = thing.m_locX;
      thing.m_lastLocY = thing.m_locY;
      thing.m_lastDeltaLocX = thing.m_deltaLocX;
      thing.m_lastDeltaLocY = thing.m_deltaLocY;
         
      // See if thing can eat any nearby items
      thing.eatItem (Model.GS_FOOD);
      thing.eatItem (Model.GS_POWERUP);
               
      // Based on the current direction, update thing's location in that direction.
      // The thing.m_deltaLocX != 0 is so that if the thing is in a cell with a wall
      // and the thing is directed towards the wall, he will still need to move towards the
      // wall until the thing is dead center in the cell.
      if (thing.m_direction == Sprite.LEFT &&
         ((m_gameModel.m_gameState [thing.m_locX][thing.m_locY] & Model.GS_WEST) == 0 || thing.m_deltaLocX != 0))
      {
         thing.m_deltaLocX--;
         bMoved = true;
      } else if (thing.m_direction == Sprite.RIGHT &&
          ((m_gameModel.m_gameState [thing.m_locX][thing.m_locY] & Model.GS_EAST) == 0 || thing.m_deltaLocX != 0))
      {
         thing.m_deltaLocX++;
         bMoved = true;
      } else if (thing.m_direction == Sprite.UP &&
          ((m_gameModel.m_gameState [thing.m_locX][thing.m_locY] & Model.GS_NORTH) == 0 || thing.m_deltaLocY != 0))
      {
         thing.m_deltaLocY--;
         bMoved = true;
      } else if (thing.m_direction == Sprite.DOWN &&
          ((m_gameModel.m_gameState [thing.m_locX][thing.m_locY] & Model.GS_SOUTH) == 0 || thing.m_deltaLocY != 0))
      {
         thing.m_deltaLocY++; 
         bMoved = true;
      }
         
      // If the thing has moved past the middle of the two cells, then switch his
      // location to the other side.
      if (thing.m_deltaLocX <= -thing.m_deltaMax) // Shift thing to adjacent cell on left
      {
thing.m_lastOccupiedX = thing.m_locX;
thing.m_lastOccupiedY = thing.m_locY;
         if (thing.m_locX != 0)
         {
            thing.m_deltaLocX = thing.m_deltaMax - 1;
            thing.m_locX--;
            bMoved = true;
            
         } else {
            // Check to see if thing should warp to right side
            if (thing.m_deltaLocX < -thing.m_deltaMax)
            {
               thing.m_deltaLocX = thing.m_deltaMax - 1;
               thing.m_locX = m_gameModel.m_gameSizeX - 1;
               bMoved = true;
            }
         }
      } else if (thing.m_deltaLocX >= thing.m_deltaMax)  // Shift thing to adjacent cell on right
      {
thing.m_lastOccupiedX = thing.m_locX;
thing.m_lastOccupiedY = thing.m_locY;
         if (thing.m_locX != m_gameModel.m_gameSizeX - 1)
         {
            thing.m_deltaLocX = 1 - thing.m_deltaMax;
            thing.m_locX++;
            bMoved = true;
         } else {
            // Check to see if thing should warp to left side
            if (thing.m_deltaLocX > thing.m_deltaMax)
            {
               thing.m_deltaLocX = 1 - thing.m_deltaMax;
               thing.m_locX = 0;
               bMoved = true;
            }
         }
      } else if (thing.m_deltaLocY <= -thing.m_deltaMax) // Shift thing to adjacent cell on top
      {
thing.m_lastOccupiedX = thing.m_locX;
thing.m_lastOccupiedY = thing.m_locY;
        if (thing.m_locY != 0)
         {
            thing.m_deltaLocY = thing.m_deltaMax - 1;
            thing.m_locY--;
            bMoved = true;
            
         } else {
            // Check to see if thing should warp to bottom side
            if (thing.m_deltaLocY < -thing.m_deltaMax)
            {
               thing.m_deltaLocY = thing.m_deltaMax - 1;
               thing.m_locY = m_gameModel.m_gameSizeY - 1;
               bMoved = true;
            }
         }
      } else if (thing.m_deltaLocY >= thing.m_deltaMax) // Shift thing to adjacent cell on bottom
      {
thing.m_lastOccupiedX = thing.m_locX;
thing.m_lastOccupiedY = thing.m_locY;
         if (thing.m_locY != m_gameModel.m_gameSizeY - 1)
         {
            thing.m_deltaLocY = 1 - thing.m_deltaMax;
            thing.m_locY++;
            bMoved = true;
         } else {
            // Check to see if thing should warp to top side
            if (thing.m_deltaLocY > thing.m_deltaMax)
            {
               thing.m_deltaLocY = 1 - thing.m_deltaMax;
               thing.m_locY = 0;
               bMoved = true;
            }
         }                  
      }
      
      if (!bMoved)
      {
          thing.m_direction = Sprite.STILL;
//          thing.m_SuperWarping = false;
      } else if (thing.m_type == Sprite.PACMAN && (m_gameModel.m_gameState[thing.m_locX][thing.m_locY] & Model.GS_SUPERWARP) != 0) {
          m_soundMgr.playSound(m_soundMgr.SOUND_WARP);
          thing.m_lastSuperWarpOccupiedX = thing.m_locX;
          thing.m_lastSuperWarpOccupiedY = thing.m_locY;
//          thing.m_SuperWarping = true;
          Point p = new Point(m_gameModel.m_superWarpPairs[(int)thing.m_direction][thing.m_locX][thing.m_locY]);
          thing.m_locX = (int)p.x;
          thing.m_locY = (int)p.y;
          thing.m_destinationX = p.x;
          thing.m_destinationY = p.y;
          thing.m_deltaLocX = 0;
          thing.m_deltaLocY = 0;
      }
	}
   
   public void start ()
   {
      if (m_ticker == null)
      {
         m_ticker = new GameClock (this);
         m_ticker.start ();
      }
   }
   
   public void stop ()
   {
      m_ticker = null;
      m_soundMgr.stop ();
   }
   
   void setTicksPerSec (int ticks)
   {
      m_ticksPerSec = ticks;
      m_delay = 1000 / m_ticksPerSec;
   }   
   
   void toggleGhostAI ()
   {
      for (int i = 0; i < m_gameModel.m_ghosts.length; i++)
      {
         m_gameModel.m_ghosts[i].m_bInsaneAI = !m_gameModel.m_ghosts[i].m_bInsaneAI;
      }
   }
   
   /* Can't run Pacman as an application since it use sound-related methods.
*/
   public static void main (String args[])
   {
      // Create new window
      MainFrame frame = new MainFrame ("PacMan");
//      frame.setUndecorated(true);
      frame.setBounds(0,0,1024,768);
      
      // Create PacMan instance
      PacMondrian pacMan = new PacMondrian ();
      initialize(pacMan);
      // Initialize instance
      pacMan.init ();
      
      frame.add ("Center", pacMan);
      frame.pack ();
      frame.show ();
      
      pacMan.start ();
   }

	public static void initialize(JApplet applet)
	{
		final String resourceName;

		resourceName = applet.getClass().getName()+".class";

		URL objectUrl = null;
		URL codeBase  = null;
		URL docBase   = null;

		// Now get the URL correponding to the resource name.
	ClassLoader cls = null;
	try {
		cls = ClassLoader.getSystemClassLoader();
		} catch (SecurityException ex) {
		// We're not allowed to access the system class loader.
		// Drop through.
	}

	final ClassLoader cloader = cls;
	objectUrl = (URL)
	java.security.AccessController.doPrivileged
	(new java.security.PrivilegedAction() {
		public Object run() {
		if (cloader == null)
			return ClassLoader.getSystemResource
						(resourceName);
		else
			return cloader.getResource(resourceName);
		}
	});
		// If we found a URL, we try to locate the docbase by taking
		// of the final path name component, and the code base by taking
		// of the complete resourceName.
		// So if we had a resourceName of "a/b/c.class" and we got an
		// objectURL of "file://bert/classes/a/b/c.class" then we would
		// want to set the codebase to "file://bert/classes/" and the
		// docbase to "file://bert/classes/a/b/"

		if (objectUrl != null) {
		String s = objectUrl.toExternalForm();

		if (s.endsWith(resourceName)) {
			int ix   = s.length() - resourceName.length();
			try {
				codeBase = new URL(s.substring(0,ix));
				} catch (java.net.MalformedURLException ex) {
			}
			docBase  = codeBase;

			ix = s.lastIndexOf('/');

			if (ix >= 0) {
				try {
					docBase = new URL(s.substring(0,ix+1));
					} catch (java.net.MalformedURLException ex) {
				}				
			}
		}
		}

		// Setup a default context and stub.
		BeansAppletContext context = new BeansAppletContext(applet);

		AppletStub stubby = (AppletStub)new BeansAppletStub(applet, context, codeBase, docBase);
		applet.setStub(stubby);
	} 

}


// Ticker thread that updates the game state and refreshes the UI
// 
class GameClock extends Thread
{

	PacMondrian      m_pacMan;
   
	public GameClock(PacMondrian pacMan)
   {
		m_pacMan = pacMan;
   }

	public void run()
   {
      while (Thread.currentThread () == m_pacMan.m_ticker)
      {
         try {
            this.sleep (m_pacMan.m_delay);
            
         } catch (InterruptedException e) {
            break;
         }
         
         m_pacMan.tick ();
         m_pacMan.requestFocus ();
      }
	}
	//{{DECLARE_CONTROLS
	//}}
}

// Key event handlers
class KeyControls extends KeyAdapter
{
   PacMondrian   m_pacMan;
   
   KeyControls (PacMondrian pacMan)
   {
      super ();
      m_pacMan = pacMan;
   }
   
   public void keyPressed(KeyEvent event)
   {
      switch (event.getKeyCode()) 
      {
         
      case KeyEvent.VK_LEFT:
      	   m_pacMan.m_gameModel.m_player.m_requestedDirection = Sprite.LEFT;
            break;
      
      case KeyEvent.VK_RIGHT:
			   m_pacMan.m_gameModel.m_player.m_requestedDirection = Sprite.RIGHT;
            break;
      
      case KeyEvent.VK_UP:
            m_pacMan.m_gameModel.m_player.m_requestedDirection = Sprite.UP;
			   break;
      
      case KeyEvent.VK_DOWN:
				m_pacMan.m_gameModel.m_player.m_requestedDirection = Sprite.DOWN;
            break;
      
       case KeyEvent.VK_1:
            if (m_pacMan.m_soundMgr.allSoundsLoaded() && (m_pacMan.m_gameModel.m_state == Model.STATE_INTRO || m_pacMan.m_gameModel.m_player.m_demoMode == true))
            {
                m_pacMan.m_soundMgr.stop();
   	        m_pacMan.m_gameModel.m_bPlayStartClip = true;
                m_pacMan.m_soundMgr.startSoundFinished = false;
                m_pacMan.m_gameModel.m_bStartClipPlayed = false;
                m_pacMan.m_gameModel.m_player.m_demoMode = false;
                m_pacMan.m_gameModel.m_state = Model.STATE_NEWGAME;
                m_pacMan.m_gameUI.m_bDrawPaused = false;
            }
            break;

/**            
      case KeyEvent.VK_P:
            if (m_pacMan.m_gameModel.m_state == Model.STATE_GAMEOVER)
               break;
            
            if (m_pacMan.m_gameModel.m_state == Model.STATE_PAUSED)
            {
               m_pacMan.m_gameModel.m_state = m_pacMan.m_gameModel.m_pausedState;
               m_pacMan.m_gameUI.m_bDrawPaused = false;
               m_pacMan.m_gameUI.m_bRedrawAll = true;
               
            } else {
               m_pacMan.m_gameModel.m_pausedState = m_pacMan.m_gameModel.m_state;
               m_pacMan.m_gameModel.m_state = Model.STATE_PAUSED;
            }
            break;

**/

/**            
      case KeyEvent.VK_A:
            m_pacMan.m_gameModel.m_state = Model.STATE_ABOUT;
            m_pacMan.m_gameModel.m_nTicks2AboutShow = 0;
            break;
**/
            
      case KeyEvent.VK_S:
            m_pacMan.m_soundMgr.m_bEnabled = !m_pacMan.m_soundMgr.m_bEnabled;
            if (m_pacMan.m_soundMgr.m_bEnabled == false)
               m_pacMan.m_soundMgr.stop ();
            m_pacMan.m_bottomCanvas.repaint ();
            break;
             
      case KeyEvent.VK_I:
            m_pacMan.toggleGhostAI ();
            break;
      
/**      case KeyEvent.VK_D:
            if (m_pacMan.m_gameModel.m_player.m_demoMode == true) {
                m_pacMan.m_gameModel.m_player.m_demoMode = false;
            } else {
                m_pacMan.m_gameModel.m_player.m_demoMode = true;
            }
            break;
**/
//      default:
//				System.out.println("Hello World!");
      }   
   }
	//{{DECLARE_CONTROLS
	//}}
}

/**
 * Package private support class.  This provides a default AppletContext
 * for beans which are applets.
 */

class BeansAppletContext implements AppletContext {
	Applet target;
	java.util.Hashtable imageCache = new java.util.Hashtable();

	BeansAppletContext(Applet target) {
		this.target = target;
	}

	public AudioClip getAudioClip(URL url) {
	// We don't currently support audio clips in the Beans.instantiate
	// applet context, unless by some luck there exists a URL content
	// class that can generate an AudioClip from the audio URL.
	try {
		return (AudioClip) url.getContent();
	} catch (Exception ex) {
		return null;
	}
	}

	public synchronized Image getImage(URL url) {
	Object o = imageCache.get(url);
	if (o != null) {
		return (Image)o;
	}
	try {
		o = url.getContent();
		if (o == null) {
		return null;
		}
		if (o instanceof Image) {
		imageCache.put(url, o);
		return (Image) o;
		}
		// Otherwise it must be an ImageProducer.
		Image img = target.createImage((java.awt.image.ImageProducer)o);
		imageCache.put(url, img);
		return img;

	} catch (Exception ex) {
		return null;
	}
	}

	public Applet getApplet(String name) {
	return null;
	}

	public java.util.Enumeration getApplets() {
	java.util.Vector applets = new java.util.Vector();
	applets.addElement(target);
	return applets.elements();
	}

	public void showDocument(URL url) {
	// We do nothing.
	}

	public void showDocument(URL url, String target) {
	// We do nothing.
	}

	public void showStatus(String status) {
	// We do nothing.
	}

	public void setStream(String key, InputStream stream)throws IOException{
	// We do nothing.
	}

	public InputStream getStream(String key){
	// We do nothing.
	return null;
	}

	public java.util.Iterator getStreamKeys(){
	// We do nothing.
	return null;
	}
}

/**
 * Package private support class.  This provides an AppletStub
 * for beans which are applets.
 */
class BeansAppletStub implements AppletStub {
	transient boolean active;
	transient Applet target;
	transient AppletContext context;
	transient URL codeBase;
	transient URL docBase;

	BeansAppletStub(Applet target,
		AppletContext context, URL codeBase,
				URL docBase) {
		this.target = target;
	this.context = context;
	this.codeBase = codeBase;
	this.docBase = docBase;
	}

	public boolean isActive() {
	return active;
	}

	public URL getDocumentBase() {
	// use the root directory of the applet's class-loader
	return docBase;
	}

	public URL getCodeBase() {
	// use the directory where we found the class or serialized object.
	return codeBase;
	}

	public String getParameter(String name) {
	return null;
	}

	public AppletContext getAppletContext() {
	return context;
	}

	public void appletResize(int width, int height) {
	// we do nothing.
	}
}

