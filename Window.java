import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Window extends JPanel implements ActionListener {
  private final int WIDTH = 256;
  private final int HEIGHT = 240;
  private final BufferedImage BUFFERED_IMAGE;
  private final JLabel J_LABEL = new JLabel();
  private final Timer TIMER = new Timer(10, this);

  private static Mesh meshCube = new Mesh(Arrays.asList(
    // South
    new Triangle(
      0, 0, 0,
      0, 1, 0,
      1, 1, 0
    ),
    new Triangle(
      0, 0, 0,
      1, 1, 0,
      1, 0, 0
    ),
    // East
    new Triangle(
      1, 0, 0,
      1, 1, 0,
      1, 1, 1
    ),
    new Triangle(
      1, 0, 0,
      1, 1, 1,
      1, 0, 1
    ),
    // North
    new Triangle(
      1, 0, 1,
      1, 1, 1,
      0, 1, 1
    ),
    new Triangle(
      1, 0, 1,
      0, 1, 1,
      0, 0, 1
    ),
    // West
    new Triangle(
      0, 0, 1,
      0, 1, 1,
      1, 1, 1
    ),
    new Triangle(
      0, 0, 1,
      0, 1, 0,
      0, 0, 0
    ),
    // Top
    new Triangle(
      0, 1, 0,
      0, 1, 1,
      1, 1, 1
    ),
    new Triangle(
      0, 1, 0,
      1, 1, 1,
      1, 1, 0
    ),
    // Bottom
    new Triangle(
      1, 0, 1,
      0, 0, 1,
      0, 0, 0
    ),
    new Triangle(
      1, 0, 1,
      0, 0, 1,
      1, 0, 0
    )
  ));

  private double near = 0.1;
  private double far = 1000;
  private double fov = 90;
  private double aspectRatio = (double)HEIGHT / WIDTH;
  private double fovRadians = 1 / Math.tan(fov * 0.5 / 180 * Math.PI);
  private Matrix4x4 projectionMatrix = new Matrix4x4(new double[][] {
    {aspectRatio * fovRadians, 0, 0, 0},
    {0, fovRadians, 0, 0},
    {0, 0, far / (far - near), 1},
    {0, 0, (far * near) / (far - near), 0}
  });

  private double theta = 0;

  public Window() {
    super(true);
    this.setLayout(new GridLayout());
    this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    BUFFERED_IMAGE = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    J_LABEL.setIcon(new ImageIcon(BUFFERED_IMAGE));
    this.add(J_LABEL);
    TIMER.start();
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    Graphics2D graphics = BUFFERED_IMAGE.createGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, WIDTH, HEIGHT);

    Matrix4x4 rotationMatrixZ = new Matrix4x4();
    Matrix4x4 rotationMatrixX = new Matrix4x4();

    theta += 0.00000000000001 * System.currentTimeMillis();

    rotationMatrixZ.matrix[0][0] = Math.cos(theta);
    rotationMatrixZ.matrix[0][1] = Math.sin(theta);
    rotationMatrixZ.matrix[1][0] = -Math.sin(theta);
    rotationMatrixZ.matrix[1][1] = Math.cos(theta);
    rotationMatrixZ.matrix[2][2] = 1;
    rotationMatrixZ.matrix[3][3] = 1;

    rotationMatrixX.matrix[0][0] = 1;
    rotationMatrixX.matrix[1][1] = Math.cos(theta * 0.5);
    rotationMatrixX.matrix[1][2] = Math.sin(theta * 0.5);
    rotationMatrixX.matrix[2][1] = -Math.sin(theta * 0.5);
    rotationMatrixX.matrix[2][2] = Math.cos(theta * 0.5);
    rotationMatrixX.matrix[3][3] = 1;

    graphics.setColor(Color.WHITE);
    for (int i = 0; i < meshCube.triangles.size(); i++) {
      Triangle projectedTriangle = new Triangle();
      Triangle translatedTriangle = new Triangle();
      Triangle zRotatedTriangle = new Triangle();
      Triangle zXRotatedTriangle = new Triangle();

      zRotatedTriangle.point[0] = multiplyMatrixVector(meshCube.triangles.get(i).point[0], rotationMatrixZ);
      zRotatedTriangle.point[1] = multiplyMatrixVector(meshCube.triangles.get(i).point[1], rotationMatrixZ);
      zRotatedTriangle.point[2] = multiplyMatrixVector(meshCube.triangles.get(i).point[2], rotationMatrixZ);

      zXRotatedTriangle.point[0] = multiplyMatrixVector(zRotatedTriangle.point[0], rotationMatrixX);
      zXRotatedTriangle.point[1] = multiplyMatrixVector(zRotatedTriangle.point[1], rotationMatrixX);
      zXRotatedTriangle.point[2] = multiplyMatrixVector(zRotatedTriangle.point[2], rotationMatrixX);
      
      translatedTriangle = zXRotatedTriangle.clone();
      translatedTriangle.point[0].z = zXRotatedTriangle.point[0].z + 3;
      translatedTriangle.point[1].z = zXRotatedTriangle.point[1].z + 3;
      translatedTriangle.point[2].z = zXRotatedTriangle.point[2].z + 3;

      projectedTriangle.point[0] = multiplyMatrixVector(translatedTriangle.point[0], projectionMatrix);
      projectedTriangle.point[1] = multiplyMatrixVector(translatedTriangle.point[1], projectionMatrix);
      projectedTriangle.point[2] = multiplyMatrixVector(translatedTriangle.point[2], projectionMatrix);

      projectedTriangle.point[0].x += 1;
      projectedTriangle.point[0].y += 1;
      projectedTriangle.point[1].x += 1;
      projectedTriangle.point[1].y += 1;
      projectedTriangle.point[2].x += 1;
      projectedTriangle.point[2].y += 1;

      projectedTriangle.point[0].x *= 0.5 * (double)WIDTH;
      projectedTriangle.point[0].y *= 0.5 * (double)HEIGHT;
      projectedTriangle.point[1].x *= 0.5 * (double)WIDTH;
      projectedTriangle.point[1].y *= 0.5 * (double)HEIGHT;
      projectedTriangle.point[2].x *= 0.5 * (double)WIDTH;
      projectedTriangle.point[2].y *= 0.5 * (double)HEIGHT;

      graphics.drawPolygon(new int[] {
        (int)projectedTriangle.point[0].x,
        (int)projectedTriangle.point[1].x,
        (int)projectedTriangle.point[2].x
      }, new int[] {
        (int)projectedTriangle.point[0].y,
        (int)projectedTriangle.point[1].y,
        (int)projectedTriangle.point[2].y
      }, 3);
    }

    graphics.dispose();
    J_LABEL.repaint();
  }

  public Vec3d multiplyMatrixVector(Vec3d vector, Matrix4x4 matrix) {
    Vec3d output = new Vec3d(
      vector.x * matrix.matrix[0][0] + vector.y * matrix.matrix[1][0] + vector.z * matrix.matrix[2][0] + matrix.matrix[3][0],
      vector.x * matrix.matrix[0][1] + vector.y * matrix.matrix[1][1] + vector.z * matrix.matrix[2][1] + matrix.matrix[3][1],
      vector.x * matrix.matrix[0][2] + vector.y * matrix.matrix[1][2] + vector.z * matrix.matrix[2][2] + matrix.matrix[3][2]
    );
    double w = vector.x * matrix.matrix[0][3] + vector.y * matrix.matrix[1][3] + vector.z * matrix.matrix[2][3] + matrix.matrix[3][3];
    if (w != 0) {
      output.x /= w;
      output.y /= w;
      output.z /= w;
    }

    return output;
  }

  public static class Vec3d {
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public Vec3d(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public Vec3d() {
      this.x = 0;
      this.y = 0;
      this.z = 0;
    }
  }

  public static class Triangle {
    public Vec3d[] point = new Vec3d[3];

    public Triangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
      point[0] = new Vec3d(x1, y1, z1);
      point[1] = new Vec3d(x2, y2, z2);
      point[2] = new Vec3d(x3, y3, z3);
    }

    public Triangle() {
      point[0] = new Vec3d(0, 0, 0);
      point[1] = new Vec3d(0, 0, 0);
      point[2] = new Vec3d(0, 0, 0);
    }

    public Triangle clone() {
      return new Triangle(
        point[0].x, point[0].y, point[0].z,
        point[1].x, point[1].y, point[1].z,
        point[2].x, point[2].y, point[2].z
      );
    }
  }

  public static class Mesh {
    public ArrayList<Triangle> triangles = new ArrayList<Triangle>();

    public Mesh(List<Triangle> triangles) {
      this.triangles.addAll(triangles);
    }
  }

  public static class Matrix4x4 {
    public double[][] matrix = new double[4][4];

    public Matrix4x4(double[][] matrix) {
      this.matrix = matrix;
    }

    public Matrix4x4() {
      this.matrix = new double[][] {
        {0, 0, 0, 0},
        {0, 0, 0, 0},
        {0, 0, 0, 0},
        {0, 0, 0, 0}
      };
    }
  }
}
