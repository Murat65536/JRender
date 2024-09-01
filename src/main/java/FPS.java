package src.main.java;

public class FPS {
  private long lastTime;
  private float fps;

  protected void update() {
    fps = 1000000000f / (System.nanoTime() - lastTime);
    lastTime = System.nanoTime();
  }

  protected float getFPS() {
    return fps;
  }
}
