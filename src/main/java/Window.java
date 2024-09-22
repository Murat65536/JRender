package src.main.java;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Window extends JPanel implements ActionListener {
  private BufferedImage bufferedImage;
  private final JLabel jLabel = new JLabel();
  private final Timer timer = new Timer(0, this);
  private ArrayList<Mesh> meshes = new ArrayList<>();
  public static Vec3d camera = new Vec3d();
  public static Vec3d lookDirection = new Vec3d();
  public static Vec3d strafeDirection = new Vec3d();
  public static float pitch = 0;
  public static float yaw = 0;
  private FPS fps = new FPS();
  private Graphics2D graphics;
  private float[][] depthBuffer = new float[Main.height][Main.width];

  public Window() {
    super(true);
    resizeBufferedImage();
    this.setLayout(new GridLayout());
    timer.start();
    meshes.add(new Mesh("cube.obj"));
    meshes.add(new Mesh("cube1.obj"));
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    resizeBufferedImage();
    graphics = bufferedImage.createGraphics();
    Matrix projection = Matrix.projection(Main.FOV, (float)Main.height / Main.width, 0.1f, 1000);
    Keys.getKeys(fps);
    pitch = Mouse.getPitch();
    yaw = Mouse.getYaw();
    Matrix worldMatrix = Matrix.translation(0, 0, 10);
    Vec3d up = new Vec3d(0, 1, 0);
    Vec3d target = new Vec3d(0, 0, 1);
    Vec3d strafe = new Vec3d(-1, 0, 0);
    Matrix rotationX = Matrix.rotationX(pitch);
    Matrix rotationY = Matrix.rotationY(yaw);
    Matrix cameraRotation = Matrix.multiply(rotationX, rotationY);
    lookDirection = Vec3d.multiplyMatrix(cameraRotation, target);
    strafeDirection = Vec3d.multiplyMatrix(cameraRotation, strafe);
    target = Vec3d.add(camera, lookDirection);
    Matrix cameraMatrix = Matrix.pointAt(camera, target, up);

    Matrix viewMatrix = Matrix.inverse(cameraMatrix);
    
    ArrayList<Triangle> trianglesToRaster = new ArrayList<>();

    for (Mesh mesh : meshes) {
      for (Triangle triangle : mesh.getTriangles()) {
        Triangle transformedTriangle = new Triangle(
          Vec3d.multiplyMatrix(worldMatrix, triangle.getPoints()[0]),
          Vec3d.multiplyMatrix(worldMatrix, triangle.getPoints()[1]),
          Vec3d.multiplyMatrix(worldMatrix, triangle.getPoints()[2]),
          triangle.getTextures()[0],
          triangle.getTextures()[1],
          triangle.getTextures()[2],
          triangle.getColor(),
          triangle.getNormalIndices(),
          triangle.getSprite()
        );

          Vec3d line1 = Vec3d.subtract(transformedTriangle.getPoints()[1], transformedTriangle.getPoints()[0]);
          Vec3d line2 = Vec3d.subtract(transformedTriangle.getPoints()[2], transformedTriangle.getPoints()[0]);
          Vec3d normal = Vec3d.normalize(Vec3d.crossProduct(line1, line2));
          Vec3d cameraRay = Vec3d.subtract(transformedTriangle.getPoints()[0], camera);

          if (Vec3d.dotProduct(normal, cameraRay) < 0) {

            Vec3d lightDirection = new Vec3d(0, 1, -1);
            lightDirection = Vec3d.normalize(lightDirection);
            transformedTriangle.setColor((short)(Math.max(0.1f, Vec3d.dotProduct(lightDirection, normal)) * 255));

          Triangle triangleViewed = new Triangle(
            Vec3d.multiplyMatrix(viewMatrix, transformedTriangle.getPoints()[0]),
            Vec3d.multiplyMatrix(viewMatrix, transformedTriangle.getPoints()[1]),
            Vec3d.multiplyMatrix(viewMatrix, transformedTriangle.getPoints()[2]),
            transformedTriangle.getTextures()[0],
            transformedTriangle.getTextures()[1],
            transformedTriangle.getTextures()[2],
            transformedTriangle.getColor(),
            transformedTriangle.getNormalIndices(),
            transformedTriangle.getSprite()
          );

          Triangle[] clipped = new Triangle[] {new Triangle(), new Triangle()};
          byte clippedTriangles = Triangle.clipPlane(new Vec3d(0, 0, 0.1f), new Vec3d(0, 0, 1), triangleViewed, clipped[0], clipped[1]);
          for (byte j = 0; j < clippedTriangles; j++) {
            Triangle projectedTriangle = new Triangle(
              Vec3d.multiplyMatrix(projection, clipped[j].getPoints()[0]),
              Vec3d.multiplyMatrix(projection, clipped[j].getPoints()[1]),
              Vec3d.multiplyMatrix(projection, clipped[j].getPoints()[2]),
              clipped[j].getTextures()[0],
              clipped[j].getTextures()[1],
              clipped[j].getTextures()[2],
              clipped[j].getColor(),
              clipped[j].getNormalIndices(),
              clipped[j].getSprite()
            );

            projectedTriangle.setTextureU(0, projectedTriangle.getTextures()[0].getU() / projectedTriangle.getPoints()[0].getW());
            projectedTriangle.setTextureU(1, projectedTriangle.getTextures()[1].getU() / projectedTriangle.getPoints()[1].getW());
            projectedTriangle.setTextureU(2, projectedTriangle.getTextures()[2].getU() / projectedTriangle.getPoints()[2].getW());

            projectedTriangle.setTextureV(0, projectedTriangle.getTextures()[0].getV() / projectedTriangle.getPoints()[0].getW());
            projectedTriangle.setTextureV(1, projectedTriangle.getTextures()[1].getV() / projectedTriangle.getPoints()[1].getW());
            projectedTriangle.setTextureV(2, projectedTriangle.getTextures()[2].getV() / projectedTriangle.getPoints()[2].getW());

            projectedTriangle.setTextureW(0, 1f / projectedTriangle.getPoints()[0].getW());
            projectedTriangle.setTextureW(1, 1f / projectedTriangle.getPoints()[1].getW());
            projectedTriangle.setTextureW(2, 1f / projectedTriangle.getPoints()[2].getW());

            projectedTriangle.setPoint(0, Vec3d.divide(projectedTriangle.getPoints()[0], projectedTriangle.getPoints()[0].getW()));
            projectedTriangle.setPoint(1, Vec3d.divide(projectedTriangle.getPoints()[1], projectedTriangle.getPoints()[1].getW()));
            projectedTriangle.setPoint(2, Vec3d.divide(projectedTriangle.getPoints()[2], projectedTriangle.getPoints()[2].getW()));

            projectedTriangle.setPointX(0, projectedTriangle.getPoints()[0].getX() * -1);
            projectedTriangle.setPointX(1, projectedTriangle.getPoints()[1].getX() * -1);
            projectedTriangle.setPointX(2, projectedTriangle.getPoints()[2].getX() * -1);
            projectedTriangle.setPointY(0, projectedTriangle.getPoints()[0].getY() * -1);
            projectedTriangle.setPointY(1, projectedTriangle.getPoints()[1].getY() * -1);
            projectedTriangle.setPointY(2, projectedTriangle.getPoints()[2].getY() * -1);

            Vec3d offsetView = new Vec3d(1, 1, 0);
            projectedTriangle.setPoint(0, Vec3d.add(projectedTriangle.getPoints()[0], offsetView));
            projectedTriangle.setPoint(1, Vec3d.add(projectedTriangle.getPoints()[1], offsetView));
            projectedTriangle.setPoint(2, Vec3d.add(projectedTriangle.getPoints()[2], offsetView));

            projectedTriangle.setPointX(0, projectedTriangle.getPoints()[0].getX() * 0.5f * Main.width);
            projectedTriangle.setPointX(1, projectedTriangle.getPoints()[1].getX() * 0.5f * Main.width);
            projectedTriangle.setPointX(2, projectedTriangle.getPoints()[2].getX() * 0.5f * Main.width);
            projectedTriangle.setPointY(0, projectedTriangle.getPoints()[0].getY() * 0.5f * Main.height);
            projectedTriangle.setPointY(1, projectedTriangle.getPoints()[1].getY() * 0.5f * Main.height);
            projectedTriangle.setPointY(2, projectedTriangle.getPoints()[2].getY() * 0.5f * Main.height);
            trianglesToRaster.add(projectedTriangle.clone());
          }
        }
      }
    }

    // Not needed until transparent or translucent material is added.

    // Collections.sort(trianglesToRaster, (t1, t2) -> {
    //   float z1 = t1.point[0].getZ() + t1.point[1].getZ() + t1.point[2].getZ();
    //   float z2 = t2.point[0].getZ() + t2.point[1].getZ() + t2.point[2].getZ();

    //   if (z1 > z2) {
    //     return -1;
    //   }
    //   else if (z1 < z2) {
    //     return 1;
    //   }
    //   return 0;
    // });

    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, Main.width, Main.height);

    depthBuffer = new float[Main.height][Main.width];

    for (Triangle rasterizedTriangles : trianglesToRaster) {
      Triangle[] clipped = new Triangle[] {new Triangle(), new Triangle()};
      ArrayList<Triangle> triangleList = new ArrayList<>();
      triangleList.add(rasterizedTriangles);
      int newTriangles = 1;
      for (byte side = 0; side < 4; side++) {
        byte trianglesToAdd = 0;
        while (newTriangles > 0) {
          switch (side) {
            case 0:
              trianglesToAdd = Triangle.clipPlane(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), triangleList.get(0), clipped[0], clipped[1]);
              break;
            case 1:
              trianglesToAdd = Triangle.clipPlane(new Vec3d(0, Main.height - 1, 0), new Vec3d(0, -1, 0), triangleList.get(0), clipped[0], clipped[1]);
              break;
            case 2:
              trianglesToAdd = Triangle.clipPlane(new Vec3d(0, 0, 0), new Vec3d(1, 0, 0), triangleList.get(0), clipped[0], clipped[1]);
              break;
            case 3:
              trianglesToAdd = Triangle.clipPlane(new Vec3d(Main.width - 1, 0, 0), new Vec3d(-1, 0, 0), triangleList.get(0), clipped[0], clipped[1]);
              break;
          }

          triangleList.remove(0);
          newTriangles--;

          for (byte w = 0; w < trianglesToAdd; w++) {
            triangleList.add(clipped[w].clone());
          }
        }
        newTriangles = triangleList.size();
      }

      for (Triangle triangle : triangleList) {
        if (triangle.getSprite() == null) {
          graphics.setColor(new Color(triangle.getColor(), triangle.getColor(), triangle.getColor()));
          graphics.fillPolygon(new int[] {
            (short)triangle.getPoints()[0].getX(),
            (short)triangle.getPoints()[1].getX(),
            (short)triangle.getPoints()[2].getX()
          }, new int[] {
            (short)triangle.getPoints()[0].getY(),
            (short)triangle.getPoints()[1].getY(),
            (short)triangle.getPoints()[2].getY()
          }, 3);
        }
        else {
          textureTriangle(
            Math.round(triangle.getPoints()[0].getX()), Math.round(triangle.getPoints()[0].getY()), triangle.getTextures()[0].getU(), triangle.getTextures()[0].getV(), triangle.getTextures()[0].getW(),
            Math.round(triangle.getPoints()[1].getX()), Math.round(triangle.getPoints()[1].getY()), triangle.getTextures()[1].getU(), triangle.getTextures()[1].getV(), triangle.getTextures()[1].getW(),
            Math.round(triangle.getPoints()[2].getX()), Math.round(triangle.getPoints()[2].getY()), triangle.getTextures()[2].getU(), triangle.getTextures()[2].getV(), triangle.getTextures()[2].getW(),
            triangle.getSprite()
          );
        }
        // graphics.setColor(Color.WHITE);
        // graphics.drawPolygon(new int[] {
        //   (int)triangle.getPoints()[0].getX(),
        //   (int)triangle.getPoints()[1].getX(),
        //   (int)triangle.getPoints()[2].getX()
        // }, new int[] {
        //   (int)triangle.getPoints()[0].getY(),
        //   (int)triangle.getPoints()[1].getY(),
        //   (int)triangle.getPoints()[2].getY()
        // }, 3);
      }
    }
    graphics.setColor(Color.WHITE);
    graphics.drawString(Float.toString(fps.getFPS()), 100, 100);
    jLabel.repaint();
    fps.update();
  }

  private void resizeBufferedImage() {
    bufferedImage = new BufferedImage(Main.width, Main.height, BufferedImage.TYPE_INT_ARGB);
    jLabel.setIcon(new ImageIcon(bufferedImage));
    this.add(jLabel);
  }

  private void textureTriangle(int x1, int y1, float u1, float v1, float w1,
                                int x2, int y2, float u2, float v2, float w2,
                                int x3, int y3, float u3, float v3, float w3, Sprite sprite) {
    if (y2 < y1) {
      y1 += (y2 - (y2 = y1));
      x1 += (x2 - (x2 = x1));
      u1 += (u2 - (u2 = u1));
      v1 += (v2 - (v2 = v1));
      w1 += (w2 - (w2 = w1));
    }
    if (y3 < y1) {
      y1 += (y3 - (y3 = y1));
      x1 += (x3 - (x3 = x1));
      u1 += (u3 - (u3 = u1));
      v1 += (v3 - (v3 = v1));
      w1 += (w3 - (w3 = w1));
    }
    if (y3 < y2) {
      y2 += (y3 - (y3 = y2));
      x2 += (x3 - (x3 = x2));
      u2 += (u3 - (u3 = u2));
      v2 += (v3 - (v3 = v2));
      w2 += (w3 - (w3 = w2));
    }
    int dx1 = x2 - x1;
    int dy1 = y2 - y1;
    float du1 = u2 - u1;
    float dv1 = v2 - v1;
    float dw1 = w2 - w1;

    int dx2 = x3 - x1;
    int dy2 = y3 - y1;
    float du2 = u3 - u1;
    float dv2 = v3 - v1;
    float dw2 = w3 - w1;
  
    float textureU;
    float textureV;
    float textureW;
  
    float daxStep = 0;
    float dbxStep = 0;

    float du1Step = 0;
    float dv1Step = 0;
    float dw1Step = 0;

    float du2Step = 0;
    float dv2Step = 0;
    float dw2Step = 0;
  
    if (dy1 != 0) {
      daxStep = dx1 / (float)Math.abs(dy1);
      du1Step = du1 / (float)Math.abs(dy1);
      dv1Step = dv1 / (float)Math.abs(dy1);
      dw1Step = dw1 / (float)Math.abs(dy1);
    }
    if (dy2 != 0) {
      dbxStep = dx2 / (float)Math.abs(dy2);
      du2Step = du2 / (float)Math.abs(dy2);
      dv2Step = dv2 / (float)Math.abs(dy2);
      dw2Step = dw2 / (float)Math.abs(dy2);
    }
  
    if (dy1 != 0) {
      for (int y = y1; y <= y2; y++) {
        int ax = Math.round(x1 + (float)(y - y1) * daxStep);
        int bx = Math.round(x1 + (float)(y - y1) * dbxStep);
  
        float textureSU = u1 + (float)(y - y1) * du1Step;
        float textureSV = v1 + (float)(y - y1) * dv1Step;
        float textureSW = w1 + (float)(y - y1) * dw1Step;
        
        float textureEU = u1 + (float)(y - y1) * du2Step;
        float textureEV = v1 + (float)(y - y1) * dv2Step;
        float textureEW = w1 + (float)(y - y1) * dw2Step;
  
        if (ax > bx) {
          ax += (bx - (bx = ax));
          textureSU += (textureEU - (textureEU = textureSU));
          textureSV += (textureEV - (textureEV = textureSV));
          textureSW += (textureEW - (textureEW = textureSW));
        }

        float tStep = 1f / (float)(bx - ax);
        float t = 0f;

        for (int x = ax; x < bx; x++) {
          textureU = (1f - t) * textureSU + t * textureEU;
          textureV = (1f - t) * textureSV + t * textureEV;
          textureW = (1f - t) * textureSW + t * textureEW;
          if (textureW > depthBuffer[y][x]) {
            bufferedImage.setRGB(x, y, sprite.getPixelColor(Math.round((sprite.lengthX() - 1) * (textureU / textureW)), Math.round((sprite.lengthY() - 1) * (textureV / textureW))));
            depthBuffer[y][x] = textureW;
          }
          t += tStep;
        }
      }
    }
    dx1 = x3 - x2;
    dy1 = y3 - y2;
    du1 = u3 - u2;
    dv1 = v3 - v2;
    dw1 = w3 - w2;

    du1Step = 0;
    dv1Step = 0;

    if (dy1 != 0) {
      daxStep = dx1 / (float)Math.abs(dy1);
      du1Step = du1 / (float)Math.abs(dy1);
      dv1Step = dv1 / (float)Math.abs(dy1);
      dw1Step = dw1 / (float)Math.abs(dy1);
    }
    if (dy2 != 0) {
      dbxStep = dx2 / (float)Math.abs(dy2);
    }

    if (dy1 != 0) {
      for (int y = y2; y <= y3; y++) {
        int ax = Math.round(x2 + (float)(y - y2) * daxStep);
        int bx = Math.round(x1 + (float)(y - y1) * dbxStep);
  
        float textureSU = u2 + (float)(y - y2) * du1Step;
        float textureSV = v2 + (float)(y - y2) * dv1Step;
        float textureSW = w2 + (float)(y - y2) * dw1Step;
  
        float textureEU = u1 + (float)(y - y1) * du2Step;
        float textureEV = v1 + (float)(y - y1) * dv2Step;
        float textureEW = w1 + (float)(y - y1) * dw2Step;
  
        if (ax > bx) {
          ax += (bx - (bx = ax));
          textureSU += (textureEU - (textureEU = textureSU));
          textureSV += (textureEV - (textureEV = textureSV));
          textureSW += (textureEW - (textureEW = textureSW));
  
        }
  
        float tStep = 1f / (float)(bx - ax);
        float t = 0f;
  
        for (int x = ax; x < bx; x++) {
          textureU = (1f - t) * textureSU + t * textureEU;
          textureV = (1f - t) * textureSV + t * textureEV;
          textureW = (1f - t) * textureSW + t * textureEW;
          if (textureW > depthBuffer[y][x]) {
            bufferedImage.setRGB(x, y, sprite.getPixelColor(Math.round((sprite.lengthX() - 1) * (textureU / textureW)), Math.round((sprite.lengthY() - 1) * (textureV / textureW))));
            depthBuffer[y][x] = textureW;
          }
          t += tStep;
        }
      }
    }
  }
}