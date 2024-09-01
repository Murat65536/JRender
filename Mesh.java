import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Mesh {
  protected ArrayList<Triangle> triangles = new ArrayList<Triangle>();
  protected ArrayList<Vec3d> vertexNormals = new ArrayList<Vec3d>();

  public Mesh(List<Triangle> triangles) {
    this.triangles.addAll(triangles);
  }

  public Mesh() {}

  protected void load(String fileName) {
    try {
      File file = new File(fileName);
      Scanner reader = new Scanner(file);
      ArrayList<Vec3d> vertices = new ArrayList<Vec3d>();
      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        String[] splitData = data.split(" ");

        // TODO Use vn and vt instead of calculating them
        if (data.length() > 0) {
          if (data.substring(0, 2).equals("v ")) {
            vertices.add(new Vec3d(Float.parseFloat(splitData[1]), Float.parseFloat(splitData[2]), Float.parseFloat(splitData[3])));
          }
          else if (data.substring(0, 2).equals("f ")) {
            triangles.add(new Triangle(vertices.get(Integer.parseInt(splitData[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(splitData[2].split("/")[0]) - 1), vertices.get(Integer.parseInt(splitData[3].split("/")[0]) - 1)));
          }
          else if (data.substring(0, 3).equals("vn ")) {
            vertexNormals.add(new Vec3d(Float.parseFloat(splitData[1]), Float.parseFloat(splitData[2]), Float.parseFloat(splitData[3])));
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