import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main {
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame frame = new JFrame();
        Window window = new Window();
        Keys keys = new Keys();
        Mouse mouse = new Mouse();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(window);
        frame.pack();
        frame.setTitle("3d Rendering Engine");
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(keys);
        frame.addMouseListener(mouse);
        frame.addMouseMotionListener(mouse);
        frame.setVisible(true);
      }
    });
  }
}