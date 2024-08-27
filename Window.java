import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Window extends JPanel implements ActionListener {
  private final int WIDTH = 256;
  private final int HEIGHT = 240;
  private final BufferedImage BUFFERED_IMAGE;
  private final JLabel J_LABEL = new JLabel();
  private final Timer TIMER = new Timer(10, this);

  private static Mesh meshCube = new Mesh();


  private double near = 0.1;
  private double far = 1000;
  private double fov = 90;
  private double aspectRatio = (double)HEIGHT / WIDTH;
  private double fovRadians = 1 / Math.tan(Math.toRadians(fov * 0.5));
  private Matrix4x4 projectionMatrix = new Matrix4x4(new double[][] {
    {aspectRatio * fovRadians, 0, 0, 0},
    {0, fovRadians, 0, 0},
    {0, 0, far / (far - near), 1},
    {0, 0, (far * near) / (far - near), 0}
  });

  private double theta = 0;
  private Vec3d camera = new Vec3d();

  public Window() {
    super(true);
    this.setLayout(new GridLayout());
    this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    BUFFERED_IMAGE = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    J_LABEL.setIcon(new ImageIcon(BUFFERED_IMAGE));
    this.add(J_LABEL);
    TIMER.start();

    meshCube.loadFromObjectFile("teapot.obj");
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

    ArrayList<Triangle> trianglesToRaster = new ArrayList<Triangle>();

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
      translatedTriangle.point[0].z = zXRotatedTriangle.point[0].z + 8;
      translatedTriangle.point[1].z = zXRotatedTriangle.point[1].z + 8;
      translatedTriangle.point[2].z = zXRotatedTriangle.point[2].z + 8;

      Vec3d normal = new Vec3d();
      Vec3d line1 = new Vec3d();
      Vec3d line2 = new Vec3d();

      line1.x = translatedTriangle.point[1].x - translatedTriangle.point[0].x;
      line1.y = translatedTriangle.point[1].y - translatedTriangle.point[0].y;
      line1.z = translatedTriangle.point[1].z - translatedTriangle.point[0].z;

      line2.x = translatedTriangle.point[2].x - translatedTriangle.point[0].x;
      line2.y = translatedTriangle.point[2].y - translatedTriangle.point[0].y;
      line2.z = translatedTriangle.point[2].z - translatedTriangle.point[0].z;

      normal.x = line1.y * line2.z - line1.z * line2.y;
      normal.y = line1.z * line2.x - line1.x * line2.z;
      normal.z = line1.x * line2.y - line1.y * line2.x;

      double normalLength = Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
      normal.x /= normalLength;
      normal.y /= normalLength;
      normal.z /= normalLength;

      if (normal.x * (translatedTriangle.point[0].x - camera.x) +
          normal.y * (translatedTriangle.point[0].y - camera.y) +
          normal.z * (translatedTriangle.point[0].z - camera.z) < 0) {

        Vec3d lightDirection = new Vec3d(0, 0, -1);
        double lightDirectionLength = Math.sqrt(lightDirection.x * lightDirection.x + lightDirection.y * lightDirection.y + lightDirection.z * lightDirection.z);
        lightDirection.x /= lightDirectionLength;
        lightDirection.y /= lightDirectionLength;
        lightDirection.z /= lightDirectionLength;

        double dotProduct = normal.x * lightDirection.x + normal.y * lightDirection.y + normal.z * lightDirection.z;
        translatedTriangle.color = (short)(dotProduct * 255);

        projectedTriangle.point[0] = multiplyMatrixVector(translatedTriangle.point[0], projectionMatrix);
        projectedTriangle.point[1] = multiplyMatrixVector(translatedTriangle.point[1], projectionMatrix);
        projectedTriangle.point[2] = multiplyMatrixVector(translatedTriangle.point[2], projectionMatrix);
        projectedTriangle.color = translatedTriangle.color;
        
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

        trianglesToRaster.add(projectedTriangle);
      }
    }

    Collections.sort(trianglesToRaster, (t1, t2) -> {
      double z1 = (t1.point[0].z + t1.point[1].z + t1.point[2].z) / 3;
      double z2 = (t2.point[0].z + t2.point[1].z + t2.point[2].z) / 3;
      if (z1 > z2) {
        return 1;
      }
      return -1;
    });

    for (Triangle projectedTriangles : trianglesToRaster) {
      graphics.setColor(new Color(Math.min(Math.max(projectedTriangles.color, 0), 255), Math.min(Math.max(projectedTriangles.color, 0), 255), Math.min(Math.max(projectedTriangles.color, 0), 255)));
      graphics.fillPolygon(new int[] {
        (int)projectedTriangles.point[0].x,
        (int)projectedTriangles.point[1].x,
        (int)projectedTriangles.point[2].x
      }, new int[] {
        (int)projectedTriangles.point[0].y,
        (int)projectedTriangles.point[1].y,
        (int)projectedTriangles.point[2].y
      }, 3);
      // graphics.setColor(Color.BLACK);
      // graphics.drawPolygon(new int[] {
      //   (int)projectedTriangles.point[0].x,
      //   (int)projectedTriangles.point[1].x,
      //   (int)projectedTriangles.point[2].x
      // }, new int[] {
      //   (int)projectedTriangles.point[0].y,
      //   (int)projectedTriangles.point[1].y,
      //   (int)projectedTriangles.point[2].y
      // }, 3);
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

  public Vec3d matrixMultiplyVector(Matrix4x4 m, Vec3d i) {
    Vec3d v = new Vec3d();
    v.x = i.x * m.matrix[0][0] + i.y * m.matrix[1][0] + i.z * m.matrix[2][0] + i.w * m.matrix[3][0];
    v.y = i.x * m.matrix[0][1] + i.y * m.matrix[1][1] + i.z * m.matrix[2][1] + i.w * m.matrix[3][1];
    v.z = i.x * m.matrix[0][2] + i.y * m.matrix[1][2] + i.z * m.matrix[2][2] + i.w * m.matrix[3][2];
    v.w = i.x * m.matrix[0][3] + i.y * m.matrix[1][3] + i.z * m.matrix[2][3] + i.w * m.matrix[3][3];

    return v;
  }

  public Vec3d vectorAdd(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
  }

  public Vec3d vectorSubtract(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
  }

  public Vec3d vectorMultiply(Vec3d v1, double k) {
    return new Vec3d(v1.x * k, v1.y * k, v1.z * k);
  }

  public Vec3d vectorDivide(Vec3d v1, double k) {
    return new Vec3d(v1.x / k, v1.y / k, v1.z / k);
  }

  public double vectorDotProduct(Vec3d v1, Vec3d v2) {
    return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
  }

  public double vectorLength(Vec3d v) {
    return Math.sqrt(vectorDotProduct(v, v));
  }

  public Vec3d vectorNormalize(Vec3d v) {
    double l = vectorLength(v);
    return new Vec3d(v.x / l, v.y / l, v.z / l);
  }

  public Vec3d vectorCrossProduct(Vec3d v1, Vec3d v2) {
    Vec3d v = new Vec3d();
    v.x = v1.y * v2.z - v1.z * v2.y;
    v.y = v1.z * v2.x - v1.x * v2.z;
    v.z = v1.x * v2.y - v1.y * v2.x;

    return v;
  }

  public Matrix4x4 matrixMakeIdentity() {
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = 1;
    matrix.matrix[1][1] = 1;
    matrix.matrix[2][2] = 1;
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public Matrix4x4 matrixRotationX(double angle) {
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = 1;
    matrix.matrix[1][1] = Math.cos(angle);
    matrix.matrix[1][2] = Math.sin(angle);
    matrix.matrix[2][1] = -Math.sin(angle);
    matrix.matrix[2][2] = Math.cos(angle);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public Matrix4x4 matrixRotationY(double angle) {
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = Math.cos(angle);
    matrix.matrix[0][2] = Math.sin(angle);
    matrix.matrix[2][0] = -Math.sin(angle);
    matrix.matrix[1][1] = 1;
    matrix.matrix[2][2] = Math.cos(angle);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public Matrix4x4 matrixRotationZ(double angle) {
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = Math.cos(angle);
    matrix.matrix[0][1] = Math.sin(angle);
    matrix.matrix[1][0] = -Math.sin(angle);
    matrix.matrix[1][1] = Math.cos(angle);
    matrix.matrix[2][2] = 1;
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public Matrix4x4 matrixMakeTranslation(double x, double y, double z) {
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = 1;
    matrix.matrix[1][1] = 1;
    matrix.matrix[2][2] = 1;
    matrix.matrix[3][3] = 1;
    matrix.matrix[3][0] = x;
    matrix.matrix[3][1] = y;
    matrix.matrix[3][2] = z;

    return matrix;
  }

  public Matrix4x4 matrixMakeProjection(double fov, double aspectRatio, double near, double far) {
    double fovRadians = 1 / Math.tan(Math.toRadians(fov * 0.5));
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = aspectRatio * fovRadians;
    matrix.matrix[1][1] = fovRadians;
    matrix.matrix[2][2] = far / (far - near);
    matrix.matrix[3][2] = (-far * near) / (far - near);
    matrix.matrix[2][3] = 1;
    matrix.matrix[3][3] = 0;

    return matrix;
  }

  public Matrix4x4 matrixMultiplyMatrix(Matrix4x4 m1, Matrix4x4 m2) {
    Matrix4x4 matrix = new Matrix4x4();
    for (int c = 0; c < 4; c++) {
      for (int r = 0; r < 4; r++) {
        matrix.matrix[r][c] = m1.matrix[r][0] * m2.matrix[0][c] + m1.matrix[r][1] * m2.matrix[1][c] + m1.matrix[r][2] * m2.matrix[2][c] + m1.matrix[r][3] * m2.matrix[3][c];
      }
    }
    return matrix;
  }

  public static class Vec3d {
    public double x = 0;
    public double y = 0;
    public double z = 0;
    public double w = 1;

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

    public Vec3d clone() {
      return new Vec3d(x, y, z);
    }
  }

  public static class Triangle {
    public Vec3d[] point = new Vec3d[3];
    public short color = 0;

    public Triangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
      point[0] = new Vec3d(x1, y1, z1);
      point[1] = new Vec3d(x2, y2, z2);
      point[2] = new Vec3d(x3, y3, z3);
    }

    public Triangle(Vec3d point1, Vec3d point2, Vec3d point3) {
      point[0] = point1.clone();
      point[1] = point2.clone();
      point[2] = point3.clone();
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

    public Mesh() {}

    public boolean loadFromObjectFile(String fileName) {
      try {
        File file = new File(fileName);
        Scanner reader = new Scanner(file);
        ArrayList<Vec3d> vertices = new ArrayList<Vec3d>();
        while (reader.hasNextLine()) {
          String data = reader.nextLine();
          String[] splitData = data.split(" ");

          if (data.length() > 0) {
            if (data.charAt(0) == 'v') {
              Vec3d vector = new Vec3d(Double.parseDouble(splitData[1]), Double.parseDouble(splitData[2]), Double.parseDouble(splitData[3]));
              vertices.add(vector);
            }
            else if (data.charAt(0) == 'f') {
              int[] faces = new int[3];
              faces[0] = Integer.parseInt(splitData[1]);
              faces[1] = Integer.parseInt(splitData[2]);
              faces[2] = Integer.parseInt(splitData[3]);
              meshCube.triangles.add(new Triangle(vertices.get(faces[0] - 1), vertices.get(faces[1] - 1), vertices.get(faces[2] - 1)));
            }
          }
        }
        reader.close();
      }
      catch (FileNotFoundException exception) {
        exception.printStackTrace();
        return false;
      }
      return true;
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