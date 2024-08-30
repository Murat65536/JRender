public class Triangle {
  protected Vec3d[] point = new Vec3d[3];
  protected short color = 0;

  public Triangle(double x1, double y1, double z1,
                  double x2, double y2, double z2,
                  double x3, double y3, double z3) {
    point[0] = new Vec3d(x1, y1, z1);
    point[1] = new Vec3d(x2, y2, z2);
    point[2] = new Vec3d(x3, y3, z3);
  }

  public Triangle(double x1, double y1, double z1,
                  double x2, double y2, double z2,
                  double x3, double y3, double z3,
                  short color) {
    point[0] = new Vec3d(x1, y1, z1);
    point[1] = new Vec3d(x2, y2, z2);
    point[2] = new Vec3d(x3, y3, z3);
    this.color = color;
  }

  public Triangle(double x1, double y1, double z1, double w1,
                  double x2, double y2, double z2, double w2,
                  double x3, double y3, double z3, double w3,
                  short color) {
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

  public Triangle clone() {
    return new Triangle(
      point[0].x, point[0].y, point[0].z,
      point[1].x, point[1].y, point[1].z,
      point[2].x, point[2].y, point[2].z,
      color
    );
  }
  public void set(Triangle triangle) {
    point[0].set(triangle.point[0]);
    point[1].set(triangle.point[1]);
    point[2].set(triangle.point[2]);
    color = triangle.color;
  }

  public void setColor(Triangle triangle) {
    color = triangle.color;
  }
}