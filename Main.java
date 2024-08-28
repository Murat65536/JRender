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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(window);
        frame.pack();
        frame.setTitle("3d Rendering Engine Test");
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(keys);
        frame.setVisible(true);
      }
    });
  }
}