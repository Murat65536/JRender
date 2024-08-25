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

  private Matrix4x4 projectionMatrix = matrixMakeProjection(90, (double)HEIGHT / WIDTH, 0.1, 1000);

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

    meshCube.loadFromObjectFile("VideoShip.obj");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    Graphics2D graphics = BUFFERED_IMAGE.createGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, WIDTH, HEIGHT);

    theta += 0.00000000000001 * System.currentTimeMillis();

    Matrix4x4 matrixRotationZ = new Matrix4x4();
    Matrix4x4 matrixRotationX = new Matrix4x4();

    matrixRotationZ = matrixMakeRotationZ(theta * 0.5);
    matrixRotationX = matrixMakeRotationX(theta);

    Matrix4x4 matrixTranslation = matrixMakeTranslation(0, 0, 8);

    Matrix4x4 matrixWorld = matrixMakeIdentity();
    matrixWorld = matrixMultiplyMatrix(matrixRotationZ, matrixRotationX);
    matrixWorld = matrixMultiplyMatrix(matrixWorld, matrixTranslation);

    ArrayList<Triangle> trianglesToRaster = new ArrayList<Triangle>();

    for (int i = 0; i < meshCube.triangles.size(); i++) {
      Triangle projectedTriangle = new Triangle();
      Triangle triangleTransformed = new Triangle();

      triangleTransformed.point[0] = matrixMultiplyVector(matrixWorld, meshCube.triangles.get(i).point[0]);
      triangleTransformed.point[1] = matrixMultiplyVector(matrixWorld, meshCube.triangles.get(i).point[1]);
      triangleTransformed.point[2] = matrixMultiplyVector(matrixWorld, meshCube.triangles.get(i).point[2]);

      Vec3d normal = new Vec3d();
      Vec3d line1 = new Vec3d();
      Vec3d line2 = new Vec3d();

      line1 = vectorSubtract(triangleTransformed.point[1], triangleTransformed.point[0]);
      line2 = vectorSubtract(triangleTransformed.point[2], triangleTransformed.point[0]);

      normal = vectorNormalize(vectorCrossProduct(line1, line2));

      Vec3d cameraRay = vectorSubtract(triangleTransformed.point[0], camera);

      if (vectorDotProduct(normal, cameraRay) < 0) {
        Vec3d lightDirection = new Vec3d(0, 1, -1);
        lightDirection = vectorNormalize(lightDirection);

        double dotProduct = Math.max(0.1, vectorDotProduct(lightDirection, normal));
        triangleTransformed.color = (short)(dotProduct * 255);

        projectedTriangle.point[0] = matrixMultiplyVector(projectionMatrix, triangleTransformed.point[0]);
        projectedTriangle.point[1] = matrixMultiplyVector(projectionMatrix, triangleTransformed.point[1]);
        projectedTriangle.point[2] = matrixMultiplyVector(projectionMatrix, triangleTransformed.point[2]);
        projectedTriangle.color = triangleTransformed.color;

        projectedTriangle.point[0] = vectorDivide(projectedTriangle.point[0], projectedTriangle.point[0].w);
        projectedTriangle.point[1] = vectorDivide(projectedTriangle.point[1], projectedTriangle.point[1].w);
        projectedTriangle.point[2] = vectorDivide(projectedTriangle.point[2], projectedTriangle.point[2].w);
        
        Vec3d offsetView = new Vec3d(1, 1, 0);
        projectedTriangle.point[0] = vectorAdd(projectedTriangle.point[0], offsetView);
        projectedTriangle.point[1] = vectorAdd(projectedTriangle.point[1], offsetView);
        projectedTriangle.point[2] = vectorAdd(projectedTriangle.point[2], offsetView);

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
      graphics.setColor(Color.BLACK);
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

  public Vec3d matrixMultiplyVector(Matrix4x4 matrix, Vec3d i) {
    Vec3d v = new Vec3d();
    v.x = i.x * matrix.matrix[0][0] + i.y * matrix.matrix[1][0] + i.z * matrix.matrix[2][0] + i.w * matrix.matrix[3][0];
    v.y = i.x * matrix.matrix[0][1] + i.y * matrix.matrix[1][1] + i.z * matrix.matrix[2][1] + i.w * matrix.matrix[3][1];
    v.z = i.x * matrix.matrix[0][2] + i.y * matrix.matrix[1][2] + i.z * matrix.matrix[2][2] + i.w * matrix.matrix[3][2];
    v.w = i.x * matrix.matrix[0][3] + i.y * matrix.matrix[1][3] + i.z * matrix.matrix[2][3] + i.w * matrix.matrix[3][3];
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

  
	public Matrix4x4 matrixMakeRotationX(double angle) {
		Matrix4x4 matrix = new Matrix4x4();
		matrix.matrix[0][0] = 1;
		matrix.matrix[1][1] = Math.cos(angle);
		matrix.matrix[1][2] = Math.sin(angle);
		matrix.matrix[2][1] = -Math.sin(angle);
		matrix.matrix[2][2] = Math.cos(angle);
		matrix.matrix[3][3] = 1;

		return matrix;
	}

  public Matrix4x4 matrixMakeRotationY(double angle) {
		Matrix4x4 matrix = new Matrix4x4();
		matrix.matrix[0][0] = Math.cos(angle);
		matrix.matrix[0][2] = Math.sin(angle);
		matrix.matrix[2][0] = -Math.sin(angle);
		matrix.matrix[1][1] = 1;
		matrix.matrix[2][2] = Math.cos(angle);
		matrix.matrix[3][3] = 1;

		return matrix;
	}

  public Matrix4x4 matrixMakeRotationZ(double angle) {
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
    for (int column = 0; column < 4; column++) {
      for (int row = 0; row < 4; row++) {
        matrix.matrix[row][column] = m1.matrix[row][0] * m2.matrix[0][column] + m1.matrix[row][1] * m2.matrix[1][column] + m1.matrix[row][2] * m2.matrix[2][column] + m1.matrix[row][3] * m2.matrix[3][column];
      }
    }
    return matrix;
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
    double length = vectorLength(v);

    return new Vec3d(v.x / length, v.y / length, v.z / length);
  }

  public Vec3d vectorCrossProduct(Vec3d v1, Vec3d v2) {
    Vec3d v = new Vec3d();
    v.x = v1.y * v2.z - v1.z * v2.y;
    v.y = v1.z * v2.x - v1.x * v2.z;
    v.z = v1.x * v2.y - v1.y * v2.x;

    return v;
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

    public Vec3d(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
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

    public void loadFromObjectFile(String fileName) {
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
      }
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
