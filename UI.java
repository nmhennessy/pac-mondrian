import java.awt.*;
import java.util.*;

// UI represents the View in an MVC model.  It consists
// of a single Canvas where the play field, intro, and about is
// painted into.  
public class UI extends Canvas
{
   int         CELL_LENGTH;
   
   // Cache the various offset to be used
   // when painting the walls for each cell in the maze
   int         WALL0;            // W0   W1 W2 W3 W4  W5  W6
   int         WALL1;            //  |    |  |  |  |  |    |
   int         WALL2;            
   int         WALL3;            // PAL_BEND is a arc through W2-W3-W4
   int         WALL4;            // PAL_EDGE is a solid line through W0-W1 or W5-W6
   int         WALL5;            // PAL_LINE is a solid line through W2-W4
   int         WALL6;
   
   Hashtable   m_redrawHash;    // Contains grid cells that always need to be redrawn, 
                                // even in update.  These are here for special cases like
                                // Powerup pills which are large enough such that passing 
                                // Ghosts or Pacman could clip them.
   Model   m_gameModel;
   PacMondrian      m_pacMan;
   
   // Double buffer members
   Image       m_offImage;
   Graphics    m_offGraphics;
   Dimension   m_offDim;
   
   Color       m_wallColor;         // Wall color, changes with different mazes
   Color       m_wallAltColor;      // Second wall color, used when flahing level complete
   Font        m_font;              // Font for Ghost points string and About page
   Font        m_readyFont;         // Font for "Ready", "GameOver" and "Paused" strings
   Font        m_readyFontItalic;   // Font for "!!" bang in Ready string
   boolean     m_bRedrawAll = false;   // Set to true to tell Update to Paint
   boolean     m_bDrawReady = false;
   int         m_gridInset;         // Starting painting the maze with this offset
   boolean     m_bFlipWallColor  = false;
   boolean     m_bDrawGameOver   = false;
   boolean     m_bDrawPaused     = false;
   boolean     m_bShowAbout      = false;
   Image       m_imagePacman;       // One and only image of "Pac-Man" banner with litte guy
   Image       m_imageDetroit;
   
   // Variables associated with the intro page
   boolean     m_bShowIntro      = true;
   boolean     m_bShowDetroitTechnoIntro = false;
   boolean     m_bShowTorontoTechnoIntro = false;

   Image       detroitTechnoIntro;
   Image       torontoTechnoIntro;
   Image       bbwIntro;
   MediaTracker tracker;

   ArcadeWriter writer;
   
   UI (PacMondrian pacMan, Model gameModel, int width, int height, ArcadeWriter writer) 
   {
      super ();
      this.writer = writer;
      setSize (width, height);
      m_gameModel = gameModel;
      m_pacMan    = pacMan;
      //CELL_LENGTH = width / (m_gameModel.m_gameSizeX + 1);
      CELL_LENGTH = height / (m_gameModel.m_gameSizeY + 1);
      m_gridInset = CELL_LENGTH / 2;
      WALL0 = 0;
      WALL1 = CELL_LENGTH / 4;
      WALL3 = CELL_LENGTH / 2;
      WALL2 = WALL3 - CELL_LENGTH / 8;
      WALL4 = WALL3 + CELL_LENGTH / 8;
      WALL5 = CELL_LENGTH - WALL1;
      WALL6 = CELL_LENGTH - 1;
      
      
      
      m_redrawHash = new Hashtable ();
      refreshRedrawHash ();
      
      // Create the fonts
      m_font = new Font ("Lucida", Font.BOLD, 14);
      m_readyFont = new Font ("Lucida", Font.BOLD, 20);
      m_readyFontItalic = new Font ("Lucida", Font.BOLD | Font.ITALIC, 20);
      
      tracker = new MediaTracker(pacMan);
      detroitTechnoIntro = m_pacMan.getImage (m_pacMan.getCodeBase(), "detroit_techno_new.jpg");
      tracker.addImage(detroitTechnoIntro, 0);
      bbwIntro = m_pacMan.getImage (m_pacMan.getCodeBase(), "mondrian_broadway.jpg");
      tracker.addImage(bbwIntro, 1);
      torontoTechnoIntro = m_pacMan.getImage (m_pacMan.getCodeBase(), "toronto_techno_new.jpg");
      tracker.addImage(torontoTechnoIntro, 2);
      
   }

   // Refresh the Redraw Hash whenever the board changes.
   void refreshRedrawHash ()
   {
      m_redrawHash.clear ();
      
      // Powerups always need to be redrawn because they may get clipped by the 
      // redraw last location of the player or the ghosts
      for (int x = 0; x < m_gameModel.m_gameSizeX; x++)
      {
         for (int y = 0; y < m_gameModel.m_gameSizeY; y++)
         {
            if ((m_gameModel.m_gameState[x][y] & Model.GS_POWERUP) != 0)
               m_redrawHash.put (Integer.toString (x) + " " + Integer.toString (y), new Point (x, y));
         }
      }
   }
   
   // Sets the clip so that any rendering is done within the grid
   public void setClip (Graphics g)
   {
      g.setClip (m_gridInset, m_gridInset, CELL_LENGTH * m_gameModel.m_gameSizeX, CELL_LENGTH * m_gameModel.m_gameSizeY);
   }
   
   // Update will only redraw the changed game cells..
   public void update (Graphics g)
   {
      if (m_bRedrawAll)
      {
         m_bRedrawAll = false;
         paint (g);
         return;
      }
      
      if (m_bShowIntro)
      {
         updateIntro (g);
         return;
      }

      if (m_bShowDetroitTechnoIntro)
      {
         updateDetroitTechnoIntro (g);
         return;
      }

      if (m_bShowTorontoTechnoIntro)
      {
         updateTorontoTechnoIntro (g);
         return;
      }

      
      // Redraw the gamestate of the location last occupied by each thing
      for (int i =0; i < m_gameModel.m_things.length; i++)
      {
         redrawLastLocation (m_offGraphics, m_gameModel.m_things[i]);
      }
      
      if (m_gameModel.m_player.justEatenPowerup == true) {
        m_gameModel.m_player.justEatenPowerup = false;
        redrawDeadPowerUpCells (m_offGraphics, m_gameModel.m_player);
      }
      
      // Redraw any cells that have been marked as always redraw
      for (Enumeration e = m_redrawHash.elements(); e.hasMoreElements();)
      {
         Point p = ((Point)e.nextElement ()).getLocation ();
         drawGameCell (m_offGraphics, p.x, p.y, false);
      }
      
      // Redraw the Hideout Door
      drawHideoutDoor (m_offGraphics);
      
      // Draw PacMan and Ghosts
      for (int i =0; i < m_gameModel.m_things.length; i++)
      {
         m_gameModel.m_things[i].draw (this, m_offGraphics);
      }
      
      // Draw the Ready string (seen right before playing starts)
      if (m_bDrawReady)
      {
         drawReadyString (m_offGraphics);
      }
      
      // Blitz buffer onto screen
      g.drawImage (m_offImage, 0, 0, this); 
   }
  
   // Draws everything
   public void paint (Graphics g)
   {
      Dimension dim = getSize ();
      
      // Create double buffer if it does not exist or is not
      // the right size
      if (m_offImage == null ||
          m_offDim.width != dim.width ||
          m_offDim.height != dim.height)
      {
         m_offDim = dim;
         m_offImage = createImage (m_offDim.width, m_offDim.height);
         m_offGraphics = m_offImage.getGraphics ();
      }
      
      // Clear everything
      m_offGraphics.setColor (Color.black);
//      m_offGraphics.setColor (Color.black);
	   m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      m_offGraphics.setColor (Color.white);
      
      if (m_bShowIntro)
      {
         paintIntro (g);
//         System.out.println(m_bShowIntro);
         return;
      }
 
       if (m_bShowDetroitTechnoIntro)
      {
         paintDetroitTechnoIntro (g);
         return;
      }

      if (m_bShowTorontoTechnoIntro)
      {
         paintTorontoTechnoIntro (g);
         return;
      }

      if (m_bShowAbout)
      {
         paintAbout (g);
         return;
      }
      
      m_offGraphics.setColor (Color.blue);
      
      // Draw from left to right
      for (int x = 0; x < m_gameModel.m_gameSizeX; x++)
      {
         // Draw the column 
         for (int y = 0; y < m_gameModel.m_gameSizeY; y++)
         {
            drawGameCell (m_offGraphics, x, y, false);
         }
      }
     
      setClip (m_offGraphics);
      
      // Draw Hideout Door
      drawHideoutDoor (m_offGraphics);
      
      // Draw PacMan PacMan and Ghosts
      for (int i =0; i < m_gameModel.m_things.length; i++)
         m_gameModel.m_things[i].draw (this, m_offGraphics);
     
      // Draw the Ready string (seen right before playing starts)
      if (m_bDrawReady)
         drawReadyString (m_offGraphics);
      
      if (m_bDrawGameOver)
         drawGameOverString (m_offGraphics);
      
      if (m_bDrawPaused)
         drawPausedString (m_offGraphics);
      
      // Blitz buffer into actual graphic
      g.drawImage (m_offImage, 0, 0, this); 
   }
   
   // Displays the About page containing the PAC-MAN banner
   public void paintAbout (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
//      m_offGraphics.setColor (Color.black);
m_offGraphics.setColor (Color.yellow);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      if (m_imagePacman == null)
      {
         m_imagePacman = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase (), "pacman.jpg");
      }
      
      // Draw Logo Image
      y = 50;
      x = (m_offDim.width - m_imagePacman.getWidth (this)) / 2;
      m_offGraphics.drawImage (m_imagePacman, x, y, this);
      
      m_offGraphics.setFont (m_font);
      m_offGraphics.setColor (Color.white);
      fm = m_offGraphics.getFontMetrics();
      
      m_offGraphics.setColor (Color.white);
      x = 10;
      y = m_gridInset + 10 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.drawString ("Welcome to PAC-MAN in Java!", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("This game contains the original Pacman maze and all", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("four Ms.Pacman mazes.  You have to finish each maze", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("twice before advancing to the next.", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("After every 10,000 points, you get an extra life.", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("Have fun and watch out for the bonus items!", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("Question, Comments or Bugs?", x , y);
      
      y += fm.getAscent() + fm.getDescent ();
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("Please e-mail me at:", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("benny_chow@hotmail.com", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("Or visit my site at:", x, y);
      
      y += fm.getAscent() + fm.getDescent ();
      m_offGraphics.drawString ("http://www.bennychow.com", x, y);
      
      //Welcome to PAC-MAN in Java!
      //
      //This game contains the original Pacman maze and all four 
      //Ms.Pacman mazes.  You have to finish each maze twice before
      //advancing to the next.
      //After every 10,000 points, you get an extra life.
      //Have fun and watch out for the bonus items!
      //
      //Question, Comments or Bugs?
      //Please e-mail me at:
      //benny_chow@hotmail.com
      //
      //Or visit my site at:
      //http://www.bennychow.com
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
         
   }
   
   public void updateDetroitTechnoIntro (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
      m_offGraphics.setColor (Color.black);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      // Draw Logo Image
       
      if (detroitTechnoIntro != null) {
              y = 50;
              x = (m_offDim.width - detroitTechnoIntro.getWidth (this)) / 2;
	      m_offGraphics.drawImage (detroitTechnoIntro, x, y, this);
	      

      }      
      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();
      width = fm.stringWidth ("Detroit Techno, Music by Anonym, 2005");
      x = (m_offDim.width - width) / 2;
      y = 500;
      m_offGraphics.setColor (Color.white);
      m_offGraphics.drawString ("Detroit Techno, Music by Anonym, 2005", x ,y);
      if (!m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y = 620;
          width = fm.stringWidth ("Please wait while sounds load...");
          x = (m_offDim.width - width) / 2;
//          m_offGraphics.drawString ("Please wait while sounds load...", x, y);
          m_pacMan.m_topCanvas.repaint();
      }
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
/**
      int x, y;
      
      if (m_imagePacman != null)
      {
         // Draw Logo Image
         y = 50;
         x = (m_offDim.width - m_imagePacman.getWidth (this)) / 2;
         m_offGraphics.drawImage (m_imagePacman, x, y, this);
      }
      
      // Redraw the gamestate of Ghosts and Pacman
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         redrawLastLocation (m_offGraphics, m_gameModel.m_ghosts[i]);
      redrawLastLocation (m_offGraphics, m_gameModel.m_player);
      
      // Draw Ghosts and PacMan PacMan
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         m_gameModel.m_ghosts[i].draw (this, m_offGraphics);
      m_gameModel.m_player.draw (this, m_offGraphics);
         
      // Draw the Food and Powerup with 10 and 50 point worth
      x = 250;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH + WALL2, m_gridInset + 22 * CELL_LENGTH + WALL2, WALL1, WALL1);
      if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
         m_offGraphics.setColor (Color.pink);
      else
         m_offGraphics.setColor (Color.black);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH, m_gridInset + 24 * CELL_LENGTH, CELL_LENGTH, CELL_LENGTH);
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
**/
   }

   
   public void updateIntro (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
      m_offGraphics.setColor (Color.black);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      
      if (bbwIntro != null) {
              y = 50;
	      x = (m_offDim.width - bbwIntro.getWidth (this)) / 2;
	      m_offGraphics.drawImage (bbwIntro, x, y, this);
/**	  System.out.println("COMPLETE");
      } else if (tracker.statusID(1, false) == MediaTracker.ERRORED) {
	  System.out.println("ERRORED");
      } else if (tracker.statusID(1, false) == MediaTracker.ABORTED) {
	  System.out.println("ABORTED");
      } else if (tracker.statusID(1, false) == MediaTracker.LOADING) {
	  System.out.println("LOADING");
**/
      }
//      int trackprint = tracker.statusID(1, false);
//      System.out.println(trackprint);
 
      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();
      width = fm.stringWidth ("Piet Mondrian, Broadway Boogie Woogie, 1942-3.");
      x = (m_offDim.width - width) / 2;
      y = 500;
      m_offGraphics.setColor (Color.white);
      m_offGraphics.drawString ("Piet Mondrian, Broadway Boogie Woogie, 1942-3.", x ,y);
      if (!m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y += fm.getAscent() + fm.getDescent();
          width = fm.stringWidth ("Please wait while sounds load...");
          x = (m_offDim.width - width) / 2;
//          m_offGraphics.drawString ("Please wait while sounds load...", x, y);
          m_pacMan.m_topCanvas.repaint();
      }
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
/**
      int x, y;
      
      if (m_imagePacman != null)
      {
         // Draw Logo Image
         y = 50;
         x = (m_offDim.width - m_imagePacman.getWidth (this)) / 2;
         m_offGraphics.drawImage (m_imagePacman, x, y, this);
      }
      
      // Redraw the gamestate of Ghosts and Pacman
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         redrawLastLocation (m_offGraphics, m_gameModel.m_ghosts[i]);
      redrawLastLocation (m_offGraphics, m_gameModel.m_player);
      
      // Draw Ghosts and PacMan PacMan
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         m_gameModel.m_ghosts[i].draw (this, m_offGraphics);
      m_gameModel.m_player.draw (this, m_offGraphics);
         
      // Draw the Food and Powerup with 10 and 50 point worth
      x = 250;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH + WALL2, m_gridInset + 22 * CELL_LENGTH + WALL2, WALL1, WALL1);
      if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
         m_offGraphics.setColor (Color.pink);
      else
         m_offGraphics.setColor (Color.black);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH, m_gridInset + 24 * CELL_LENGTH, CELL_LENGTH, CELL_LENGTH);
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
**/
   }
   
  
   // Displays the intro page containing the PAC-MAN banner
   public void paintIntro (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
      m_offGraphics.setColor (Color.black);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
//     if (m_imagePacman == null)
//      {
//         m_imagePacman = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase (), "mondrian_broadway.jpg");
//      }
      
      if (bbwIntro != null) {
              y = 50;
	      x = (m_offDim.width - bbwIntro.getWidth (this)) / 2;
	      m_offGraphics.drawImage (bbwIntro, x, y, this);
      }
/**	  System.out.println("COMPLETE");
      } else if (tracker.statusID(1, false) == MediaTracker.ERRORED) {
	  System.out.println("ERRORED");
      } else if (tracker.statusID(1, false) == MediaTracker.ABORTED) {
	  System.out.println("ABORTED");
      } else if (tracker.statusID(1, false) == MediaTracker.LOADING) {
	  System.out.println("LOADING");
      }
      int trackprint = tracker.statusID(1, false);
      System.out.println(trackprint);
**/
/**      if (m_imagePacman != null)
      {
      // Draw Logo Image
      y = 50;
      x = (m_offDim.width - m_imagePacman.getWidth (this)) / 2;
      
      m_offGraphics.drawImage (m_imagePacman, x, y, this);
      
      }
**/      
/**
      // Draw Ghosts and PacMan PacMan
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         m_gameModel.m_ghosts[i].draw (this, m_offGraphics);
      m_gameModel.m_player.draw (this, m_offGraphics);
      
      m_offGraphics.setFont (m_readyFont);
      m_offGraphics.setColor (Color.white);
      fm = m_offGraphics.getFontMetrics();
      
      x = 150;
      stringLength = m_gridInset + 18 * CELL_LENGTH - x;
      
      // Draw Ghost intro strings
      y = m_gridInset + 10 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.red);
      m_offGraphics.drawString (padString (m_offGraphics, "\"BLINKY\" ", stringLength),x , y);
      
      y = m_gridInset + 12 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.drawString (padString (m_offGraphics, "\"PINKY\" ", stringLength),x ,y);
      
      y = m_gridInset + 14 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.cyan);
      m_offGraphics.drawString (padString (m_offGraphics, "\"INKY\" ", stringLength), x , y);
      
      y = m_gridInset + 16 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.orange);
      m_offGraphics.drawString (padString (m_offGraphics, "\"CLYDE\" ", stringLength),x , y);
      
      // Draw the Food and Powerup with 10 and 50 point worth
      m_offGraphics.setFont (m_font);
      x = 210;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH + WALL2, m_gridInset + 22 * CELL_LENGTH + WALL2, WALL1, WALL1);
      m_offGraphics.drawString ("= 10 Points", x, m_gridInset + 22 * CELL_LENGTH + fm.getAscent() / 2);
      m_offGraphics.drawString ("= 50 Points", x, m_gridInset + 24 * CELL_LENGTH + fm.getAscent() / 2);
      
      if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
         m_offGraphics.setColor (Color.pink);
      else
         m_offGraphics.setColor (Color.black);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH, m_gridInset + 24 * CELL_LENGTH, CELL_LENGTH, CELL_LENGTH);
**/
      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();
      width = fm.stringWidth ("Piet Mondrian, Broadway Boogie Woogie, 1942-3.");
      x = (m_offDim.width - width) / 2;
      y = 500;
      m_offGraphics.setColor (Color.white);
      m_offGraphics.drawString ("Piet Mondrian, Broadway Boogie Woogie, 1942-3.", x ,y);

      if (!m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y += fm.getAscent() + fm.getDescent();
          width = fm.stringWidth ("Please wait while sounds load...");
          x = (m_offDim.width - width) / 2;
//          m_offGraphics.drawString ("Please wait while sounds load...", x, y);
      }
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
   }
   
   
   // Displays the intro page containing the PAC-MAN banner
   public void paintDetroitTechnoIntro (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
      m_offGraphics.setColor (Color.black);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      if (detroitTechnoIntro != null) {
              y = 50;
              x = (m_offDim.width - detroitTechnoIntro.getWidth (this)) / 2;
	      m_offGraphics.drawImage (detroitTechnoIntro, x, y, this);

      }      
      
/**
      // Draw Ghosts and PacMan PacMan
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         m_gameModel.m_ghosts[i].draw (this, m_offGraphics);
      m_gameModel.m_player.draw (this, m_offGraphics);
      
      m_offGraphics.setFont (m_readyFont);
      m_offGraphics.setColor (Color.white);
      fm = m_offGraphics.getFontMetrics();
      
      x = 150;
      stringLength = m_gridInset + 18 * CELL_LENGTH - x;
      
      // Draw Ghost intro strings
      y = m_gridInset + 10 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.red);
      m_offGraphics.drawString (padString (m_offGraphics, "\"BLINKY\" ", stringLength),x , y);
      
      y = m_gridInset + 12 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.drawString (padString (m_offGraphics, "\"PINKY\" ", stringLength),x ,y);
      
      y = m_gridInset + 14 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.cyan);
      m_offGraphics.drawString (padString (m_offGraphics, "\"INKY\" ", stringLength), x , y);
      
      y = m_gridInset + 16 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.orange);
      m_offGraphics.drawString (padString (m_offGraphics, "\"CLYDE\" ", stringLength),x , y);
      
      // Draw the Food and Powerup with 10 and 50 point worth
      m_offGraphics.setFont (m_font);
      x = 210;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH + WALL2, m_gridInset + 22 * CELL_LENGTH + WALL2, WALL1, WALL1);
      m_offGraphics.drawString ("= 10 Points", x, m_gridInset + 22 * CELL_LENGTH + fm.getAscent() / 2);
      m_offGraphics.drawString ("= 50 Points", x, m_gridInset + 24 * CELL_LENGTH + fm.getAscent() / 2);
      
      if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
         m_offGraphics.setColor (Color.pink);
      else
         m_offGraphics.setColor (Color.black);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH, m_gridInset + 24 * CELL_LENGTH, CELL_LENGTH, CELL_LENGTH);

      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();
      width = fm.stringWidth ("Piet Mondrian, Broadway Boogie Woogie, 1942-3.");
      x = (m_offDim.width - width) / 2;
      y = 500;
      m_offGraphics.setColor (Color.white);
      m_offGraphics.drawString ("Piet Mondrian, Broadway Boogie Woogie, 1942-3.", x ,y);
**/

      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();

      if (!m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y = 620;
          width = fm.stringWidth ("Please wait while sounds load...");
          x = (m_offDim.width - width) / 2;
//          m_offGraphics.drawString ("Please wait while sounds load...", x, y);
      }
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
   }
   
   
   public void updateTorontoTechnoIntro (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
      m_offGraphics.setColor (Color.black);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      if (torontoTechnoIntro != null) {
              y = 50;
              x = (m_offDim.width - torontoTechnoIntro.getWidth (this)) / 2;
	      m_offGraphics.drawImage (torontoTechnoIntro, x, y, this);

      }      
      
      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();
      width = fm.stringWidth ("Toronto Techno, Music by Algorithm, 2006");
      x = (m_offDim.width - width) / 2;
      y = 500;
      m_offGraphics.setColor (Color.white);
      m_offGraphics.drawString ("Toronto Techno, Music by Algorithm, 2006", x ,y);
      if (!m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y += fm.getAscent() + fm.getDescent();
          width = fm.stringWidth ("Please wait while sounds load...");
          x = (m_offDim.width - width) / 2;
//          m_offGraphics.drawString ("Please wait while sounds load...", x, y);
          m_pacMan.m_topCanvas.repaint();
      }
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
/**
      int x, y;
      
      if (m_imagePacman != null)
      {
         // Draw Logo Image
         y = 50;
         x = (m_offDim.width - m_imagePacman.getWidth (this)) / 2;
         m_offGraphics.drawImage (m_imagePacman, x, y, this);
      }
      
      // Redraw the gamestate of Ghosts and Pacman
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         redrawLastLocation (m_offGraphics, m_gameModel.m_ghosts[i]);
      redrawLastLocation (m_offGraphics, m_gameModel.m_player);
      
      // Draw Ghosts and PacMan PacMan
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         m_gameModel.m_ghosts[i].draw (this, m_offGraphics);
      m_gameModel.m_player.draw (this, m_offGraphics);
         
      // Draw the Food and Powerup with 10 and 50 point worth
      x = 250;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH + WALL2, m_gridInset + 22 * CELL_LENGTH + WALL2, WALL1, WALL1);
      if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
         m_offGraphics.setColor (Color.pink);
      else
         m_offGraphics.setColor (Color.black);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH, m_gridInset + 24 * CELL_LENGTH, CELL_LENGTH, CELL_LENGTH);
      
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
**/
   }
   
  
   // Displays the intro page containing the PAC-MAN banner
   public void paintTorontoTechnoIntro (Graphics g)
   {
      int         x             = 0;
      int         y             = 0;
      int         width         = 0;
      int         stringLength  = 0;
      FontMetrics fm;
      
      m_offGraphics.setColor (Color.black);
      m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);
      if (torontoTechnoIntro != null) {
              y = 50;
              x = (m_offDim.width - torontoTechnoIntro.getWidth (this)) / 2;
	      m_offGraphics.drawImage (torontoTechnoIntro, x, y, this);

      }      
      
/**
      // Draw Ghosts and PacMan PacMan
      for (int i =0; i < m_gameModel.m_ghosts.length; i++)
         m_gameModel.m_ghosts[i].draw (this, m_offGraphics);
      m_gameModel.m_player.draw (this, m_offGraphics);
      
      m_offGraphics.setFont (m_readyFont);
      m_offGraphics.setColor (Color.white);
      fm = m_offGraphics.getFontMetrics();
      
      x = 150;
      stringLength = m_gridInset + 18 * CELL_LENGTH - x;
      
      // Draw Ghost intro strings
      y = m_gridInset + 10 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.red);
      m_offGraphics.drawString (padString (m_offGraphics, "\"BLINKY\" ", stringLength),x , y);
      
      y = m_gridInset + 12 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.drawString (padString (m_offGraphics, "\"PINKY\" ", stringLength),x ,y);
      
      y = m_gridInset + 14 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.cyan);
      m_offGraphics.drawString (padString (m_offGraphics, "\"INKY\" ", stringLength), x , y);
      
      y = m_gridInset + 16 * CELL_LENGTH + CELL_LENGTH / 2 + fm.getAscent() / 2;
      m_offGraphics.setColor (Color.orange);
      m_offGraphics.drawString (padString (m_offGraphics, "\"CLYDE\" ", stringLength),x , y);
      
      // Draw the Food and Powerup with 10 and 50 point worth
      m_offGraphics.setFont (m_font);
      x = 210;
      m_offGraphics.setColor (Color.pink);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH + WALL2, m_gridInset + 22 * CELL_LENGTH + WALL2, WALL1, WALL1);
      m_offGraphics.drawString ("= 10 Points", x, m_gridInset + 22 * CELL_LENGTH + fm.getAscent() / 2);
      m_offGraphics.drawString ("= 50 Points", x, m_gridInset + 24 * CELL_LENGTH + fm.getAscent() / 2);
      
      if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
         m_offGraphics.setColor (Color.pink);
      else
         m_offGraphics.setColor (Color.black);
      m_offGraphics.fillOval (m_gridInset + 12 * CELL_LENGTH, m_gridInset + 24 * CELL_LENGTH, CELL_LENGTH, CELL_LENGTH);
**/
      m_offGraphics.setFont (m_readyFont);
      fm = m_offGraphics.getFontMetrics();
      width = fm.stringWidth ("Toronto Techno, Music by Algorithm, 2005");
      x = (m_offDim.width - width) / 2;
      y = 500;
      m_offGraphics.setColor (Color.white);
      m_offGraphics.drawString ("Toronto Techno, Music by Algorithm, 2005", x ,y);

      if (!m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y += fm.getAscent() + fm.getDescent();
          width = fm.stringWidth ("Please wait while sounds load...");
          x = (m_offDim.width - width) / 2;
//          m_offGraphics.drawString ("Please wait while sounds load...", x, y);
      }
      // Blitz buffer to screen
      g.drawImage (m_offImage, 0, 0, this); 
   }

   // This method is used to pad a string to a desired
   // length by appending periods.  Used in coming up
   // with the ghost name strings in the intro.
   public String padString (Graphics g, String stuff, int length)
   {
      FontMetrics fm = g.getFontMetrics();
      while (fm.stringWidth (stuff) < length)
      {
         stuff += ".";
      }
      return stuff;
   }
   
   // draw paused string
   public void drawPausedString (Graphics g)
   {
      int x = 226;
      int y = 190;
      writer.writeText("paused", x, y, g, this);
   }
   
   // draw game over string
   public void drawGameOverString (Graphics g)
   {
    int x = 200;
    int y = 190;
    writer.writeText("game over", x, y, g, this);

   }
   
   // draw begin string
   public void drawReadyString (Graphics g)
   {
    int x = 226;
    int y = 190;
    writer.writeText("ready", x, y, g, this);
   }

   // Draw Ghost Hide out Door.  
   // Treat door as special case in UI and always redraw it.
   public void drawHideoutDoor (Graphics g)
   {
      g.setColor (Color.black);
      g.fillRect (m_gridInset + (m_gameModel.m_doorLocX - 1) * CELL_LENGTH, m_gridInset + m_gameModel.m_doorLocY * CELL_LENGTH + WALL2 + WALL2 / 2, CELL_LENGTH * 4, WALL4 - WALL2);
   }
   
   // Given the X and Y game state location, this method will clear
   // and then draw the contents of the cell.
   // bClearExtra is used to clean up the rendering of Sprites that exceed the
   // boundbox of the each grid cell.
   public void drawGameCell (Graphics g, int x, int y, boolean bClearExtra)
   {

if (x < 0 || x >= this.m_gameModel.m_gameSizeX || y < 0 || y >= this.m_gameModel.m_gameSizeY)
    return;

      int      x1 = m_gridInset + x * CELL_LENGTH;
      int      y1 = m_gridInset + y * CELL_LENGTH;
      int      gameCell = 0;
      Color    wallColor;
      
      // Flipping Wall Color if Pacman has finished the Level
      if (!m_bFlipWallColor)
         wallColor = m_wallColor;
      else
         wallColor = m_wallAltColor;
      
//      g.setColor (Color.black);
g.setColor (Color.yellow);
      if (!bClearExtra)
         g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
      else 
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
//         g.fillRect (x1 - WALL1, y1 - WALL1, CELL_LENGTH + WALL1 * 2, CELL_LENGTH + WALL1 * 2);   
      }
	   
      g.setColor (wallColor);
      try 
      {
          gameCell = m_gameModel.m_gameState [x][y];
      } catch (java.lang.ArrayIndexOutOfBoundsException e)
      {   
          System.out.println(e.toString());
      }

      // Treat the Hideout door as a special case because it will 
      // be re-drawn last.
      if (y == m_gameModel.m_doorLocY &&
          x >= m_gameModel.m_doorLocX - 1&&
          x <= m_gameModel.m_doorLocX + 2)
         return;
      
      // If we are in the intro, don't draw any walls.
      if (m_bShowIntro == true)
         return;
      
g.setColor(Color.white);
      if ((gameCell & Model.PAL_EDGE_TOP) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
//         g.fillRect (x1, y1, CELL_LENGTH, WALL1);

      } 
      if ((gameCell & Model.PAL_EDGE_LEFT) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**         // If the edge has no connector then smooth it out...
         if ((y != 0) &&
             (m_gameModel.m_gameState [x][y-1] & Model.PAL_LINE_HORIZ) != 0)
         {
            g.fillArc (x1 - WALL1, y1 + CELL_LENGTH - WALL1, WALL1 * 2, WALL1 * 2, 0, 90);
            
         } else if ((y != m_gameModel.m_gameSizeY - 1) &&
             (m_gameModel.m_gameState [x][y+1] & Model.PAL_LINE_HORIZ) != 0)
         {
            g.fillArc (x1 - WALL1, y1 - WALL1, WALL1 * 2, WALL1 * 2, 0, -90);
            
         } else {
            g.fillRect (x1, y1, WALL1, CELL_LENGTH);
         }
**/
      }
      if ((gameCell & Model.PAL_EDGE_BOTTOM) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
//         g.fillRect (x1, y1 + CELL_LENGTH - WALL1, CELL_LENGTH, WALL1);
      } 
      if ((gameCell & Model.PAL_EDGE_RIGHT) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**
         // If the edge has no connector then smooth it out...
         if ((y != 0) &&
             (m_gameModel.m_gameState [x][y-1] & Model.PAL_LINE_HORIZ) != 0)
         {
            g.fillArc (x1 + CELL_LENGTH - WALL1, y1 + CELL_LENGTH - WALL1, WALL1 * 2, WALL1 * 2, 180, -90);
            
         } else if ((y != m_gameModel.m_gameSizeY - 1) &&
             (m_gameModel.m_gameState [x][y+1] & Model.PAL_LINE_HORIZ) != 0)
         {
            g.fillArc (x1 + CELL_LENGTH - WALL1, y1 - WALL1, WALL1 * 2, WALL1 * 2, 180, 90);
            
         } else {
            g.fillRect (x1 + CELL_LENGTH - WALL1, y1, WALL1, CELL_LENGTH);
         }
 **/
      }
      if ((gameCell & Model.PAL_LINE_HORIZ) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
//         g.fillRect (x1, y1 + WALL2, CELL_LENGTH, WALL4 - WALL2);
      } 
      if ((gameCell & Model.PAL_LINE_VERT) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
//         g.fillRect (x1 + WALL2, y1, WALL4 - WALL2, CELL_LENGTH);
      }
      
      if ((gameCell & Model.PAL_BEND_TOPLEFT) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**         // Smooth out the corner 
         if ((gameCell & Model.PAL_EDGE_BOTTOM) != 0 && (gameCell & Model.PAL_EDGE_RIGHT) != 0 )
         {
            g.setColor (Color.black);
            g.fillRect (x1, y1 + CELL_LENGTH - WALL1, CELL_LENGTH, WALL1);
            g.fillRect (x1 + CELL_LENGTH - WALL1, y1, WALL1, CELL_LENGTH);
            g.setColor (wallColor);
            g.fillArc (x1 - CELL_LENGTH, y1 - CELL_LENGTH, CELL_LENGTH * 2, CELL_LENGTH * 2, 0, -90);
            g.setColor (Color.black);
            g.fillArc (x1 - CELL_LENGTH + WALL1, y1 - CELL_LENGTH + WALL1, WALL5 * 2, WALL5 *2, 0, -90);
            g.setColor (wallColor);
         }
         g.fillArc (x1 - WALL4, y1 - WALL4, WALL4 * 2, WALL4 * 2, 0, -90);
         g.setColor (Color.black);
         g.fillArc (x1 - WALL2, y1 - WALL2, WALL2 * 2, WALL2 * 2, 0, -90);
         g.setColor (wallColor);
         // Draw corner for adjacent edges
         if ((x != 0) &&
             (y != 0) &&
             (m_gameModel.m_gameState [x-1][y] & Model.PAL_EDGE_TOP) != 0 &&
             (m_gameModel.m_gameState [x][y-1] & Model.PAL_EDGE_LEFT) != 0)
         {
            g.fillArc (x1 - WALL1, y1 - WALL1, WALL1 * 2, WALL1 * 2, 0, -90);
         }
         // Draw corner for edge against wall
         if ((x == 0) &&
             (y != 0) &&
             (m_gameModel.m_gameState [x][y-1] & Model.PAL_EDGE_LEFT) != 0)
         {
            g.fillArc (x1 - WALL1, y1 - WALL1, WALL1 * 2, WALL1 * 2, 0, -90);
         }
**/             
      } 
      if ((gameCell & Model.PAL_BEND_BOTTOMLEFT) != 0)
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**         // Smooth out the corner 
         if ((gameCell & Model.PAL_EDGE_TOP) != 0 && (gameCell & Model.PAL_EDGE_RIGHT) != 0 )
         {
            g.setColor (Color.black);
            g.fillRect (x1, y1, CELL_LENGTH, WALL1);
            g.fillRect (x1 + CELL_LENGTH - WALL1, y1, WALL1, CELL_LENGTH);
            g.setColor (wallColor);
            g.fillArc (x1 - CELL_LENGTH, y1, CELL_LENGTH * 2, CELL_LENGTH * 2, 0, 90);
            g.setColor (Color.black);
            g.fillArc (x1 - CELL_LENGTH + WALL1, y1 + WALL1, WALL5 * 2, WALL5 *2, 0, 90);
            g.setColor (wallColor);
         }
         g.fillArc (x1 - WALL4, y1 + CELL_LENGTH - WALL4, WALL4 * 2, WALL4 * 2, 0, 90);
         g.setColor (Color.black);
         g.fillArc (x1 - WALL2, y1 + CELL_LENGTH - WALL2, WALL2 * 2, WALL2 * 2, 0, 90);
         g.setColor (wallColor);
         // Draw corner for adjacent edges
         if ((x != 0) &&
             (y != m_gameModel.m_gameSizeY - 1) &&
             (m_gameModel.m_gameState [x-1][y] & Model.PAL_EDGE_BOTTOM) != 0 &&
             (m_gameModel.m_gameState [x][y+1] & Model.PAL_EDGE_LEFT) != 0)
         {
            g.fillArc (x1 - WALL1, y1 + CELL_LENGTH - WALL1, WALL1 * 2, WALL1 * 2, 0, 90);
         }
         // Draw corner for edge against wall
         if ((x == 0) &&
             (y != m_gameModel.m_gameSizeY - 1) &&
             (m_gameModel.m_gameState [x][y+1] & Model.PAL_EDGE_LEFT) != 0)
         {
            g.fillArc (x1 - WALL1, y1 + CELL_LENGTH - WALL1, WALL1 * 2, WALL1 * 2, 0, 90);
         }
**/
      }
      if ((gameCell & Model.PAL_BEND_BOTTOMRIGHT) != 0) // Door is always on top
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**         // Smooth out the corner 
         if ((gameCell & Model.PAL_EDGE_TOP) != 0 && (gameCell & Model.PAL_EDGE_LEFT) != 0 )
         {
            g.setColor (Color.black);
            g.fillRect (x1, y1, CELL_LENGTH, WALL1);
            g.fillRect (x1, y1, WALL1, CELL_LENGTH);
            g.setColor (wallColor);
            g.fillArc (x1, y1, CELL_LENGTH * 2, CELL_LENGTH * 2, 90, 90);
            g.setColor (Color.black);
            g.fillArc (x1 + WALL1, y1 + WALL1, WALL5 * 2, WALL5 *2, 90, 90);
            g.setColor (wallColor);
         }
         g.fillArc (x1 + CELL_LENGTH - WALL4, y1 + CELL_LENGTH - WALL4, WALL4 * 2, WALL4 * 2, -180, -90);
         g.setColor (Color.black);
         g.fillArc (x1 + CELL_LENGTH - WALL2, y1 + CELL_LENGTH - WALL2, WALL2 * 2, WALL2 * 2, -180, -90);
         g.setColor (wallColor);
         // Draw corner for adjacent edges
         if ((x != m_gameModel.m_gameSizeX - 1) &&
             (y != m_gameModel.m_gameSizeY - 1) &&
             (m_gameModel.m_gameState [x][y+1] & Model.PAL_EDGE_RIGHT) != 0 &&
             (m_gameModel.m_gameState [x+1][y] & Model.PAL_EDGE_BOTTOM) != 0)
         {
            g.fillArc (x1 + CELL_LENGTH - WALL1, y1 + CELL_LENGTH - WALL1, WALL1 * 2, WALL1 * 2, 90, 90);
         }
         // Draw corner for edge against wall
         if ((x == m_gameModel.m_gameSizeX - 1) &&
             (y != m_gameModel.m_gameSizeY - 1) &&
             (m_gameModel.m_gameState [x][y+1] & Model.PAL_EDGE_RIGHT) != 0)
         {
            g.fillArc (x1 + CELL_LENGTH - WALL1, y1 + CELL_LENGTH - WALL1, WALL1 * 2, WALL1 * 2, 90, 90);
         }
**/
      }
      if ((gameCell & Model.PAL_BEND_TOPRIGHT) != 0) // Door is always on bottom
      {
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**         // Smooth out the corner 
         if ((gameCell & Model.PAL_EDGE_BOTTOM) != 0 && (gameCell & Model.PAL_EDGE_LEFT) != 0 )
         {
            g.setColor (Color.black);
            g.fillRect (x1, y1 + CELL_LENGTH - WALL1, CELL_LENGTH, WALL1);
            g.fillRect (x1, y1, WALL1, CELL_LENGTH);
            g.setColor (wallColor);
            g.fillArc (x1, y1 - CELL_LENGTH, CELL_LENGTH * 2, CELL_LENGTH * 2, 180, 90);
            g.setColor (Color.black);
            g.fillArc (x1 + WALL1, y1 - CELL_LENGTH + WALL1, WALL5 * 2, WALL5 *2, 180, 90);
            g.setColor (wallColor);
         }
         g.fillArc (x1 + CELL_LENGTH - WALL4, y1 - WALL4, WALL4 * 2, WALL4 * 2, -180, 90);
         g.setColor (Color.black);
         g.fillArc (x1 + CELL_LENGTH - WALL2, y1 - WALL2, WALL2 * 2, WALL2 * 2, -180, 90);
         g.setColor (wallColor);
         // Draw corner for adjacent edges
         if ((x != m_gameModel.m_gameSizeX - 1) &&
             (y != 0) &&
             (m_gameModel.m_gameState [x+1][y] & Model.PAL_EDGE_TOP) != 0 &&
             (m_gameModel.m_gameState [x][y-1] & Model.PAL_EDGE_RIGHT) != 0)
         {
            g.fillArc (x1 + CELL_LENGTH - WALL1, y1 - WALL1, WALL1 * 2, WALL1 * 2, 180, 90);
         }
         // Draw corner for edge against wall
         if ((x == m_gameModel.m_gameSizeX - 1) &&
             (y != 0) &&
             (m_gameModel.m_gameState [x][y-1] & Model.PAL_EDGE_RIGHT) != 0)
         {
            g.fillArc (x1 + CELL_LENGTH - WALL1, y1 - WALL1, WALL1 * 2, WALL1 * 2, 180, 90);
         }
**/
      }
      if ((gameCell & Model.PAL_BLUE) != 0)
      {
g.setColor (Color.blue);
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
/**         g.setColor (Color.pink);
         g.fillOval (x1 + WALL2, y1 + WALL2, WALL1, WALL1);
**/
         g.setColor (wallColor);
      }
if ((gameCell & Model.PAL_GREY) != 0)
      {
g.setColor (new Color(185,185,185));
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
g.setColor (wallColor);
      }

if ((gameCell & Model.PAL_RED) != 0)
      {
g.setColor (Color.red);
g.fillRect (x1, y1, CELL_LENGTH, CELL_LENGTH);
g.setColor (wallColor);
      }

/**
      if ((gameCell & Model.GS_POWERUP) != 0)
      {
         if ((m_gameModel.m_pacMan.m_globalTickCount % (1000 / m_pacMan.m_delay)) > (200 / m_pacMan.m_delay))
            g.setColor (Color.pink);
         else
            g.setColor (Color.black);
         
         g.fillOval (x1, y1, CELL_LENGTH, CELL_LENGTH);
         g.setColor (wallColor);
      }
**/

   }
   
   // This method will redraw the cells that thing last occupied.
   // The reason why we have to save the deltaLoc is because
   // the thing may have crossed over to an adjacent cell, moved in the new
   // cell, but it still rendering a part of itself to the previous cell.
   // In this case, m_locationX and Y refer to the new cell, even though
   // the thing is spilling over into the previous cell
   public void redrawLastLocation (Graphics g, Sprite thing)
   {
      if (thing.m_lastLocX < 0 && thing.m_lastLocY < 0)
         return;
      
      if (thing.m_lastDeltaLocX < 0 && thing.m_lastLocX != 0)
         drawGameCell (g, thing.m_lastLocX - 1, thing.m_lastLocY, false);
      else if (thing.m_lastDeltaLocX > 0 && thing.m_lastLocX != m_gameModel.m_gameSizeX - 1)
         drawGameCell (g, thing.m_lastLocX + 1, thing.m_lastLocY, false);
      else if (thing.m_lastDeltaLocY < 0 && thing.m_lastLocY != 0)
         drawGameCell (g, thing.m_lastLocX, thing.m_lastLocY - 1, false);
      else if (thing.m_lastDeltaLocY > 0 && thing.m_lastLocY != m_gameModel.m_gameSizeY - 1)
         drawGameCell (g, thing.m_lastLocX, thing.m_lastLocY + 1, false);
      
      drawGameCell (g, thing.m_lastLocX, thing.m_lastLocY, false);

if (thing.m_lastOccupiedX - 1 >= 0)
    drawGameCell (g, thing.m_lastOccupiedX - 1, thing.m_lastOccupiedY, true);
if (thing.m_lastOccupiedX + 1 < m_gameModel.m_gameSizeX) 
    drawGameCell (g, thing.m_lastOccupiedX + 1, thing.m_lastOccupiedY, true);
if (thing.m_lastOccupiedY - 1 >= 0) 
    drawGameCell (g, thing.m_lastOccupiedX, thing.m_lastOccupiedY - 1, true);
if (thing.m_lastOccupiedY + 1 < m_gameModel.m_gameSizeY) 
    drawGameCell (g, thing.m_lastOccupiedX, thing.m_lastOccupiedY + 1, true);

if (thing.m_locX - 1 >= 0) {
    drawGameCell (g, thing.m_locX - 1, thing.m_locY, true);
    if (thing.m_locY - 1 >= 0)
        drawGameCell(g, thing.m_locX - 1, thing.m_locY - 1, true);
}
if (thing.m_locX + 1 < m_gameModel.m_gameSizeX) {
    drawGameCell (g, thing.m_locX + 1, thing.m_locY, true);
    if (thing.m_locY + 1 < m_gameModel.m_gameSizeY)
        drawGameCell(g, thing.m_locX + 1, thing.m_locY + 1, true);
}
if (thing.m_locY - 1 >= 0) {
    drawGameCell (g, thing.m_locX, thing.m_locY - 1, true);
    if (thing.m_locX + 1 < m_gameModel.m_gameSizeX)
        drawGameCell (g, thing.m_locX + 1, thing.m_locY - 1, true);
}
if (thing.m_locY + 1 < m_gameModel.m_gameSizeY) {
    drawGameCell (g, thing.m_locX, thing.m_locY + 1, true);
    if (thing.m_locX - 1 >= 0)
        drawGameCell (g, thing.m_locX - 1, thing.m_locY + 1, true);
}

if (thing.m_lastSuperWarpOccupiedX != -1)
{
    drawGameCell (g, thing.m_lastSuperWarpOccupiedX, thing.m_lastSuperWarpOccupiedY, true);
    drawGameCell (g, thing.m_lastSuperWarpOccupiedX - 1, thing.m_lastSuperWarpOccupiedY, true);
    drawGameCell (g, thing.m_lastSuperWarpOccupiedX + 1, thing.m_lastSuperWarpOccupiedY, true);
    drawGameCell (g, thing.m_lastSuperWarpOccupiedX, thing.m_lastSuperWarpOccupiedY - 1, true);
    drawGameCell (g, thing.m_lastSuperWarpOccupiedX, thing.m_lastSuperWarpOccupiedY + 1, true);

}
   
   }
   
   public void redrawDeadPowerUpCells (Graphics g, PacMan p)
   {
        while (p.xStack.empty() != true)
        {
            drawGameCell (g, ((Integer) p.xStack.pop()).intValue(), ((Integer) p.yStack.pop()).intValue(), false);
        }    
   }



/**   public void redrawLastLocation (Graphics g, Sprite thing)
   {
        if (thing.m_locX > 0)
            drawGameCell (g, thing.m_locX - 1, thing.m_locY, false);
        if (thing.m_locX < m_gameModel.m_gameSizeX - 1)
            drawGameCell (g, thing.m_locX + 1, thing.m_locY, false);
        if (thing.m_locY < m_gameModel.m_gameSizeY - 1)
            drawGameCell (g, thing.m_locX, thing.m_locY + 1, false);
        if (thing.m_locY > 0)
            drawGameCell (g, thing.m_locX, thing.m_locY - 1, false);
   }
**/
}

