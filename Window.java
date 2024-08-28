import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class Window extends JPanel implements ActionListener, KeyListener {
  private final int WIDTH = 1024;
  private final int HEIGHT = 960;
  private final BufferedImage BUFFERED_IMAGE;
  private final JLabel J_LABEL = new JLabel();
  private final Timer TIMER = new Timer(10, this);

  private static Mesh meshCube = new Mesh();

  private Matrix4x4 projectionMatrix = matrixMakeProjection(90, (double)HEIGHT / WIDTH, 0.1, 1000);

  private double theta = 0;
  private Vec3d camera = new Vec3d();
  private Vec3d lookDirection = new Vec3d();
  private double yaw = 0;

  private static ArrayList<Integer> pressedKeys = new ArrayList<Integer>();

  public Window() {
    super(true);
    this.setLayout(new GridLayout());
    this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    BUFFERED_IMAGE = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    J_LABEL.setIcon(new ImageIcon(BUFFERED_IMAGE));
    this.add(J_LABEL);
    TIMER.start();

    meshCube.loadFromObjectFile("axis.obj");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    getKeys();
    Graphics2D graphics = BUFFERED_IMAGE.createGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, WIDTH, HEIGHT);

    // theta += 0.00000000000001 * System.currentTimeMillis();

    Matrix4x4 rotationMatrixZ = matrixRotationZ(theta * 0.5);
    Matrix4x4 rotationMatrixX = matrixRotationX(theta);

    Matrix4x4 translationMatrix = matrixMakeTranslation(0, 0, 16);
    Matrix4x4 worldMatrix = matrixMultiplyMatrix(matrixMultiplyMatrix(rotationMatrixZ, rotationMatrixX), translationMatrix);

    Vec3d up = new Vec3d(0, 1, 0);
    Vec3d target = new Vec3d(0, 0, 1);
    Matrix4x4 cameraRotateMatrix = matrixRotationY(yaw);
    lookDirection = matrixMultiplyVector(cameraRotateMatrix, target);
    target = vectorAdd(camera, lookDirection);

    Matrix4x4 cameraMatrix = matrixPointAt(camera, target, up);
    Matrix4x4 viewMatrix = matrixQuickInverse(cameraMatrix);

    ArrayList<Triangle> trianglesToRaster = new ArrayList<Triangle>();

    for (int i = 0; i < meshCube.triangles.size(); i++) {
      Triangle projectedTriangle = new Triangle();
      Triangle transformedTriangle = new Triangle();
      Triangle triangleViewed = new Triangle();
      
      transformedTriangle.point[0] = matrixMultiplyVector(worldMatrix, meshCube.triangles.get(i).point[0]);
      transformedTriangle.point[1] = matrixMultiplyVector(worldMatrix, meshCube.triangles.get(i).point[1]);
      transformedTriangle.point[2] = matrixMultiplyVector(worldMatrix, meshCube.triangles.get(i).point[2]);

      Vec3d normal = new Vec3d();
      Vec3d line1 = new Vec3d();
      Vec3d line2 = new Vec3d();

      line1 = vectorSubtract(transformedTriangle.point[1], transformedTriangle.point[0]);
      line2 = vectorSubtract(transformedTriangle.point[2], transformedTriangle.point[0]);

      normal = vectorCrossProduct(line1, line2);
      normal = vectorNormalize(normal);

      double normalLength = Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
      normal.x /= normalLength;
      normal.y /= normalLength;
      normal.z /= normalLength;

      Vec3d cameraRay = vectorSubtract(transformedTriangle.point[0], camera);

      if (vectorDotProduct(normal, cameraRay) < 0) {

        Vec3d lightDirection = new Vec3d(0, 0, -1);
        lightDirection = vectorNormalize(lightDirection);
        double dotProduct = Math.max(0.1, vectorDotProduct(lightDirection, normal));
        transformedTriangle.color = (short)(dotProduct * 255);

        triangleViewed.point[0] = matrixMultiplyVector(viewMatrix, transformedTriangle.point[0]);
        triangleViewed.point[1] = matrixMultiplyVector(viewMatrix, transformedTriangle.point[1]);
        triangleViewed.point[2] = matrixMultiplyVector(viewMatrix, transformedTriangle.point[2]);
        triangleViewed.color = transformedTriangle.color;
        
        int clippedTriangles = 0;
        Triangle[] clipped = new Triangle[2];
        clipped[0] = new Triangle();
        clipped[1] = new Triangle();
        clippedTriangles = triangleClipAgainstPlane(new Vec3d(0, 0, 0.1), new Vec3d(0, 0, 1), triangleViewed, clipped[0], clipped[1]);
        if (clippedTriangles == 2) {
          System.out.println(clipped[1].color);
        }
        for (int j = 0; j < clippedTriangles; j++) {
          projectedTriangle.point[0] = multiplyMatrixVector(clipped[j].point[0], projectionMatrix);
          projectedTriangle.point[1] = multiplyMatrixVector(clipped[j].point[1], projectionMatrix);
          projectedTriangle.point[2] = multiplyMatrixVector(clipped[j].point[2], projectionMatrix);
          projectedTriangle.color = clipped[j].color;

          projectedTriangle.point[0] = vectorDivide(projectedTriangle.point[0], projectedTriangle.point[0].w);
          projectedTriangle.point[1] = vectorDivide(projectedTriangle.point[1], projectedTriangle.point[1].w);
          projectedTriangle.point[2] = vectorDivide(projectedTriangle.point[2], projectedTriangle.point[2].w);
        
          projectedTriangle.point[0].x *= -1;
          projectedTriangle.point[1].x *= -1;
          projectedTriangle.point[2].x *= -1;
          projectedTriangle.point[0].y *= -1;
          projectedTriangle.point[1].y *= -1;
          projectedTriangle.point[2].y *= -1;


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
          trianglesToRaster.add(projectedTriangle.clone());
        }
      }
    }

    Collections.sort(trianglesToRaster, (t1, t2) -> {
      double z1 = (t1.point[0].z + t1.point[1].z + t1.point[2].z) / 3;
      double z2 = (t2.point[0].z + t2.point[1].z + t2.point[2].z) / 3;

      if (z1 > z2) {
        return -1;
      }
      else if (z1 < z2) {
        return 1;
      }
      return 0;
    });

    for (Triangle projectedTriangles : trianglesToRaster) {
      graphics.setColor(new Color(projectedTriangles.color, projectedTriangles.color, projectedTriangles.color));
      graphics.fillPolygon(new int[] {
        (int)projectedTriangles.point[0].x,
        (int)projectedTriangles.point[1].x,
        (int)projectedTriangles.point[2].x
      }, new int[] {
        (int)projectedTriangles.point[0].y,
        (int)projectedTriangles.point[1].y,
        (int)projectedTriangles.point[2].y
      }, 3);
      graphics.setColor(Color.RED);
      graphics.drawPolygon(new int[] {
        (int)projectedTriangles.point[0].x,
        (int)projectedTriangles.point[1].x,
        (int)projectedTriangles.point[2].x
      }, new int[] {
        (int)projectedTriangles.point[0].y,
        (int)projectedTriangles.point[1].y,
        (int)projectedTriangles.point[2].y
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
    Matrix4x4 matrix = new Matrix4x4(new double[][] {
      {aspectRatio * fovRadians, 0, 0, 0},
      {0, fovRadians, 0, 0},
      {0, 0, far / (far - near), 1},
      {0, 0, (-far * near) / (far - near), 0}
    });

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

  public Matrix4x4 matrixPointAt(Vec3d pos, Vec3d target, Vec3d up) {
    Vec3d newForward = vectorSubtract(target, pos);
    newForward = vectorNormalize(newForward);

    Vec3d a = vectorMultiply(newForward, vectorDotProduct(up, newForward));
    Vec3d newUp = vectorSubtract(up, a);
    newUp = vectorNormalize(newUp);

    Vec3d newRight = vectorCrossProduct(newUp, newForward);

    Matrix4x4 matrix = new Matrix4x4(new double[][] {
      {newRight.x, newRight.y, newRight.z, 0},
      {newUp.x, newUp.y, newUp.z, 0},
      {newForward.x, newForward.y, newForward.z, 0},
      {pos.x, pos.y, pos.z, 1}
    });

    return matrix;
  }

  public Matrix4x4 matrixQuickInverse(Matrix4x4 m) {
    Matrix4x4 matrix = new Matrix4x4();
    matrix.matrix[0][0] = m.matrix[0][0];
    matrix.matrix[0][1] = m.matrix[1][0];
    matrix.matrix[0][2] = m.matrix[2][0];
    matrix.matrix[0][3] = 0;
    matrix.matrix[1][0] = m.matrix[0][1];
    matrix.matrix[1][1] = m.matrix[1][1];
    matrix.matrix[1][2] = m.matrix[2][1];
    matrix.matrix[1][3] = 0;
    matrix.matrix[2][0] = m.matrix[0][2];
    matrix.matrix[2][1] = m.matrix[1][2];
    matrix.matrix[2][2] = m.matrix[2][2];
    matrix.matrix[2][3] = 0;
    matrix.matrix[3][0] = -(m.matrix[3][0] * matrix.matrix[0][0] + m.matrix[3][1] * matrix.matrix[1][0] + m.matrix[3][2] * matrix.matrix[2][0]);
    matrix.matrix[3][1] = -(m.matrix[3][0] * matrix.matrix[0][1] + m.matrix[3][1] * matrix.matrix[1][1] + m.matrix[3][2] * matrix.matrix[2][1]);
    matrix.matrix[3][2] = -(m.matrix[3][0] * matrix.matrix[0][2] + m.matrix[3][1] * matrix.matrix[1][2] + m.matrix[3][2] * matrix.matrix[2][2]);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public Vec3d vectorIntersectPlane(Vec3d planeP, Vec3d planeN, Vec3d lineStart, Vec3d lineEnd) {
    planeN = vectorNormalize(planeN);
    double planeD = -vectorDotProduct(planeN, planeP);
    double ad = vectorDotProduct(lineStart, planeN);
    double bd = vectorDotProduct(lineEnd, planeN);
    double t = (-planeD - ad) / (bd - ad);
    Vec3d lineStartToEnd = vectorSubtract(lineEnd, lineStart);
    Vec3d lineToIntersect = vectorMultiply(lineStartToEnd, t);

    return vectorAdd(lineStart, lineToIntersect);
  }

  public int triangleClipAgainstPlane(Vec3d planeP, Vec3d planeN, Triangle inTriangle, Triangle outTriangle1, Triangle outTriangle2) {
    planeN = vectorNormalize(planeN);
    Vec3d[] insidePoints = new Vec3d[3];
    insidePoints[0] = new Vec3d();
    insidePoints[1] = new Vec3d();
    insidePoints[2] = new Vec3d();
    int insidePointCount = 0;
    Vec3d[] outsidePoints = new Vec3d[3];
    outsidePoints[0] = new Vec3d();
    outsidePoints[1] = new Vec3d();
    outsidePoints[2] = new Vec3d();
    int outsidePointCount = 0;

    double d0 = planeN.x * inTriangle.point[0].x + planeN.y * inTriangle.point[0].y + planeN.z * inTriangle.point[0].z - vectorDotProduct(planeN, planeP);
    double d1 = planeN.x * inTriangle.point[1].x + planeN.y * inTriangle.point[1].y + planeN.z * inTriangle.point[1].z - vectorDotProduct(planeN, planeP);
    double d2 = planeN.x * inTriangle.point[2].x + planeN.y * inTriangle.point[2].y + planeN.z * inTriangle.point[2].z - vectorDotProduct(planeN, planeP);

    if (d0 >= 0) {
      insidePoints[insidePointCount++].set(inTriangle.point[0]);
    }
    else {
      outsidePoints[outsidePointCount++].set(inTriangle.point[0]);
    }
    if (d1 >= 0) {
      insidePoints[insidePointCount++].set(inTriangle.point[1]);
    }
    else {
      outsidePoints[outsidePointCount++].set(inTriangle.point[1]);
    }
    if (d2 >= 0) {
      insidePoints[insidePointCount++].set(inTriangle.point[2]);
    }
    else {
      outsidePoints[outsidePointCount++].set(inTriangle.point[2]);
    }

    if (insidePointCount == 0) {
      return 0;
    }
    else if (insidePointCount == 3) {
      outTriangle1.set(inTriangle);

      return 1;
    }

    else if (insidePointCount == 1 && outsidePointCount == 2) {
      outTriangle1.color = inTriangle.color;

      outTriangle1.point[0].set(insidePoints[0]);
      outTriangle1.point[1].set(vectorIntersectPlane(planeP, planeN, insidePoints[0], outsidePoints[0]));
      outTriangle1.point[2].set(vectorIntersectPlane(planeP, planeN, insidePoints[0], outsidePoints[1]));

      return 1;
    }

    else if (insidePointCount == 2 && outsidePointCount == 1) {
      outTriangle1.color = inTriangle.color;
      outTriangle2.color = inTriangle.color;

      outTriangle1.point[0].set(insidePoints[0]);
      outTriangle1.point[1].set(insidePoints[1]);
      outTriangle1.point[2].set(vectorIntersectPlane(planeP, planeN, insidePoints[0], outsidePoints[0]));

      outTriangle2.point[0].set(insidePoints[1]);
      outTriangle2.point[1].set(outTriangle1.point[2]);
      outTriangle2.point[2].set(vectorIntersectPlane(planeP, planeN, insidePoints[1], outsidePoints[0]));

      return 2;
    }

    return 0;
  }

  public void getKeys() {
    if (pressedKeys.contains(KeyEvent.VK_UP)) {
      camera.y += 0.1;
    }

    if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
      camera.y -= 0.1;
    }

    if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
      camera.x -= 0.1;
    }

    if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
      camera.x += 0.1;
    }

    Vec3d forward = vectorMultiply(lookDirection, 0.1);

    if (pressedKeys.contains(KeyEvent.VK_W)) {
      camera = vectorAdd(camera, forward);
    }

    if (pressedKeys.contains(KeyEvent.VK_S)) {
      camera = vectorSubtract(camera, forward);
    }

    if (pressedKeys.contains(KeyEvent.VK_A)) {
      yaw -= 0.005;
    }
    
    if (pressedKeys.contains(KeyEvent.VK_D)) {
      yaw += 0.005;
    }
  }

  public void keyPressed(KeyEvent event) {
    if (!pressedKeys.contains(event.getKeyCode())) {
      pressedKeys.add(event.getKeyCode());
    }
  }
  public void keyReleased(KeyEvent event) {
    if (pressedKeys.contains(event.getKeyCode())) {
      pressedKeys.remove(Integer.valueOf(event.getKeyCode()));
    }
  }
  public void keyTyped(KeyEvent event) {}

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

    public void set(Vec3d vector) {
      x = vector.x;
      y = vector.y;
      z = vector.z;
      w = vector.w;
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

    public Triangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, short color) {
      point[0] = new Vec3d(x1, y1, z1);
      point[1] = new Vec3d(x2, y2, z2);
      point[2] = new Vec3d(x3, y3, z3);
      this.color = color;
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