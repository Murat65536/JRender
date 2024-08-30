public class FPS {
  private long lastTime;
  private double fps;

  protected void update() {
    fps = 1000000000d / (System.nanoTime() - lastTime);
    lastTime = System.nanoTime();
  }

  protected double getFPS() {
    return fps;
  }
}
