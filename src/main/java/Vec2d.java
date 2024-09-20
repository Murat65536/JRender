package src.main.java;

public class Vec2d {
  private float u = 0;
  private float v = 0;
  private float w = 1;

  public Vec2d(float u, float v) {
    this.u = u;
    this.v = v;
  }

  public Vec2d(float u, float v, float w) {
    this.u = u;
    this.v = v;
    this.w = w;
  }

  public Vec2d() {}

  public Vec2d clone() {
    return new Vec2d(u, v, w);
  }

  public float getU() {
    return u;
  }

  public float getV() {
    return v;
  }

  public float getW() {
    return w;
  }

  public void setU(float u) {
    this.u = u;
  }

  public void setV(float v) {
    this.v = v;
  }

  public void setW(float w) {
    this.w = w;
  }

  public void set(Vec2d vector) {
    this.u = vector.getU();
    this.v = vector.getV();
    this.w = vector.getW();
  }
}
