import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class Mouse implements MouseListener, MouseMotionListener {
  public static final Point origin = new Point(0, 0);
  private Point mousePoint = new Point();
  private JFrame frame;

  public Mouse(JFrame frame) {
    this.frame = frame;
  }

  public void mouseEntered(MouseEvent event) {}
  public void mouseClicked(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mousePressed(MouseEvent event) {
    mousePoint = event.getPoint();
    frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));
  }
  
  public void mouseReleased(MouseEvent event) {
    frame.setCursor(Cursor.getDefaultCursor());
  }
  
  public void mouseMoved(MouseEvent event) {}
  public void mouseDragged(MouseEvent event) {
    int directionX = event.getX() - mousePoint.x;
    int directionY = event.getY() - mousePoint.y;
    origin.setLocation(origin.x + directionX, origin.y + directionY);
    try {
      new Robot().mouseMove(mousePoint.x, mousePoint.y);
    }
    catch (AWTException exception) {
      exception.printStackTrace();
    }
  }
}
