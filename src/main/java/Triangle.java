package src.main.java;

public class Triangle {
  public Vec3d[] point = new Vec3d[3];
  public Vec2d[] texture = new Vec2d[3];
  public int[] textureCoords = new int[3];
  public int[] normalIndices = new int[3];
  public short color = 0;

  public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, short color) {
    point[0] = new Vec3d(x1, y1, z1);
    point[1] = new Vec3d(x2, y2, z2);
    point[2] = new Vec3d(x3, y3, z3);
    texture[0] = new Vec2d();
    texture[1] = new Vec2d();
    texture[2] = new Vec2d();
    this.color = color;
  }

  public Triangle(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2, float x3, float y3, float z3, float w3, short color) {
    point[0] = new Vec3d(x1, y1, z1, w1);
    point[1] = new Vec3d(x2, y2, z2, w2);
    point[2] = new Vec3d(x3, y3, z3, w3);
    texture[0] = new Vec2d();
    texture[1] = new Vec2d();
    texture[2] = new Vec2d();
    this.color = color;
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3) {
    point[0] = point1.clone();
    point[1] = point2.clone();
    point[2] = point3.clone();
    texture[0] = new Vec2d();
    texture[1] = new Vec2d();
    texture[2] = new Vec2d();
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, Vec2d texture1, Vec2d texture2, Vec2d texture3) {
    point[0] = point1.clone();
    point[1] = point2.clone();
    point[2] = point3.clone();
    texture[0] = texture1.clone();
    texture[1] = texture2.clone();
    texture[2] = texture3.clone();
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, Vec2d texture1, Vec2d texture2, Vec2d texture3, short color) {
    point[0] = point1.clone();
    point[1] = point2.clone();
    point[2] = point3.clone();
    texture[0] = texture1.clone();
    texture[1] = texture2.clone();
    texture[2] = texture3.clone();
    this.color = color;
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, int[] normalIndices) {
    point[0] = point1.clone();
    point[1] = point2.clone();
    point[2] = point3.clone();
    texture[0] = new Vec2d();
    texture[1] = new Vec2d();
    texture[2] = new Vec2d();
    this.normalIndices = normalIndices;
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, Vec2d texture1, Vec2d texture2, Vec2d texture3, int[] normalIndices) {
    point[0] = point1.clone();
    point[1] = point2.clone();
    point[2] = point3.clone();
    texture[0] = texture1.clone();
    texture[1] = texture2.clone();
    texture[2] = texture3.clone();
    this.normalIndices = normalIndices;
  }

  public Triangle() {
    point[0] = new Vec3d();
    point[1] = new Vec3d();
    point[2] = new Vec3d();
    texture[0] = new Vec2d();
    texture[1] = new Vec2d();
    texture[2] = new Vec2d();
  }

  public Triangle clone() {
    return new Triangle(point[0], point[1], point[2], texture[0], texture[1], texture[2], color);
  }
  public void set(Triangle triangle) {
    point[0].set(triangle.point[0]);
    point[1].set(triangle.point[1]);
    point[2].set(triangle.point[2]);
    texture[0].set(triangle.texture[0]);
    texture[1].set(triangle.texture[1]);
    texture[2].set(triangle.texture[2]);
    color = triangle.color;
    normalIndices = triangle.normalIndices;
  }

  public void setColor(Triangle triangle) {
    color = triangle.color;
  }

  public static byte clipPlane(Vec3d planeP, Vec3d planeN, Triangle inTriangle, Triangle outTriangle1, Triangle outTriangle2) {
    planeN = Vec3d.normalize(planeN);
    
    Vec3d[] insidePoints = new Vec3d[3];
    byte insidePointCount = 0;
    Vec3d[] outsidePoints = new Vec3d[3];
    byte outsidePointCount = 0;
    Vec2d[] insideTextures = new Vec2d[3];
    byte insideTexturesCount = 0;
    Vec2d[] outsideTextures = new Vec2d[3];
    byte outsideTexturesCount = 0;

    float d0 = planeN.getX() * inTriangle.point[0].getX() + planeN.getY() * inTriangle.point[0].getY() + planeN.getZ() * inTriangle.point[0].getZ() - Vec3d.dotProduct(planeN, planeP);
    float d1 = planeN.getX() * inTriangle.point[1].getX() + planeN.getY() * inTriangle.point[1].getY() + planeN.getZ() * inTriangle.point[1].getZ() - Vec3d.dotProduct(planeN, planeP);
    float d2 = planeN.getX() * inTriangle.point[2].getX() + planeN.getY() * inTriangle.point[2].getY() + planeN.getZ() * inTriangle.point[2].getZ() - Vec3d.dotProduct(planeN, planeP);

    if (d0 >= 0) {
      insidePoints[insidePointCount++] = inTriangle.point[0];
      insideTextures[insideTexturesCount++] = inTriangle.texture[0];
    }
    else {
      outsidePoints[outsidePointCount++] = inTriangle.point[0];
      outsideTextures[outsideTexturesCount++] = inTriangle.texture[0];
    }
    if (d1 >= 0) {
      insidePoints[insidePointCount++] = inTriangle.point[1];
      insideTextures[insideTexturesCount++] = inTriangle.texture[1];
    }
    else {
      outsidePoints[outsidePointCount++] = inTriangle.point[1];
      outsideTextures[outsideTexturesCount++] = inTriangle.texture[1];
    }
    if (d2 >= 0) {
      insidePoints[insidePointCount++] = inTriangle.point[2];
      insideTextures[insideTexturesCount++] = inTriangle.texture[2];
    }
    else {
      outsidePoints[outsidePointCount++] = inTriangle.point[2];
      outsideTextures[outsideTexturesCount++] = inTriangle.texture[2];
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
      outTriangle1.texture[0].set(insideTextures[0]);
      Texture texture = new Texture();
      outTriangle1.point[1] = Vec3d.planeIntersect(planeP, planeN, insidePoints[0], outsidePoints[0], texture);
      outTriangle1.texture[1].setU(texture.getTexture() * (outsideTextures[0].getU() - insideTextures[0].getU()) + insideTextures[0].getU());
      outTriangle1.texture[1].setV(texture.getTexture() * (outsideTextures[0].getV() - insideTextures[0].getV()) + insideTextures[0].getV());
      outTriangle1.texture[1].setW(texture.getTexture() * (outsideTextures[0].getW() - insideTextures[0].getW()) + insideTextures[0].getW());
      outTriangle1.point[2] = Vec3d.planeIntersect(planeP, planeN, insidePoints[0], outsidePoints[1], texture);
      outTriangle1.texture[2].setU(texture.getTexture() * (outsideTextures[1].getU() - insideTextures[0].getU()) + insideTextures[0].getU());
      outTriangle1.texture[2].setV(texture.getTexture() * (outsideTextures[1].getV() - insideTextures[0].getV()) + insideTextures[0].getV());
      outTriangle1.texture[2].setW(texture.getTexture() * (outsideTextures[1].getW() - insideTextures[0].getW()) + insideTextures[0].getW());

      return 1;
    }

    else if (insidePointCount == 2 && outsidePointCount == 1) {
      outTriangle1.color = inTriangle.color;
      outTriangle2.color = inTriangle.color;

      outTriangle1.point[0].set(insidePoints[0]);
      outTriangle1.texture[0].set(insideTextures[0]);
      outTriangle1.point[1].set(insidePoints[1]);
      outTriangle1.texture[1].set(insideTextures[1]);
      Texture texture = new Texture();
      outTriangle1.point[2] = Vec3d.planeIntersect(planeP, planeN, insidePoints[0], outsidePoints[0], texture);
      outTriangle1.texture[2].setU(texture.getTexture() * (outsideTextures[0].getU() - insideTextures[0].getU()) + insideTextures[0].getU());
      outTriangle1.texture[2].setV(texture.getTexture() * (outsideTextures[0].getV() - insideTextures[0].getV()) + insideTextures[0].getV());
      outTriangle1.texture[2].setW(texture.getTexture() * (outsideTextures[0].getW() - insideTextures[0].getW()) + insideTextures[0].getW());

      outTriangle2.point[0].set(insidePoints[1]);
      outTriangle2.texture[0].set(insideTextures[1]);
      outTriangle2.point[1].set(outTriangle1.point[2]);
      outTriangle2.texture[1].set(outTriangle1.texture[2]);
      outTriangle2.point[2] = Vec3d.planeIntersect(planeP, planeN, insidePoints[1], outsidePoints[0], texture);
      outTriangle2.texture[2].setU(texture.getTexture() * (outsideTextures[0].getU() - insideTextures[1].getU()) + insideTextures[1].getU());
      outTriangle2.texture[2].setV(texture.getTexture() * (outsideTextures[0].getV() - insideTextures[1].getV()) + insideTextures[1].getV());
      outTriangle2.texture[2].setW(texture.getTexture() * (outsideTextures[0].getW() - insideTextures[1].getW()) + insideTextures[1].getW());

      return 2;
    }

    return 0;
  }
}