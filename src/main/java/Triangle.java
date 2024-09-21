package src.main.java;

public class Triangle {
  public Vec3d[] point = new Vec3d[3];
  public Vec2d[] texture = new Vec2d[3];
  public int[] textureCoords = new int[3];
  public int[] normalIndices = new int[3];
  public short color = 0;
  public Sprite sprite;

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, Vec2d texture1, Vec2d texture2, Vec2d texture3, short color) {
    this.point[0] = point1.clone();
    this.point[1] = point2.clone();
    this.point[2] = point3.clone();
    this.texture[0] = texture1.clone();
    this.texture[1] = texture2.clone();
    this.texture[2] = texture3.clone();
    this.color = color;
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, Vec2d texture1, Vec2d texture2, Vec2d texture3, int[] normalIndices, Sprite sprite) {
    this.point[0] = point1.clone();
    this.point[1] = point2.clone();
    this.point[2] = point3.clone();
    this.texture[0] = texture1.clone();
    this.texture[1] = texture2.clone();
    this.texture[2] = texture3.clone();
    this.normalIndices = normalIndices;
    this.sprite = sprite;
  }

  public Triangle(Vec3d point1, Vec3d point2, Vec3d point3, Vec2d texture1, Vec2d texture2, Vec2d texture3, short color, int[] normalIndices, Sprite sprite) {
    this.point[0] = point1.clone();
    this.point[1] = point2.clone();
    this.point[2] = point3.clone();
    this.texture[0] = texture1.clone();
    this.texture[1] = texture2.clone();
    this.texture[2] = texture3.clone();
    this.color = color;
    this.normalIndices = normalIndices;
    this.sprite = sprite;
  }

  public Triangle() {
    this.point[0] = new Vec3d();
    this.point[1] = new Vec3d();
    this.point[2] = new Vec3d();
    this.texture[0] = new Vec2d();
    this.texture[1] = new Vec2d();
    this.texture[2] = new Vec2d();
  }

  public Triangle clone() {
    return new Triangle(point[0], point[1], point[2], texture[0], texture[1], texture[2], color, normalIndices, sprite);
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
    sprite = triangle.sprite;
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
      outTriangle1.sprite = inTriangle.sprite;

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
      outTriangle1.sprite = inTriangle.sprite;

      outTriangle2.point[0].set(insidePoints[1]);
      outTriangle2.texture[0].set(insideTextures[1]);
      outTriangle2.point[1].set(outTriangle1.point[2]);
      outTriangle2.texture[1].set(outTriangle1.texture[2]);
      outTriangle2.point[2] = Vec3d.planeIntersect(planeP, planeN, insidePoints[1], outsidePoints[0], texture);
      outTriangle2.texture[2].setU(texture.getTexture() * (outsideTextures[0].getU() - insideTextures[1].getU()) + insideTextures[1].getU());
      outTriangle2.texture[2].setV(texture.getTexture() * (outsideTextures[0].getV() - insideTextures[1].getV()) + insideTextures[1].getV());
      outTriangle2.texture[2].setW(texture.getTexture() * (outsideTextures[0].getW() - insideTextures[1].getW()) + insideTextures[1].getW());
      outTriangle2.sprite = inTriangle.sprite;

      return 2;
    }

    return 0;
  }
}