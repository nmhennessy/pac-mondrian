import java.awt.*;


class Ghost extends Sprite
{
   int[]    m_ghostMouthX;    // X points of Ghost's crooked mouth when Pacman powersup
   int[]    m_ghostMouthY;    // Y points of Ghost's crooked mouth when Pacman powersup
   Polygon  m_ghostPolygon;   // Alternate between these two polygons to draw the ghost
   Polygon  m_ghostPolygon2;
   boolean  m_bOtherPolygon = false;
   int      m_lastDirection;
   Color    m_color;
   int      m_nTicks2Exit;       // Ticks before ghost is allowed to exit.
   int      m_nExitMilliSec;     // Milliseconds before exiting.
   int      m_nTicks2Flee = 0;   // How long the Ghost will run from Pacman
   boolean  m_bEaten = false;    // Set to true when Pacman has eaten this ghost
   int      m_ghostDeltaMax = 4; // Should never change
   int      m_eatenPoints;       // Point worth for eaten Ghost
   int      m_nTicks2Popup;      // Ticks to display eaten points
   boolean  m_bEnteringDoor = false;
   
   // Variables to toggle Ghost AI
   boolean  m_bCanFollow         = false;  // Can ghosts follow each other, i.e. Same destination and direction
   boolean  m_bCanPredict        = true;   // Can ghosts predict pacman's destination
   boolean  m_bCanBackTrack      = false;  // Can ghost go back the direction they came
   boolean  m_bCanUseNextBest    = true;   // Can ghost try the next best direction first 25% of the time
   boolean  m_bInsaneAI          = false;   // No holds barred!
      
   Ghost (Model gameModel, byte type, int startX, int startY, boolean bMiddle, Color color, int nExitMilliSec)
   {
      super (gameModel, type, startX, startY, bMiddle);
      m_deltaMax = m_ghostDeltaMax;
      m_destinationX = -1;
      m_destinationY = -1;
      m_color = color;
      m_bInsideRoom = true;
      m_nExitMilliSec = nExitMilliSec;
      m_nTicks2Exit = m_nExitMilliSec / gameModel.m_pacMan.m_delay;
   }
   
   // Overriden to draw Ghosts
   public void draw (UI gameUI, Graphics g2)
   {
      if (!m_bVisible)
         return;
      
      // Ghost Head Diameter is also the Width and Height of the Ghost
      int ghostHeadDiameter = gameUI.CELL_LENGTH + gameUI.WALL1 + gameUI.WALL1;
      int ghostLegHalf = ghostHeadDiameter / 2;
      int ghostLegQuarter = ghostHeadDiameter / 4;
      int ghostLegUnit = ghostLegQuarter / 4;
      int ghostLegHeight = ghostLegQuarter * 3/4;
         
      if (m_ghostPolygon == null) // I.e. Ghosts is not Inited yet
      {  
         int[] xPoints = {0,
                          0,
                          ghostLegUnit,
                          ghostLegQuarter - ghostLegUnit,
                          ghostLegQuarter + ghostLegUnit,
                          ghostLegHalf - ghostLegUnit- ghostLegUnit,
                          ghostLegHalf - ghostLegUnit- ghostLegUnit,
                          ghostLegHalf + ghostLegUnit + ghostLegUnit,
                          ghostLegHalf + ghostLegUnit + ghostLegUnit,
                          ghostLegHalf + ghostLegQuarter - ghostLegUnit,
                          ghostLegHalf + ghostLegQuarter + ghostLegUnit,
                          ghostHeadDiameter - ghostLegUnit,
                          ghostHeadDiameter,
                          ghostHeadDiameter};
      
         int[] yPoints = {ghostHeadDiameter / 2,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter / 2};
         
    
         int[] xPoints2 = {0,
                          0,
                          ghostLegUnit,
                          ghostLegQuarter - ghostLegUnit,
                          ghostLegQuarter,
                          ghostLegHalf - ghostLegUnit - ghostLegUnit,
                          ghostLegHalf + ghostLegUnit + ghostLegUnit,
                          ghostLegHalf + ghostLegQuarter,
                          ghostHeadDiameter - ghostLegQuarter + ghostLegUnit,
                          ghostHeadDiameter - ghostLegUnit,
                          ghostHeadDiameter,
                          ghostHeadDiameter};
      
         int[] yPoints2 = {ghostHeadDiameter / 2,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter,
                          ghostHeadDiameter,
                          ghostHeadDiameter - ghostLegHeight,
                          ghostHeadDiameter / 2};
         
         m_ghostPolygon = new Polygon (xPoints, yPoints, xPoints.length);
         m_ghostPolygon2 = new Polygon (xPoints2, yPoints2, xPoints2.length);
         
         int ghostMouthHalf = ghostHeadDiameter / 2;
         int ghostMouthQuarter = ghostMouthHalf / 2;
         int ghostMouthTeeth = ghostMouthQuarter / 2;
         int ghostMouthY1  = ghostHeadDiameter / 2 + ghostHeadDiameter / 4;
         int ghostMouthY2  = ghostHeadDiameter / 2 + ghostHeadDiameter / 7;
         
                  
         m_ghostMouthX = new int[7];
         m_ghostMouthX[0] = ghostMouthTeeth;
         m_ghostMouthX[1] = ghostMouthQuarter;
         m_ghostMouthX[2] = ghostMouthQuarter + ghostMouthTeeth;
         m_ghostMouthX[3] = ghostMouthHalf;
         m_ghostMouthX[4] = ghostMouthHalf + ghostMouthTeeth;
         m_ghostMouthX[5] = ghostMouthHalf + ghostMouthQuarter;
         m_ghostMouthX[6] = ghostMouthHalf + ghostMouthQuarter + ghostMouthTeeth;
                          
         m_ghostMouthY = new int[7];
         m_ghostMouthY[0] = ghostMouthY1;
         m_ghostMouthY[1] = ghostMouthY2;
         m_ghostMouthY[2] = ghostMouthY1;
         m_ghostMouthY[3] = ghostMouthY2;
         m_ghostMouthY[4] = ghostMouthY1;
         m_ghostMouthY[5] = ghostMouthY2;
         m_ghostMouthY[6] = ghostMouthY1;  
         
      }
      
      Polygon polygon;
      int ghostX = gameUI.m_gridInset + (int)(m_locX * gameUI.CELL_LENGTH - ghostHeadDiameter / 2.0 + gameUI.CELL_LENGTH / 2.0 + m_deltaLocX * (gameUI.CELL_LENGTH / (m_deltaMax * 2.0 - 1)));
      int ghostY = gameUI.m_gridInset + (int)(m_locY * gameUI.CELL_LENGTH - ghostHeadDiameter / 2.0 + gameUI.CELL_LENGTH / 2.0 + m_deltaLocY * (gameUI.CELL_LENGTH / (m_deltaMax * 2.0 - 1)));
      
      // If Pacman just ate this Ghost, draw the point worth of
      // the ghost.
      if (m_nTicks2Popup > 0)
      {
         g2.setColor (Color.cyan);
         g2.setFont (m_gameModel.m_pacMan.m_gameUI.m_font);
         FontMetrics fm = g2.getFontMetrics();
         g2.drawString (Integer.toString (m_eatenPoints), ghostX, ghostY + fm.getAscent());
         m_gameModel.m_pacMan.m_gameUI.m_bRedrawAll = true;
         return;
      }
      
      // Alter the Ghost's color if Pacman ate a Powerup
      if (m_nTicks2Flee == 0)
      {
         g2.setColor (m_color);
         
      } else {
         // Check if the Powerup is almost out for this ghost,
         // if so, flash white.
         if (m_nTicks2Flee < 2000 / m_gameModel.m_pacMan.m_delay && (m_nTicks2Flee % (200 / m_gameModel.m_pacMan.m_delay)) < (100 / m_gameModel.m_pacMan.m_delay))
            g2.setColor (m_color.white);
         else
            g2.setColor (m_color.blue);

         if (m_gameModel.m_stage % 3 == 0 && m_nTicks2Flee < 2000 / m_gameModel.m_pacMan.m_delay && !m_gameModel.m_pacMan.m_soundMgr.GhostChaseTimeRunningOutSoundOn)
         {
	     m_gameModel.m_pacMan.m_soundMgr.stopSound(SoundManager.SOUND_GHOSTBLUE);
             m_gameModel.m_pacMan.m_soundMgr.playSound(SoundManager.SOUND_GHOSTCHASETIMERUNNINGOUT);
             m_gameModel.m_pacMan.m_soundMgr.GhostChaseTimeRunningOutSoundOn = true;
         }


      }
      
      // If the ghost is eaten, then do not draw the body
      if (!m_bEaten)
      {
         g2.fillArc (ghostX, ghostY, ghostHeadDiameter, ghostHeadDiameter, 0, 180);
         if (!m_bOtherPolygon)
         {
            polygon = new Polygon (m_ghostPolygon.xpoints, m_ghostPolygon.ypoints, m_ghostPolygon.npoints);
            polygon.translate (ghostX, ghostY);
         } else {
            polygon = new Polygon (m_ghostPolygon2.xpoints, m_ghostPolygon2.ypoints, m_ghostPolygon2.npoints);
            polygon.translate (ghostX, ghostY);
         }
         if ((m_gameModel.m_pacMan.m_globalTickCount % (m_ghostDeltaMax * 2)) == 0)
            m_bOtherPolygon = !m_bOtherPolygon;
         g2.fillPolygon (polygon);
      }
        
      // Draw Eyes
      double crossEyeDelta = 1; // TODO: Make this relative.
      double ghostEyeWidth = ghostHeadDiameter / 2.7;
      double ghostEyeHeight = ghostHeadDiameter / 2.0;
      
      double ghostEyeX = 0;
      double ghostEyeY = 0;
      
      double ghostEyeDiameter = ghostHeadDiameter / 5.0;
      double ghostEyeBallX = 0;
      double ghostEyeBallY = 0;
      
      if (m_nTicks2Flee > 0 && !m_bEaten)
      {
         crossEyeDelta = 2;
         ghostEyeX = ghostX + ghostHeadDiameter / 4.0 - ghostEyeWidth / 2.0;
         ghostEyeY = ghostY + ghostHeadDiameter / 7.0;
         ghostEyeBallX = ghostEyeX + ghostEyeWidth / 2.0 - ghostEyeDiameter / 2.0;
         ghostEyeBallY = ghostEyeY + ghostEyeHeight / 2.0 - ghostEyeDiameter / 2.0;
    
      } else if (m_direction == STILL)
      {
         // Look right for now
         /*
         ghostEyeX = ghostX + ghostHeadDiameter / 4 - ghostEyeWidth / 2;
         ghostEyeY = ghostY + ghostHeadDiameter / 5;
         ghostEyeBallX = ghostEyeX + ghostEyeWidth / 2 - ghostEyeDiameter / 2;
         ghostEyeBallY = ghostEyeY + ghostEyeHeight / 2 - ghostEyeDiameter / 2;
         */
         ghostEyeX = ghostX + ghostHeadDiameter / 4.0 - ghostEyeWidth / 2.0;
         ghostEyeY = ghostY + ghostHeadDiameter / 5.0;
         ghostEyeBallX = ghostEyeX + ghostEyeWidth - ghostEyeDiameter;
         ghostEyeBallY = ghostEyeY + ghostEyeHeight / 2.0 - ghostEyeDiameter / 2.0;
         
      } else if (m_direction == UP)
      {
         if (!m_bEaten)
            ghostEyeHeight = ghostHeadDiameter / 3.0;
         ghostEyeX = ghostX + ghostHeadDiameter / 4.0 - ghostEyeWidth / 2.0;
         ghostEyeY = ghostY + ghostHeadDiameter / 7.0;
         ghostEyeBallX = ghostEyeX + ghostEyeWidth / 2.0 - ghostEyeDiameter / 2.0;
         ghostEyeBallY = ghostEyeY;
         
      } else if (m_direction == LEFT)
      {
         ghostEyeX = ghostX + ghostHeadDiameter / 4.0 - ghostEyeWidth / 2.0;
         ghostEyeY = ghostY + ghostHeadDiameter / 5.0;
         ghostEyeBallX = ghostEyeX;
         ghostEyeBallY = ghostEyeY + ghostEyeHeight / 2.0 - ghostEyeDiameter / 2.0;
         
      } else if (m_direction == RIGHT)
      {
         ghostEyeX = ghostX + ghostHeadDiameter / 4.0 - ghostEyeWidth / 2.0;
         ghostEyeY = ghostY + ghostHeadDiameter / 5.0;
         ghostEyeBallX = ghostEyeX + ghostEyeWidth - ghostEyeDiameter;
         ghostEyeBallY = ghostEyeY + ghostEyeHeight / 2.0 - ghostEyeDiameter / 2.0;
      } else if (m_direction == DOWN)
      {
         ghostEyeX = ghostX + ghostHeadDiameter / 4.0 - ghostEyeWidth / 2.0;
         ghostEyeY = ghostY + ghostHeadDiameter / 4.0;
         ghostEyeBallX = ghostEyeX + ghostEyeWidth / 2.0 - ghostEyeDiameter / 2.0;
         ghostEyeBallY = ghostEyeY + ghostEyeHeight - ghostEyeDiameter;
      }
      
      // Draw the ghost eyes while it's chasing Pacman
      if (m_nTicks2Flee == 0 && !m_bEaten)
      {
         g2.setColor (Color.white);
         // Left Eye
         g2.fillOval ((int)(ghostEyeX + crossEyeDelta), (int)(ghostEyeY), (int)(ghostEyeWidth), (int)ghostEyeHeight);
         // Right Eye
         g2.fillOval ((int)(ghostEyeX + ghostHeadDiameter / 2.0 - crossEyeDelta), (int)(ghostEyeY), (int)(ghostEyeWidth), (int)ghostEyeHeight);
      
            g2.setColor (Color.blue);
         
         // Left Eye Ball
         g2.fillRoundRect ((int)(ghostEyeBallX + crossEyeDelta), (int)(ghostEyeBallY), (int)(ghostEyeDiameter) , (int)(ghostEyeDiameter), (int)(ghostEyeDiameter), (int)(ghostEyeDiameter));
         // Right Eye Ball  
         g2.fillRoundRect ((int)(ghostEyeBallX + ghostHeadDiameter / 2.0 - crossEyeDelta), (int)(ghostEyeBallY), (int)(ghostEyeDiameter) , (int)(ghostEyeDiameter), (int)(ghostEyeDiameter), (int)(ghostEyeDiameter));
      
      } else if (m_nTicks2Flee > 0 && !m_bEaten)
      {
         // Draw the ghost running away
         g2.setColor (Color.lightGray);
         // Left Eye Ball
         g2.fillRoundRect ((int)(ghostEyeBallX+ crossEyeDelta), (int)(ghostEyeBallY), (int)(ghostEyeDiameter) , (int)(ghostEyeDiameter), (int)(ghostEyeDiameter), (int)(ghostEyeDiameter));
         // Right Eye Ball  
         g2.fillRoundRect ((int)(ghostEyeBallX  + ghostHeadDiameter / 2.0 - crossEyeDelta), (int)(ghostEyeBallY), (int)(ghostEyeDiameter) , (int)(ghostEyeDiameter), (int)(ghostEyeDiameter), (int)(ghostEyeDiameter));
         // Draw Crooked Grin
         for (int i = 0; i < m_ghostMouthX.length - 1; i++)
         {
            g2.drawLine ((int)(ghostX + m_ghostMouthX[i]), (int)(ghostY + m_ghostMouthY[i]), (int)(ghostX + m_ghostMouthX[i+1]), (int)(ghostY + m_ghostMouthY[i+1]));
            g2.drawLine ((int)(ghostX  + m_ghostMouthX[i] - 1), (int)(ghostY + m_ghostMouthY[i]), (int)(ghostX + m_ghostMouthX[i+1] - 1), (int)(ghostY + m_ghostMouthY[i+1]));
         }
         
      } else {
         // Draw the eaten ghost returning to hideout.
         g2.setColor (Color.lightGray);
         // Left Eye
         g2.fillOval ((int)(ghostEyeX + crossEyeDelta), (int)(ghostEyeY), (int)(ghostEyeWidth), (int)ghostEyeHeight);
         // Right Eye
         g2.fillOval ((int)(ghostEyeX + ghostHeadDiameter / 2.0 - crossEyeDelta), (int)(ghostEyeY), (int)(ghostEyeWidth), (int)ghostEyeHeight);
      
         // Left Eye Ball
         g2.setColor (Color.blue);
         g2.fillRoundRect ((int)(ghostEyeBallX + crossEyeDelta), (int)(ghostEyeBallY), (int)(ghostEyeDiameter) , (int)(ghostEyeDiameter), (int)(ghostEyeDiameter), (int)(ghostEyeDiameter));
         // Right Eye Ball  
         g2.fillRoundRect ((int)(ghostEyeBallX + ghostHeadDiameter / 2.0 - crossEyeDelta), (int)(ghostEyeBallY), (int)(ghostEyeDiameter) , (int)(ghostEyeDiameter), (int)(ghostEyeDiameter), (int)(ghostEyeDiameter));
      
      }
      m_boundingBox.setBounds ((int)(ghostX), (int)(ghostY), ghostHeadDiameter, ghostHeadDiameter);
      m_boundingBox.grow (-ghostHeadDiameter / 4, -ghostHeadDiameter / 4);
      //m_boundingBox.setBounds ((int)(ghostX + deltaPixelX), (int)(ghostY + deltaPixelY + ghostHeight / 5), ghostHeight, ghostHeight - ghostHeight / 5);
      
      // TODO: Draw bounding box for testing
      //g2.setColor (Color.white);
      //g2.drawRect (m_boundingBox.x, m_boundingBox.y, m_boundingBox.width, m_boundingBox.height);

   }
   
   // Overriden to update Ghost's directions
   public void tickSprite ()
   {  
      boolean  bBackoff = false;
      // Don't let the ghost go back the way it came.
      byte prevDirection = STILL;
       
      // Count down for how long the Points for eating the Ghost popup
      if (m_nTicks2Popup > 0)
      {
         m_nTicks2Popup--;
         if (m_nTicks2Popup == 0)
         {
            m_gameModel.setPausedGame (false);      
            m_gameModel.m_player.setVisible (true);
//            m_gameModel.m_pacMan.m_soundMgr.playSound (SoundManager.SOUND_RETURNGHOST);
         }
      }
         
      // Count down until Ghost can leave Hideout
      if (m_nTicks2Exit > 0)
      {
         m_nTicks2Exit--;
         if (m_nTicks2Exit == 0)
         {
            m_destinationX = -1;
            m_destinationY = -1;   
         }
      }
      
      // Count down until the powerup expires
      if (m_nTicks2Flee > 0)
      {
         m_nTicks2Flee--;
         if (m_nTicks2Flee == 0 && !m_bEaten)
         {
            m_deltaMax = m_ghostDeltaMax;
            m_bEaten   = false;
            m_destinationX = -1;
            m_destinationY = -1;   
         }
      }
      
      // If the ghost is located at the door and is ready to enter because
      // he was eaten, then let him in.
      if (m_bEaten &&
          m_locX == m_gameModel.m_doorLocX && 
          m_locY == (m_gameModel.m_doorLocY - 1) &&
          m_deltaLocX == 0 &&
          m_deltaLocY == 0)
      {
         m_destinationX = m_gameModel.m_doorLocX;
         m_destinationY = m_gameModel.m_doorLocY + 2;
         m_direction = DOWN;
         m_deltaLocY = 1;
         m_bInsideRoom = true;
         m_nTicks2Flee = 0;
         m_bEnteringDoor = true;
         m_deltaMax = m_ghostDeltaMax;
         return;
      }
      
      // If the ghost has entered the room and was just eaten,
      // reset it so it can wander in the room a bit before coming out
      if (m_bEaten &&
          m_locX == m_gameModel.m_doorLocX && 
          m_locY == (m_gameModel.m_doorLocY + 2) &&
          m_deltaLocX == 0 &&
          m_deltaLocY == 0)
      {
         m_destinationX = -1;
         m_destinationY = -1;
         m_direction = STILL;
         m_nTicks2Exit = 3000 / m_gameModel.m_pacMan.m_delay;
         m_bEnteringDoor = false;
         m_bEaten = false;
         return;
      }
      
      // If the ghost was just eaten and is returning to the hideout, 
      // if during this time Pacman eats another powerup, we need
      // to set the destinationX and Y back so that the ghost will continue
      // to enter the room and not get stuck
      if (m_bEnteringDoor)
      {
         m_destinationX = m_gameModel.m_doorLocX;
         m_destinationY = m_gameModel.m_doorLocY + 2;
         m_direction = DOWN;
      }
      
      // If the ghost is located at the door and is ready to leave, 
      // then let him out.
      if (m_bInsideRoom &&
          m_locX == m_gameModel.m_doorLocX && 
          m_locY == m_gameModel.m_doorLocY + 2 && 
          m_deltaLocX == 0 &&
          m_deltaLocY == 0 &&
          m_nTicks2Exit == 0)
      {
         m_destinationX = m_locX;
         m_destinationY = m_gameModel.m_doorLocY - 1;
         m_direction = UP;
         m_deltaLocY = -1;
         m_bInsideRoom = false;
         m_bEnteringDoor = false;
         m_bEaten = false;
         return;
      } 
         
      // A ghost will back off only if:
      // 1. It's not waiting to leave the room.
      // 2. It's not entering the door.
      // 3. It's not eaten.
      // 4. It's not leaving the room.
      // 5. Time to backoff is here.
      // 6. Insane AI is off
      if (m_gameModel.m_state == Model.STATE_PLAYING &&
          m_bInsideRoom == false &&
          m_bEnteringDoor == false &&
          m_bEaten == false &&
          (m_destinationX != m_gameModel.m_doorLocX && m_destinationY != m_gameModel.m_doorLocY - 1) &&
          (m_gameModel.m_pacMan.m_globalTickCount % m_gameModel.m_nTicks2Backoff) == 0 &&
          m_bInsaneAI == false)
      {
         m_destinationX = -1;   
         m_destinationY = -1;
         bBackoff = true;
      }
      
      // If there is a destination, then check if the destination has been reached.
      if (m_destinationX >= 0 && m_destinationY >= 0)
      {
         // Check if the destination has been reached, if so, then
         // get new destination.
         if (m_destinationX == m_locX &&
             m_destinationY == m_locY &&
             m_deltaLocX == 0 &&
             m_deltaLocY == 0)
         {
            m_destinationX = -1;
            m_destinationY = -1;
            prevDirection = m_direction;
         } else {
            // Otherwise, we haven't reached the destionation so
            // continue in same direction.
            return;
         }
      }

      // Reset the previous direction to allow backtracking
      if (bBackoff || (!m_bEaten && m_bCanBackTrack))
         prevDirection = STILL;
      
      // Get the next direction of the ghost.
      // This is where different AIs can be plugged.
      setNextDirection (prevDirection, bBackoff);
   }
   
   void setNextDirection (byte prevDirection, boolean bBackoff)
   {
      int deltaX, deltaY, targetX, targetY;
      Point nextLocation = new Point ();
      byte[] bestDirection = new byte[4];
      
      // If the ghost is inside the room, he needs to move to the door to get out.
      if (m_bInsideRoom)
      {
         targetX = m_gameModel.m_doorLocX;
         targetY = m_gameModel.m_doorLocY;
      } else if (m_bEaten)
      {
         // If the ghost is eaten, it needs to return to the hideout.
         targetX = m_gameModel.m_doorLocX;
         targetY = m_gameModel.m_doorLocY - 1;
         
      } else {
         // Otherwise, he is outside the door and chasing Pacman   
         if (!m_bInsaneAI && m_bCanPredict)
         {
            // Get Pacman's destination and use that as the target.
            getDestination (m_gameModel.m_player.m_direction, m_gameModel.m_player.m_locX, m_gameModel.m_player.m_locY, nextLocation);
            targetX = nextLocation.x;
            targetY = nextLocation.y;
         
         } else {
            // Get Pacman's location and use that as the target.
            targetX = m_gameModel.m_player.m_locX;
            targetY = m_gameModel.m_player.m_locY;
         }
      }
      
      deltaX = m_locX - targetX;
      deltaY = m_locY - targetY;
      
      if (Math.abs (deltaX) > Math.abs (deltaY))
      {
         if (deltaX > 0)
         {
            bestDirection[0] = LEFT;
            bestDirection[3] = RIGHT;
            if (deltaY > 0)
            {
               bestDirection[1] = UP;
               bestDirection[2] = DOWN;
            } else {
               bestDirection[1] = DOWN;
               bestDirection[2] = UP;
            }
         } else {
            bestDirection[0] = RIGHT;
            bestDirection[3] = LEFT;
            if (deltaY > 0)
            {
               bestDirection[1] = UP;
               bestDirection[2] = DOWN;
            } else {
               bestDirection[1] = DOWN;
               bestDirection[2] = UP;
            }    
         }
      } else {
         if (deltaY > 0)
         {
            bestDirection[0] = UP;
            bestDirection[3] = DOWN;
            if (deltaX > 0)
            {
               bestDirection[1] = LEFT;
               bestDirection[2] = RIGHT;
            } else {
               bestDirection[1] = RIGHT;
               bestDirection[2] = LEFT;
            }    
           
         } else {
            bestDirection[0] = DOWN;
            bestDirection[3] = UP;
            if (deltaX > 0)
            {
               bestDirection[1] = LEFT;
               bestDirection[2] = RIGHT;
            } else {
               bestDirection[1] = RIGHT;
               bestDirection[2] = LEFT;
            }    
         }
      }
      
      // There's a 50% chance that the ghost will try the sub-optimal direction first.
      // This will keep the ghosts from following each other and to trap Pacman.
      if (!m_bInsaneAI && m_bCanUseNextBest && Math.random () < .50)
      {  
         byte temp = bestDirection[0];
         bestDirection[0] = bestDirection[1];
         bestDirection[1] = temp;
      }
                  
      // If the ghost is fleeing and not eaten, then reverse the array of best directions to go.
      if (bBackoff || (m_nTicks2Flee > 0 && !m_bEaten))
      {
         byte temp = bestDirection[0];
         bestDirection[0] = bestDirection[3];
         bestDirection[3] = temp;
         
         temp = bestDirection[1];
         bestDirection[1] = bestDirection[2];
         bestDirection[2] = temp;
      }
            
      for (int i = 0; i < 4; i++)
      {
         if (bestDirection[i] == UP && 
            (m_gameModel.m_gameState[m_locX][m_locY] & m_gameModel.GS_NORTH) == 0 &&
             m_deltaLocX == 0 &&
             prevDirection != DOWN)
         {
            if (!getDestination (UP, m_locX, m_locY, nextLocation))
               continue;
            m_destinationX = nextLocation.x;
            m_destinationY = nextLocation.y;
            m_direction = UP;
            if (m_bCanFollow || !isFollowing ())
               break;
            
         } else if (bestDirection[i] == DOWN && 
            (m_gameModel.m_gameState[m_locX][m_locY] & m_gameModel.GS_SOUTH) == 0 &&
             m_deltaLocX == 0 &&
             prevDirection != UP)
         {
            if (!getDestination (DOWN, m_locX, m_locY, nextLocation))
               continue;
            m_destinationX = nextLocation.x;
            m_destinationY = nextLocation.y;
            m_direction = DOWN;
            if (m_bCanFollow || !isFollowing ())
               break;
            
         } else if (bestDirection[i] == RIGHT && 
            (m_gameModel.m_gameState[m_locX][m_locY] & m_gameModel.GS_EAST) == 0 &&
             m_deltaLocY == 0 &&
             prevDirection != LEFT)
      
         {
            if (!getDestination (RIGHT, m_locX, m_locY, nextLocation))
               continue;
            m_destinationX = nextLocation.x;
            m_destinationY = nextLocation.y;
            m_direction = RIGHT;
            if (m_bCanFollow || !isFollowing ())
               break;
            
         } else if (bestDirection[i] == LEFT && 
            (m_gameModel.m_gameState[m_locX][m_locY] & m_gameModel.GS_WEST) == 0 &&
             m_deltaLocY == 0 &&
             prevDirection != RIGHT)
         {
            if (!getDestination (LEFT, m_locX, m_locY, nextLocation))
               continue;
            m_destinationX = nextLocation.x;
            m_destinationY = nextLocation.y;
            m_direction = LEFT;
            if (m_bCanFollow || !isFollowing ())
               break;
            
         }
      } 
      
      // REMOVE
      //if (m_gameModel.m_ghosts[0] == this)
      //   System.out.println (m_direction + " " + targetX + " " + targetY);
   }
   
   // This method returns true if this ghost is traveling to the same
   // destination with the same direction as another ghost.
   boolean isFollowing ()
   {
      boolean bFollowing = false;
      double  dRandom;
   
      // If the ghost is in the same location as another ghost
      // and moving in the same direction, then they are on
      // top of each other and should not follow.
      for (int i = 0; i < m_gameModel.m_ghosts.length; i++)
      {
         // Ignore myself
         if (this == m_gameModel.m_ghosts[i])
            continue;
         
         if (m_gameModel.m_ghosts[i].m_locX == m_locX &&
             m_gameModel.m_ghosts[i].m_locY == m_locY &&
             m_gameModel.m_ghosts[i].m_direction == m_direction)
         {
            return true;
         }
      }
      
      // This will allow ghosts to often
      // clump together for easier eating
      dRandom = Math.random ();
      if (!m_bInsaneAI && dRandom < .90)
      {  
         //if (m_bInsaneAI && dRandom < .25)
         //   return false;
         //else
            return false;
      }
      
      // If ghost is moving to the same location and using the
      // same direction, then it is following another ghost.
      for (int i = 0; i < m_gameModel.m_ghosts.length; i++)
      {
         // Ignore myself
         if (this == m_gameModel.m_ghosts[i])
            continue;
         
         if (m_gameModel.m_ghosts[i].m_destinationX == m_destinationX &&
             m_gameModel.m_ghosts[i].m_destinationY == m_destinationY &&
             m_gameModel.m_ghosts[i].m_direction == m_direction)
         {
            bFollowing = true;
            break;
         }
      }
      
      return bFollowing;
   }
   
   // This method will check if the bounding box of this ghosts intersects with
   // the bound box of the player.  If so, then either kill the player or eat the 
   // fleeing ghost
   // return: 0 for no collision, 1 for ate a ghost, 2 for pacman died
   public int checkCollision (PacMan player)
   {
      Rectangle intersectRect;
      intersectRect = m_boundingBox.intersection (player.m_boundingBox);
      if (!intersectRect.isEmpty ())
      {
         // If the ghost is not fleeing and is not eaten,
         // then Pacman was caught.
         if (m_nTicks2Flee == 0 && !m_bEaten)
         {
            player.m_direction = Sprite.RIGHT;
            return 2;
            
         } else if (m_nTicks2Flee > 0 && !m_bEaten)
         {
            // If the ghost was fleeing and is not eaten,
            // then Pacman caught the Ghost.
            player.m_score += m_gameModel.m_eatGhostPoints;
            m_eatenPoints = m_gameModel.m_eatGhostPoints;
            // TODO: Remove
            //System.out.println (m_gameModel.m_eatGhostPoints);
            m_gameModel.m_eatGhostPoints *= 2;
            m_bEaten = true;
            m_destinationX = -1;
            m_destinationY = -1;
            // Boost speed of dead ghost
            // to make the eyes get back to the hideout faster
            m_deltaMax = 2;
            // Pause the game to display the points for eating this ghost.
            m_gameModel.setPausedGame (true);
            m_nTicks2Popup = 500 / m_gameModel.m_pacMan.m_delay; 
            player.setVisible (false);
            return 1;
         }
      }  
      return 0;
      
   }
   
   // This is called each time the game is restarted
   public void returnToStart ()
   {
      super.returnToStart ();
      m_destinationX = -1;
      m_destinationY = -1;
      // First ghost always starts outside of room
      if (m_gameModel.m_ghosts[0] == this)
         m_bInsideRoom = false;
      else
         m_bInsideRoom = true;
      
      m_nTicks2Exit = m_nExitMilliSec / m_gameModel.m_pacMan.m_delay;
      m_deltaMax = m_ghostDeltaMax;
      m_nTicks2Flee = 0;  
      m_bEaten = false;
      m_nTicks2Popup = 0;
      m_bEnteringDoor = false;
   }
}
