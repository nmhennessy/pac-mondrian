import java.awt.Frame;
import java.awt.Event;

class MainFrame extends Frame
{
   MainFrame (String title)
   {
      super (title);
      setUndecorated(true);
   }
   
   public boolean handleEvent (Event e)
   {
      if (e.id ==Event.WINDOW_DESTROY)
      {
         System.exit (0);
      }
      return super.handleEvent (e);
   }
}
