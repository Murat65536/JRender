package src.main.java;

public class Matrix {
  public float[][] matrix = new float[4][4];

  public Matrix(float[][] matrix) {
    this.matrix = matrix;
  }

  public Matrix() {
    matrix[0][0] = 1;
    matrix[1][1] = 1;
    matrix[2][2] = 1;
    matrix[3][3] = 1;
  }

  public static Matrix rotationX(float angle) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = 1;
    matrix.matrix[1][1] = (float)Math.cos(angle);
    matrix.matrix[1][2] = (float)Math.sin(angle);
    matrix.matrix[2][1] = -(float)Math.sin(angle);
    matrix.matrix[2][2] = (float)Math.cos(angle);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public static Matrix rotationY(float angle) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = (float)Math.cos(angle);
    matrix.matrix[0][2] = (float)Math.sin(angle);
    matrix.matrix[2][0] = -(float)Math.sin(angle);
    matrix.matrix[1][1] = 1;
    matrix.matrix[2][2] = (float)Math.cos(angle);
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public static Matrix rotationZ(float angle) {
    Matrix matrix = new Matrix();
    matrix.matrix[0][0] = (float)Math.cos(angle);
    matrix.matrix[0][1] = (float)Math.sin(angle);
    matrix.matrix[1][0] = -(float)Math.sin(angle);
    matrix.matrix[1][1] = (float)Math.cos(angle);
    matrix.matrix[2][2] = 1;
    matrix.matrix[3][3] = 1;

    return matrix;
  }

  public static Matrix translation(float x, float y, float z) {
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

  public static Matrix projection(float fov, float aspectRatio, float near, float far) {
    float fovRadians = 1 / (float)Math.tan(Math.toRadians(fov * 0.5));
    Matrix matrix = new Matrix(new float[][] {
      {aspectRatio * fovRadians, 0, 0, 0},
      {0, fovRadians, 0, 0},
      {0, 0, far / (far - near), 1},
      {0, 0, (-far * near) / (far - near), 0}
    });

    return matrix;
  }

  public static Matrix multiply(Matrix m1, Matrix m2) {
    Matrix matrix = new Matrix();
    for (byte c = 0; c < 4; c++) {
      for (byte r = 0; r < 4; r++) {
        matrix.matrix[r][c] = m1.matrix[r][0] * m2.matrix[0][c] + m1.matrix[r][1] * m2.matrix[1][c] + m1.matrix[r][2] * m2.matrix[2][c] + m1.matrix[r][3] * m2.matrix[3][c];
      }
    }
    return matrix;
  }

  public static Matrix pointAt(Vec3d pos, Vec3d target, Vec3d up) {
    Vec3d newForward = Vec3d.subtract(target, pos);
    newForward = Vec3d.normalize(newForward);

    Vec3d a = Vec3d.multiply(newForward, Vec3d.dotProduct(up, newForward));
    Vec3d newUp = Vec3d.subtract(up, a);
    newUp = Vec3d.normalize(newUp);

    Vec3d newRight = Vec3d.crossProduct(newUp, newForward);

    Matrix matrix = new Matrix(new float[][] {
      {newRight.getX(), newRight.getY(), newRight.getZ(), 0},
      {newUp.getX(), newUp.getY(), newUp.getZ(), 0},
      {newForward.getX(), newForward.getY(), newForward.getZ(), 0},
      {pos.getX(), pos.getY(), pos.getZ(), 1}
    });

    return matrix;
  }

  public static Matrix inverse(Matrix m) {
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
}