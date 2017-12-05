import java.awt.*;
import java.util.*;

// Top Right Canvas which is repainted many times because
// it contains the Score string.
class TopCanvas extends Canvas
{
	Font        m_font;
   Model   m_gameModel;
   
   Image       m_offImage;
   Image       m_imagePacman;
   Graphics    m_offGraphics;
   Dimension   m_offDim;

   MediaTracker tracker;
   ArcadeWriter writer;
   Image       highscore;
   Image       oneup;
   Image       instructions;

	public TopCanvas (Model gameModel, int width, int height, ArcadeWriter writer)
   {
      super ();
      this.writer = writer;
      setSize (width, height);
      m_gameModel = gameModel;
      m_font = new Font ("Lucida", Font.BOLD, 18);
  //    getImages();      
	}

	public void update(Graphics g)
   {
		paint(g);
	}

	public void paint(Graphics g)
   {
      int         y;
      int         x;
		Dimension   dim = getSize ();
      
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
 
      m_offGraphics.setColor (Color.black);
	   m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);

//    if ((tracker.statusID(0, true) & MediaTracker.COMPLETE) != 0) 
//         m_offGraphics.drawImage (m_imagePacman, 0, 0, this);

      // Draw Logo Image
/*      y = 50;
      x = 0; //(m_offDim.width - m_imagePacman.getWidth (this)) / 2;
      m_offGraphics.drawImage (m_imagePacman, x, y, this);
*/      m_offGraphics.setColor (Color.white);
      m_offGraphics.setFont (m_font);
      FontMetrics fm = m_offGraphics.getFontMetrics ();


      // HIGH SCORE
/**
		y = 90 + fm.getAscent() + fm.getDescent();
      x = 0; 
		m_offGraphics.drawString ("HIGH SCORE", x, y);

      y += fm.getAscent () + fm.getDescent (); 
      x = fm.stringWidth ("HIGH SCORE") - fm.stringWidth (Integer.toString (m_gameModel.m_highScore));
      m_offGraphics.drawString (Integer.toString (m_gameModel.m_highScore), x, y);
**/
      x = 0 ;
      y = 10;
//      writer.writeText ("       1up     high score     2up", x, y, m_offGraphics, this);
	  writer.writeText ("               high score        ", x, y, m_offGraphics, this);
      y += 22;
      x = 368;  //width of highscore - width of number
      writer.writeScore(m_gameModel.m_highScore, x, y, m_offGraphics, this);
      y += 30;
      x = 154 - 48; //width of highscore - width of oneup
      writer.writeText ("1up", x, y, m_offGraphics, this);
      x = 160; //width of highscore - width of number
      y = 32;
      writer.writeScore(m_gameModel.m_player.m_score, x, y, m_offGraphics, this);

/**      
      // SCORE
      y += 10 + fm.getAscent() + fm.getDescent(); 
      x = fm.stringWidth ("HIGH SCORE") - fm.stringWidth ("SCORE");
      m_offGraphics.drawString ("SCORE", x, y);
      
      y += fm.getAscent() + fm.getDescent(); 
      x = fm.stringWidth ("HIGH SCORE") - fm.stringWidth (Integer.toString (m_gameModel.m_player.m_score));
      m_offGraphics.drawString (Integer.toString (m_gameModel.m_player.m_score), x, y);
**/

//    }
      g.drawImage(m_offImage, 0, 0, this);

	}

    private void getImages()
    {
        tracker = new MediaTracker(this);
        
        
        highscore = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase(), "mspac_wide.gif");
        tracker.addImage(highscore, 0);
	oneup = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase(), "mspac_ball.gif");
        tracker.addImage(oneup, 0);
        m_imagePacman = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase (), "mspac_open.gif");
        tracker.addImage(m_imagePacman, 0);	
    }
	

}



// Bottom right canvas that is repainted infrequently.
// It contains the number of lives, level and instructions
class BottomCanvas extends Canvas
{
    Font        m_font;
   Model   m_gameModel;
   PacMondrian      m_pacMan;
   ArcadeWriter writer;
   Image       m_offImage;
   Graphics    m_offGraphics;
   Dimension   m_offDim;

    public BottomCanvas (PacMondrian pacMan, Model gameModel, int width, int height, ArcadeWriter writer)
    {
      super ();
      this.writer = writer;
      setSize (width, height);
		m_gameModel = gameModel;
      m_pacMan = pacMan;
      m_font = new Font ("Lucida", Font.BOLD, 18);
    }

	public void update(Graphics g)
   {
		paint(g);
	}

	public void paint(Graphics g)
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
      
      double pacManDiameter = 20;
      int x = 0;
      int y = 0;

     
      m_offGraphics.setColor (Color.black);
	   m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);

      m_offGraphics.setFont(m_font);
		FontMetrics fm = m_offGraphics.getFontMetrics();

      m_offGraphics.setColor (Color.yellow);
		for (int count = 0; count < m_gameModel.m_nLives; count++)
      {
         m_offGraphics.fillArc (x, y, (int)pacManDiameter, (int)pacManDiameter, 45, 200);
         m_offGraphics.fillArc (x, y, (int)pacManDiameter, (int)pacManDiameter, -45, -200);
         x += pacManDiameter * 1.5;  
      }
/**
      //Credits

      x = 0;
      y = 0;
      
      writer.writeText ("               credits", x, y, m_offGraphics, this);

      x = 368;  //width of highscore

      writer.writeScore(m_pacMan.credits, x, y, m_offGraphics, this);
**/

/**
      y += 2 * (int)pacManDiameter + 40;
      x = 0;

      writer.writeText ("press 1 to play", x, y, m_offGraphics, this);
      y += 30;
      writer.writeText ("arrows control", x, y, m_offGraphics, this);
      y += 22;
      writer.writeText ("   pac mondrian", x, y, m_offGraphics, this);
      y += 30;
      writer.writeText ("bonus at 10000", x, y, m_offGraphics, this);
      y += 50;
      if (m_pacMan.m_soundMgr.errorSound) {
	writer.writeText ("sound unavailable", x, y, m_offGraphics, this);
      } else if (m_pacMan.m_soundMgr != null && m_pacMan.m_soundMgr.m_bEnabled && !m_pacMan.m_soundMgr.errorSound) {
	writer.writeText ("s turns sound off", x, y, m_offGraphics, this);
      } else {
	writer.writeText ("s turns sound on", x, y, m_offGraphics, this);
      }
      y += 30;
      writer.writeText ("p pauses game", x, y, m_offGraphics, this);
      y += 30;
//      writer.writeText ("click on game if it", x, y, m_offGraphics, this);
      y += 22;
//      writer.writeText ("   does not respond", x, y, m_offGraphics, this);
      y += 99;
      writer.writeText ("prize budget", x, y, m_offGraphics, this);
      y += 22;
      writer.writeText ("   for boys 2002", x, y, m_offGraphics, this);
**/      
/**      
      m_offGraphics.setColor (Color.white);
      y += 2 * (int)pacManDiameter + fm.getAscent() + fm.getDescent();
      x = 0;
      m_offGraphics.drawString ("Use Arrow Keys", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'P\' to Pause", x, y);
            
      y += fm.getAscent() + fm.getDescent();
      if (m_pacMan.m_soundMgr != null && m_pacMan.m_soundMgr.m_bEnabled)
         m_offGraphics.drawString ("\'S\' for No Sound", x, y);
      else
         m_offGraphics.drawString ("\'S\' for Sound", x, y);
   
      if (m_pacMan.m_soundMgr.allSoundsLoaded())
      {
          y += fm.getAscent() + fm.getDescent();
          m_offGraphics.drawString ("\'N\' for New Game", x, y);
      }
**/
      //y += fm.getAscent() + fm.getDescent();
      //m_offGraphics.drawString ("\'I\'  for Insane AI", x, y);
      
      // buffer to front
		g.drawImage (m_offImage, 0, 0, this);
	}
}