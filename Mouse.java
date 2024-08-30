import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Mouse implements MouseListener, MouseMotionListener {
  public static final Point origin = new Point(0, 0);
  private Point mousePoint = new Point();
  
  public void mouseEntered(MouseEvent event) {}
  public void mouseReleased(MouseEvent event) {}
  public void mouseClicked(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mousePressed(MouseEvent event) {
    mousePoint = event.getPoint();
  }

  public void mouseMoved(MouseEvent event) {}
  public void mouseDragged(MouseEvent event) {
    int directionX = event.getX() - mousePoint.x;
    int directionY = event.getY() - mousePoint.y;
    origin.setLocation(origin.x + directionX, origin.y + directionY);
    mousePoint = event.getPoint();
  }
}
