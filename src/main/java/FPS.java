package src.main.java;

public class FPS {
  private long lastTime;
  private float fps;

  public void update() {
    fps = 1000000000f / (System.nanoTime() - lastTime);
    lastTime = System.nanoTime();
  }

  public float getFPS() {
    return fps;
  }
}
