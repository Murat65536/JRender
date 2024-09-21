package src.main.java;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.Robot;
import javax.swing.JFrame;


public class Mouse implements MouseListener, MouseMotionListener {
  private static final Point origin = new Point(0, 0);
  private Point mousePoint = new Point();
  private JFrame frame;

  public Mouse(JFrame frame) {
    this.frame = frame;
  }

  @Override
  public void mouseClicked(MouseEvent event) {

  }
  
  @Override
  public void mouseEntered(MouseEvent event) {
    mousePoint = event.getPoint();
  }
  
  @Override
  public void mouseExited(MouseEvent event) {
    
  }
  
  @Override
  public void mousePressed(MouseEvent event) {
    frame.setCursor(frame.getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
    mousePoint = event.getPoint();
  }
  
  @Override
  public void mouseReleased(MouseEvent event) {
    frame.setCursor(Cursor.getDefaultCursor());
  }

  @Override
  public void mouseMoved(MouseEvent event) {

  }

  @Override
  public void mouseDragged(MouseEvent event) {
    // System.out.println(event.getXOnScreen() - event.getX());
    short directionX = (short)(event.getX() - mousePoint.getX());
    short directionY = (short)(event.getY() - mousePoint.getY());
    origin.setLocation(origin.getX() + directionX, Math.min(Main.height * 0.75, Math.max(-Main.height * 0.75, origin.getY() + directionY)));
    try {
      new Robot().mouseMove((int)frame.getLocation().getX() + (int)mousePoint.getX(), (int)frame.getLocation().getY() + (int)mousePoint.getY());
    }
    catch (AWTException exception) {
      exception.printStackTrace();
    }
  }

  public static float getPitch() {
    return (float)origin.getY() * Main.MOUSE_SENSITIVITY / Main.height;
  }

  public static float getYaw() {
    return (float)origin.getX() * Main.MOUSE_SENSITIVITY / Main.width;
  }
}
