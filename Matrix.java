public class Matrix {
  protected double[][] matrix = new double[4][4];

  public Matrix(double[][] matrix) {
    this.matrix = matrix;
  }

  public Matrix() {
    this.matrix = new double[4][4];
  }
}