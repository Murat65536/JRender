import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Mesh {
  protected ArrayList<Triangle> triangles = new ArrayList<Triangle>();

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

        if (data.length() > 0) {
          if (data.substring(0, 2).equals("v ")) {
            Vec3d vector = new Vec3d(Float.parseFloat(splitData[1]), Float.parseFloat(splitData[2]), Float.parseFloat(splitData[3]));
            vertices.add(vector);
          }
          else if (data.substring(0, 2).equals("f ")) {
            int[] faces = new int[3];
            faces[0] = Integer.parseInt(splitData[1].split("/")[0]);
            faces[1] = Integer.parseInt(splitData[2].split("/")[0]);
            faces[2] = Integer.parseInt(splitData[3].split("/")[0]);
            triangles.add(new Triangle(vertices.get(faces[0] - 1), vertices.get(faces[1] - 1), vertices.get(faces[2] - 1)));
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