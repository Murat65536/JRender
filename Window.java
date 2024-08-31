import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Window extends JPanel implements ActionListener {
  private final int WIDTH = 512;
  private final int HEIGHT = 512;
  private final double MOUSE_SENSITIVITY = 2;
  private final double MOVEMENT_SPEED = 160;
  private final double FOV = 90;
  private final BufferedImage bufferedImage;
  private final JLabel jLabel = new JLabel();
  private final Timer timer = new Timer(10, this);
  private static final Mesh mesh = new Mesh();
  private final Matrix projectionMatrix = projectionMatrix(FOV, (double)HEIGHT / WIDTH, 0.1, 1000);
  private Vec3d camera = new Vec3d();
  private Vec3d lookDirection = new Vec3d();
  private Vec3d sideDirection = new Vec3d();
  private double yaw = 0;
  private FPS fps = new FPS();

  protected Window() {
    super(true);
    this.setLayout(new GridLayout());
    this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    jLabel.setIcon(new ImageIcon(bufferedImage));
    this.add(jLabel);
    timer.start();
    mesh.load("assets/mountains.obj");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    // System.out.println(fps.getFPS());
    getKeys();
    getMouse();
    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, WIDTH, HEIGHT);
    Matrix worldMatrix = translationMatrix(0, -10, 0);
    Vec3d up = new Vec3d(0, 1, 0);
    Vec3d target = new Vec3d(0, 0, 1);
    Matrix rotationMatrix = rotationMatrixY(yaw);
    lookDirection = multiplyMatrixVector(rotationMatrix, target);
    sideDirection = multiplyMatrixVector(rotationMatrix, new Vec3d(-1, 0, 0));
    target = addVector(camera, lookDirection);
    Matrix cameraMatrix = matrixPoint(camera, target, up);
    Matrix viewMatrix = quickInverseMatrix(cameraMatrix);
    ArrayList<Triangle> trianglesToRaster = new ArrayList<Triangle>();

    for (int i = 0; i < mesh.triangles.size(); i++) {
      Triangle projectedTriangle = new Triangle();
      Triangle transformedTriangle = new Triangle();
      Triangle triangleViewed = new Triangle();
      
      transformedTriangle.point[0] = multiplyMatrixVector(worldMatrix, mesh.triangles.get(i).point[0]);
      transformedTriangle.point[1] = multiplyMatrixVector(worldMatrix, mesh.triangles.get(i).point[1]);
      transformedTriangle.point[2] = multiplyMatrixVector(worldMatrix, mesh.triangles.get(i).point[2]);

      Vec3d normal = new Vec3d();
      Vec3d line1 = new Vec3d();
      Vec3d line2 = new Vec3d();

      line1 = subtractVector(transformedTriangle.point[1], transformedTriangle.point[0]);
      line2 = subtractVector(transformedTriangle.point[2], transformedTriangle.point[0]);

      normal = vectorCrossProduct(line1, line2);
      normal = normalizeVector(normal);

      double normalLength = Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
      normal.x /= normalLength;
      normal.y /= normalLength;
      normal.z /= normalLength;

      Vec3d cameraRay = subtractVector(transformedTriangle.point[0], camera);

      if (vectorDotProduct(normal, cameraRay) < 0) {

        Vec3d lightDirection = new Vec3d(0, 0, -1);
        lightDirection = normalizeVector(lightDirection);
        double dotProduct = Math.max(0.1, vectorDotProduct(lightDirection, normal));
        transformedTriangle.color = (short)(dotProduct * 255);

        triangleViewed.point[0] = multiplyMatrixVector(viewMatrix, transformedTriangle.point[0]);
        triangleViewed.point[1] = multiplyMatrixVector(viewMatrix, transformedTriangle.point[1]);
        triangleViewed.point[2] = multiplyMatrixVector(viewMatrix, transformedTriangle.point[2]);
        triangleViewed.color = transformedTriangle.color;
        
        int clippedTriangles = 0;
        Triangle[] clipped = new Triangle[2];
        clipped[0] = new Triangle();
        clipped[1] = new Triangle();
        clippedTriangles = trianglePlaneClip(new Vec3d(0, 0, 0.1), new Vec3d(0, 0, 1), triangleViewed, clipped[0], clipped[1]);
        for (int j = 0; j < clippedTriangles; j++) {
          projectedTriangle.point[0] = multiplyMatrixVector(projectionMatrix, clipped[j].point[0]);
          projectedTriangle.point[1] = multiplyMatrixVector(projectionMatrix, clipped[j].point[1]);
          projectedTriangle.point[2] = multiplyMatrixVector(projectionMatrix, clipped[j].point[2]);
          projectedTriangle.color = clipped[j].color;

          projectedTriangle.point[0] = divideVector(projectedTriangle.point[0], projectedTriangle.point[0].w);
          projectedTriangle.point[1] = divideVector(projectedTriangle.point[1], projectedTriangle.point[1].w);
          projectedTriangle.point[2] = divideVector(projectedTriangle.point[2], projectedTriangle.point[2].w);
        
          projectedTriangle.point[0].x *= -1;
          projectedTriangle.point[1].x *= -1;
          projectedTriangle.point[2].x *= -1;
          projectedTriangle.point[0].y *= -1;
          projectedTriangle.point[1].y *= -1;
          projectedTriangle.point[2].y *= -1;

          Vec3d offsetView = new Vec3d(1, 1, 0);
          projectedTriangle.point[0] = addVector(projectedTriangle.point[0], offsetView);
          projectedTriangle.point[1] = addVector(projectedTriangle.point[1], offsetView);
          projectedTriangle.point[2] = addVector(projectedTriangle.point[2], offsetView);

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

    for (Triangle rasterizedTriangles : trianglesToRaster) {
      Triangle[] clipped = new Triangle[2];
      clipped[0] = new Triangle();
      clipped[1] = new Triangle();
      ArrayList<Triangle> triangleList = new ArrayList<Triangle>();
      triangleList.add(rasterizedTriangles);
      int newTriangles = 1;
      for (int p = 0; p < 4; p++) {
        int trianglesToAdd = 0;
        while (newTriangles > 0) {
          Triangle test = triangleList.get(0);
          triangleList.remove(0);
          newTriangles--;

          switch (p) {
            case 0:
              trianglesToAdd = trianglePlaneClip(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), test, clipped[0], clipped[1]);
              break;
            case 1:
              trianglesToAdd = trianglePlaneClip(new Vec3d(0, HEIGHT - 1, 0), new Vec3d(0, -1, 0), test, clipped[0], clipped[1]);
              break;
            case 2:
              trianglesToAdd = trianglePlaneClip(new Vec3d(0, 0, 0), new Vec3d(1, 0, 0), test, clipped[0], clipped[1]);
              break;
            case 3:
              trianglesToAdd = trianglePlaneClip(new Vec3d(WIDTH - 1, 0, 0), new Vec3d(-1, 0, 0), test, clipped[0], clipped[1]);
              break;
          }

          for (int w = 0; w < trianglesToAdd; w++) {
            triangleList.add(clipped[w].clone());
          }
        }
        newTriangles = triangleList.size();
      }

      for (Triangle triangle : triangleList) {
        graphics.setColor(new Color(triangle.color, triangle.color, triangle.color));
        graphics.fillPolygon(new int[] {
          (int)triangle.point[0].x,
          (int)triangle.point[1].x,
          (int)triangle.point[2].x
        }, new int[] {
          (int)triangle.point[0].y,
          (int)triangle.point[1].y,
          (int)triangle.point[2].y
        }, 3);
        // graphics.setColor(Color.RED);
        // graphics.drawPolygon(new int[] {
        //   (int)triangle.point[0].x,
        //   (int)triangle.point[1].x,
        //   (int)triangle.point[2].x
        // }, new int[] {
        //   (int)triangle.point[0].y,
        //   (int)triangle.point[1].y,
        //   (int)triangle.point[2].y
        // }, 3);
      }
    }

    graphics.dispose();
    jLabel.repaint();
    fps.update();
  }

  private Vec3d multiplyMatrixVector(Matrix m, Vec3d i) {
    return new Vec3d(i.x * m.matrix[0][0] + i.y * m.matrix[1][0] + i.z * m.matrix[2][0] + i.w * m.matrix[3][0],
                     i.x * m.matrix[0][1] + i.y * m.matrix[1][1] + i.z * m.matrix[2][1] + i.w * m.matrix[3][1],
                     i.x * m.matrix[0][2] + i.y * m.matrix[1][2] + i.z * m.matrix[2][2] + i.w * m.matrix[3][2],
                     i.x * m.matrix[0][3] + i.y * m.matrix[1][3] + i.z * m.matrix[2][3] + i.w * m.matrix[3][3]);
  }

  private Vec3d addVector(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
  }

  private Vec3d subtractVector(Vec3d v1, Vec3d v2) {
    return new Vec3d(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
  }

  private Vec3d multiplyVector(Vec3d v1, double k) {
    return new Vec3d(v1.x * k, v1.y * k, v1.z * k);
  }

  private Vec3d divideVector(Vec3d v1, double k) {
    return new Vec3d(v1.x / k, v1.y / k, v1.z / k);
  }

  private double vectorDotProduct(Vec3d v1, Vec3d v2) {
    return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
  }

  private double vectorLength(Vec3d v) {
    return Math.sqrt(vectorDotProduct(v, v));
  }

  private Vec3d normalizeVector(Vec3d v) {
    double l = vectorLength(v);
    return new Vec3d(v.x / l, v.y / l, v.z / l);
  }

  private Vec3d vectorCrossProduct(Vec3d v1, Vec3d v2) {
    Vec3d v = new Vec3d();
    v.x = v1.y * v2.z - v1.z * v2.y;
    v.y = v1.z * v2.x - v1.x * v2.z;
    v.z = v1.x * v2.y - v1.y * v2.x;

    return v;
  }

  @SuppressWarnings("unused")
  private Matrix rotationMatrixX(double angle) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = 1;
    matrix.matrix[1][1] = Math.cos(angle);
    matrix.matrix[1][2] = Math.sin(angle);
    matrix.matrix[2][1] = -Math.sin(angle);
    matrix.matrix[2][2] = Math.cos(angle);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  private Matrix rotationMatrixY(double angle) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = Math.cos(angle);
    matrix.matrix[0][2] = Math.sin(angle);
    matrix.matrix[2][0] = -Math.sin(angle);
    matrix.matrix[1][1] = 1;
    matrix.matrix[2][2] = Math.cos(angle);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  @SuppressWarnings("unused")
  private Matrix rotationMatrixZ(double angle) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = Math.cos(angle);
    matrix.matrix[0][1] = Math.sin(angle);
    matrix.matrix[1][0] = -Math.sin(angle);
    matrix.matrix[1][1] = Math.cos(angle);
    matrix.matrix[2][2] = 1;
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  private Matrix translationMatrix(double x, double y, double z) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = 1;
    matrix.matrix[1][1] = 1;
    matrix.matrix[2][2] = 1;
    matrix.matrix[3][3] = 1;
    matrix.matrix[3][0] = x;
    matrix.matrix[3][1] = y;
    matrix.matrix[3][2] = z;

    return matrix;
  }

  private Matrix projectionMatrix(double fov, double aspectRatio, double near, double far) {
    double fovRadians = 1 / Math.tan(Math.toRadians(fov * 0.5));
    Matrix matrix = new Matrix(new double[][] {
      {aspectRatio * fovRadians, 0, 0, 0},
      {0, fovRadians, 0, 0},
      {0, 0, far / (far - near), 1},
      {0, 0, (-far * near) / (far - near), 0}
    });

    return matrix;
  }

  private Matrix matrixPoint(Vec3d pos, Vec3d target, Vec3d up) {
    Vec3d newForward = subtractVector(target, pos);
    newForward = normalizeVector(newForward);

    Vec3d a = multiplyVector(newForward, vectorDotProduct(up, newForward));
    Vec3d newUp = subtractVector(up, a);
    newUp = normalizeVector(newUp);

    Vec3d newRight = vectorCrossProduct(newUp, newForward);

    Matrix matrix = new Matrix(new double[][] {
      {newRight.x, newRight.y, newRight.z, 0},
      {newUp.x, newUp.y, newUp.z, 0},
      {newForward.x, newForward.y, newForward.z, 0},
      {pos.x, pos.y, pos.z, 1}
    });

    return matrix;
  }

  private Matrix quickInverseMatrix(Matrix m) {
    Matrix matrix = new Matrix();
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

  private Vec3d vectorPlaneIntersect(Vec3d planeP, Vec3d planeN, Vec3d lineStart, Vec3d lineEnd) {
    planeN = normalizeVector(planeN);
    double planeD = -vectorDotProduct(planeN, planeP);
    double ad = vectorDotProduct(lineStart, planeN);
    double bd = vectorDotProduct(lineEnd, planeN);
    double t = (-planeD - ad) / (bd - ad);
    Vec3d lineStartToEnd = subtractVector(lineEnd, lineStart);
    Vec3d lineToIntersect = multiplyVector(lineStartToEnd, t);

    return addVector(lineStart, lineToIntersect);
  }

  private int trianglePlaneClip(Vec3d planeP, Vec3d planeN, Triangle inTriangle, Triangle outTriangle1, Triangle outTriangle2) {
    planeN = normalizeVector(planeN);
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
      outTriangle1.point[1].set(vectorPlaneIntersect(planeP, planeN, insidePoints[0], outsidePoints[0]));
      outTriangle1.point[2].set(vectorPlaneIntersect(planeP, planeN, insidePoints[0], outsidePoints[1]));

      return 1;
    }

    else if (insidePointCount == 2 && outsidePointCount == 1) {
      outTriangle1.color = inTriangle.color;
      outTriangle2.color = inTriangle.color;

      outTriangle1.point[0].set(insidePoints[0]);
      outTriangle1.point[1].set(insidePoints[1]);
      outTriangle1.point[2].set(vectorPlaneIntersect(planeP, planeN, insidePoints[0], outsidePoints[0]));

      outTriangle2.point[0].set(insidePoints[1]);
      outTriangle2.point[1].set(outTriangle1.point[2]);
      outTriangle2.point[2].set(vectorPlaneIntersect(planeP, planeN, insidePoints[1], outsidePoints[0]));

      return 2;
    }

    return 0;
  }

  private void getKeys() {
    Vec3d forward = multiplyVector(lookDirection, MOVEMENT_SPEED / fps.getFPS());
    Vec3d side = multiplyVector(sideDirection, MOVEMENT_SPEED / fps.getFPS());

    if (Keys.pressedKeys.contains(KeyEvent.VK_W)) {
      camera = addVector(camera, forward);
    }
    
    if (Keys.pressedKeys.contains(KeyEvent.VK_S)) {
      camera = subtractVector(camera, forward);
    }

    if (Keys.pressedKeys.contains(KeyEvent.VK_A)) {
      camera = subtractVector(camera, side);
    }
    
    if (Keys.pressedKeys.contains(KeyEvent.VK_D)) {
      camera = addVector(camera, side);
    }
    
    if (Keys.pressedKeys.contains(KeyEvent.VK_SPACE)) {
      camera.y += MOVEMENT_SPEED / fps.getFPS();
    }
    
    if (Keys.pressedKeys.contains(KeyEvent.VK_SHIFT)) {
      camera.y -= MOVEMENT_SPEED / fps.getFPS();
    }

  }
  
  private void getMouse() {
    yaw = Mouse.origin.x * MOUSE_SENSITIVITY / WIDTH;
  }
}