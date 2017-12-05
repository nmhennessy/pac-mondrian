import java.awt.*;

class MsPacMan extends PacMan
{
    Image msPacManSprites[][];
    MediaTracker tracker;
    
    MsPacMan (Model gameModel, byte type, int startX, int startY, boolean bMiddleX)
    {
        super (gameModel, type, startX, startY, bMiddleX);
        getImages();
    }
    
    MsPacMan (Model gameModel, byte type, int startX, int startY, boolean bMiddleX, int score)
    {
        super (gameModel, type, startX, startY, bMiddleX, score);
        getImages();
    }
    
    private void getImages()
    {
        msPacManSprites = new Image[4][3];
        tracker = new MediaTracker(m_gameModel.m_pacMan);
        msPacManSprites[0][0] = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase(), "mspac_open.gif");
        tracker.addImage(msPacManSprites[0][0], 0);
        msPacManSprites[0][1] = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase(), "mspac_wide.gif");
        tracker.addImage(msPacManSprites[0][1], 0);
        msPacManSprites[0][2] = m_gameModel.m_pacMan.getImage (m_gameModel.m_pacMan.getCodeBase(), "mspac_close.gif");
        tracker.addImage(msPacManSprites[0][2], 0);
        System.out.println("Images gotten");
    }

   public void draw (UI gameUI, Graphics g2)
   {
      if (!m_bVisible)
         return;
            
      int pacManDiameter = gameUI.CELL_LENGTH + gameUI.WALL1 + gameUI.WALL1;           
      double pacManX = gameUI.m_gridInset + m_locX * gameUI.CELL_LENGTH - pacManDiameter / 2.0;
      double pacManY = gameUI.m_gridInset + m_locY * gameUI.CELL_LENGTH - pacManDiameter / 2.0;
      double deltaPixelX = 0;
      double deltaPixelY = 0;
      int degreeMouth = 0;
           
      pacManX += gameUI.CELL_LENGTH / 2.0;
      pacManY += gameUI.CELL_LENGTH / 2.0;
      
      if (m_deltaLocX != 0)
         deltaPixelX = m_deltaLocX * (gameUI.CELL_LENGTH / (m_deltaMax * 2.0 - 1));
      else if (m_deltaLocY != 0)
         deltaPixelY = m_deltaLocY * (gameUI.CELL_LENGTH / (m_deltaMax * 2.0 - 1));
      
      if (m_direction != STILL && !m_bPaused)
      {
         if (m_mouthChomping)
            m_mouthDegree -= 20;
         else
            m_mouthDegree += 20;
         
         if (m_mouthDegree <= 0 || m_mouthDegree >= MAX_MOUTH_DEGREE)
            m_mouthChomping = !m_mouthChomping;
      }
         
      switch (m_direction)
      {
         case Sprite.UP:
            m_degreeRotation = 90;
            break;
         case Sprite.RIGHT:
            m_degreeRotation = 0;
            break;
         case Sprite.LEFT:
            m_degreeRotation = 180;
            break;
         case Sprite.DOWN:
            m_degreeRotation = 270;
            break;
      }
      
//      g2.setColor (Color.yellow);
g2.setColor (Color.black);

      // Draw Pacman Chomping
      if (!m_bDrawDead)
      {
         g2.fillArc ((int)(pacManX + deltaPixelX), (int)(pacManY + deltaPixelY), pacManDiameter, pacManDiameter, m_degreeRotation + m_mouthDegree, 200);
         g2.fillArc ((int)(pacManX + deltaPixelX), (int)(pacManY + deltaPixelY), pacManDiameter, pacManDiameter, m_degreeRotation - m_mouthDegree, -200);
         g2.drawImage(msPacManSprites[0][0], (int)pacManX, (int)pacManY, gameUI);
      } else {
         // Draw Pacman dying
         if (m_rotationDying > 450)
         {
            m_rotationDying = 450;
            m_mouthDegreeDying += 5;
            m_mouthArcDying -= 5;
            
            if (m_mouthArcDying < 0)
               m_mouthArcDying = 0;
            
         }
         g2.fillArc ((int)(pacManX + deltaPixelX), (int)(pacManY + deltaPixelY), pacManDiameter, pacManDiameter, m_rotationDying + m_mouthDegreeDying, m_mouthArcDying);
         g2.fillArc ((int)(pacManX + deltaPixelX), (int)(pacManY + deltaPixelY), pacManDiameter, pacManDiameter, m_rotationDying - m_mouthDegreeDying, -m_mouthArcDying);
         m_rotationDying += 20;
      }
      
      m_boundingBox.setBounds ((int)(pacManX + deltaPixelX), (int)(pacManY + deltaPixelY), pacManDiameter, pacManDiameter);
      m_boundingBoxFull.setBounds (m_boundingBox);
      m_boundingBox.grow (-pacManDiameter / 5, -pacManDiameter / 5); // Make the bounding box smaller because Pacman is a Circle and not a Square.
      
      // TODO: Draw bounding box for testing
      // g2.setColor (Color.white);
      // g2.drawRect (m_boundingBox.x, m_boundingBox.y, m_boundingBox.width, m_boundingBox.height);
   }
}