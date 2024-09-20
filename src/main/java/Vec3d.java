package src.main.java;

public class Vec3d {
  private float x = 0;
  private float y = 0;
  private float z = 0;
  private float w = 1;

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

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getZ() {
    return z;
  }

  public float getW() {
    return w;
  }

  public void setX(float x) {
    this.x = x;
  }

  public void setY(float y) {
    this.y = y;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public void setW(float w) {
    this.w = w;
  }

  public void set(Vec3d vector) {
    this.x = vector.getX();
    this.y = vector.getY();
    this.z = vector.getZ();
    this.w = vector.getW();
  }

  public static Vec3d add(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());
  }

  public static Vec3d subtract(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.getX() - v2.getX(), v1.getY() - v2.getY(), v1.getZ() - v2.getZ());
  }

  public static Vec3d multiply(Vec3d v1, float k) {
    return new Vec3d(v1.getX() * k, v1.getY() * k, v1.getZ() * k);
  }

  public static Vec3d divide(Vec3d v1, float k) {
    return new Vec3d(v1.getX() / k, v1.getY() / k, v1.getZ() / k);
  }

  public static float dotProduct(Vec3d v1, Vec3d v2) {
    return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
  }

  public static float length(Vec3d v) {
    return 1 / (float)Math.sqrt(dotProduct(v, v));
  }

  public static Vec3d multiplyMatrix(Matrix m, Vec3d i) {
    return new Vec3d(
      i.getX() * (float)m.matrix[0][0] + i.getY() * (float)m.matrix[1][0] + i.getZ() * (float)m.matrix[2][0] + i.getW() * (float)m.matrix[3][0],
      i.getX() * (float)m.matrix[0][1] + i.getY() * (float)m.matrix[1][1] + i.getZ() * (float)m.matrix[2][1] + i.getW() * (float)m.matrix[3][1],
      i.getX() * (float)m.matrix[0][2] + i.getY() * (float)m.matrix[1][2] + i.getZ() * (float)m.matrix[2][2] + i.getW() * (float)m.matrix[3][2],
      i.getX() * (float)m.matrix[0][3] + i.getY() * (float)m.matrix[1][3] + i.getZ() * (float)m.matrix[2][3] + i.getW() * (float)m.matrix[3][3]
    );
  }

  public static Vec3d normalize(Vec3d v) {
    float l = length(v);
    return new Vec3d(v.getX() * l, v.getY() * l, v.getZ() * l);
  }

  public static Vec3d crossProduct(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.getY() * v2.getZ() - v1.getZ() * v2.getY(), v1.getZ() * v2.getX() - v1.getX() * v2.getZ(), v1.getX() * v2.getY() - v1.getY() * v2.getX());
  }

  public static Vec3d planeIntersect(Vec3d planeP, Vec3d planeN, Vec3d lineStart, Vec3d lineEnd, Texture t) {
    planeN = normalize(planeN);
    float planeD = -dotProduct(planeN, planeP);
    float ad = dotProduct(lineStart, planeN);
    float bd = dotProduct(lineEnd, planeN);
    t.setTexture((-planeD - ad) / (bd - ad));
    Vec3d lineStartToEnd = subtract(lineEnd, lineStart);
    Vec3d lineToIntersect = multiply(lineStartToEnd, t.getTexture());

    return add(lineStart, lineToIntersect);
  }
}