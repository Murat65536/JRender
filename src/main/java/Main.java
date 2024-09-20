package src.main.java;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main {
  public static int width = 700;
  public static int height = 500;
  public static final float MOUSE_SENSITIVITY = 2;
  public static final float MOVEMENT_SPEED = 5;
  public static final float FOV = 90;
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame frame = new JFrame();
        Window window = new Window();
        Keys keys = new Keys();
        Mouse mouse = new Mouse(frame);
        WindowComponents components = new WindowComponents();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(window);
        frame.pack();
        frame.setTitle("3d Rendering Engine");
        frame.setResizable(true);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(keys);
        frame.addMouseListener(mouse);
        frame.addMouseMotionListener(mouse);
        frame.addComponentListener(components);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      }
    });
  }
}