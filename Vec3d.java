public class Vec3d {
  protected double x = 0;
  protected double y = 0;
  protected double z = 0;
  protected double w = 1;

  public Vec3d(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3d(double x, double y, double z, double w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vec3d() {}

  public Vec3d clone() {
    return new Vec3d(x, y, z, w);
  }

  public void set(Vec3d vector) {
    x = vector.x;
    y = vector.y;
    z = vector.z;
    w = vector.w;
  }
}