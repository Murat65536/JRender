package src.main.java;

import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keys implements KeyListener {
  public static ArrayList<Integer> pressedKeys = new ArrayList<>();

  @Override
  public void keyPressed(KeyEvent event) {
    if (!pressedKeys.contains(event.getKeyCode())) {
      pressedKeys.add(event.getKeyCode());
    }
  }

  @Override
  public void keyReleased(KeyEvent event) {
    if (pressedKeys.contains(event.getKeyCode())) {
      pressedKeys.remove(Integer.valueOf(event.getKeyCode()));
    }
  }

  @Override
  public void keyTyped(KeyEvent event) {

  }

  public static void getKeys(FPS fps) {
    Vec3d forward = Vec3d.multiply(Window.lookDirection, Main.MOVEMENT_SPEED / fps.getFPS());
    Vec3d strafe = Vec3d.multiply(Window.strafeDirection, Main.MOVEMENT_SPEED / fps.getFPS());
    if (pressedKeys.contains(KeyEvent.VK_W)) {
      Window.camera = Vec3d.add(Window.camera, forward);
    }
    
    if (pressedKeys.contains(KeyEvent.VK_S)) {
      Window.camera = Vec3d.subtract(Window.camera, forward);
    }

    if (pressedKeys.contains(KeyEvent.VK_A)) {
      Window.camera = Vec3d.subtract(Window.camera, strafe);
    }
    
    if (pressedKeys.contains(KeyEvent.VK_D)) {
      Window.camera = Vec3d.add(Window.camera, strafe);
    }
    
    if (pressedKeys.contains(KeyEvent.VK_SPACE)) {
      Window.camera.setY(Window.camera.getY() + Main.MOVEMENT_SPEED / fps.getFPS());
    }
    
    if (pressedKeys.contains(KeyEvent.VK_SHIFT)) {
      Window.camera.setY(Window.camera.getY() - Main.MOVEMENT_SPEED / fps.getFPS());
    }
  }
}
