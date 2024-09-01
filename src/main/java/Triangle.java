package src.main.java;

public class Triangle {
  protected Vec3d[] point = new Vec3d[3];
  protected short color = 0;

  public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
    point[0] = new Vec3d(x1, y1, z1);
    point[1] = new Vec3d(x2, y2, z2);
    point[2] = new Vec3d(x3, y3, z3);
  }

  public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, short color) {
    point[0] = new Vec3d(x1, y1, z1);
    point[1] = new Vec3d(x2, y2, z2);
    point[2] = new Vec3d(x3, y3, z3);
    this.color = color;
  }

  public Triangle(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2, float x3, float y3, float z3, float w3, short color) {
    point[0] = new Vec3d(x1, y1, z1, w1);
    point[1] = new Vec3d(x2, y2, z2, w2);
    point[2] = new Vec3d(x3, y3, z3, w3);
    this.color = color;
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3) {
    point[0] = point1.clone();
    point[1] = point2.clone();
    point[2] = point3.clone();
  }

  public Triangle() {
    point[0] = new Vec3d();
    point[1] = new Vec3d();
    point[2] = new Vec3d();
  }

  protected Triangle clone() {
    return new Triangle(
      point[0].x, point[0].y, point[0].z,
      point[1].x, point[1].y, point[1].z,
      point[2].x, point[2].y, point[2].z,
      color
    );
  }
  protected void set(Triangle triangle) {
    point[0].set(triangle.point[0]);
    point[1].set(triangle.point[1]);
    point[2].set(triangle.point[2]);
    color = triangle.color;
  }

  protected void setColor(Triangle triangle) {
    color = triangle.color;
  }
}