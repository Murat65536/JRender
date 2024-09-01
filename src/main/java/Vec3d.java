package src.main.java;

public class Vec3d {
  protected float x = 0;
  protected float y = 0;
  protected float z = 0;
  protected float w = 1;

  public Vec3d(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3d(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vec3d() {}

  public Vec3d clone() {
    return new Vec3d(x, y, z, w);
  }

  protected void set(Vec3d vector) {
    x = vector.x;
    y = vector.y;
    z = vector.z;
    w = vector.w;
  }
}