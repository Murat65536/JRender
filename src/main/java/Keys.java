package src.main.java;

import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keys implements KeyListener {
  protected static ArrayList<Integer> pressedKeys = new ArrayList<Integer>();

  public void keyPressed(KeyEvent event) {
    if (!pressedKeys.contains(event.getKeyCode())) {
      pressedKeys.add(event.getKeyCode());
    }
  }

  public void keyReleased(KeyEvent event) {
    if (pressedKeys.contains(event.getKeyCode())) {
      pressedKeys.remove(Integer.valueOf(event.getKeyCode()));
    }
  }
  public void keyTyped(KeyEvent event) {}
}
