package src.main.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Mesh {
  private ArrayList<Triangle> triangles = new ArrayList<>();
  private ArrayList<Vec3d> vertexNormals = new ArrayList<>();
  private ArrayList<Vec2d> textureCoords = new ArrayList<>();
  private final String PATH = "src/main/resources/";
  private Map<String, Map<String, String[]>> textures = new HashMap<>();
  private Map<String, String[]> attributes = new HashMap<>();
  private Sprite sprite;

  public Mesh(List<Triangle> triangles) {
    this.triangles.addAll(triangles);
  }

  public Mesh(String fileName, boolean hasTexture) {
    try {
      File file = new File(PATH + fileName);
      Scanner reader = new Scanner(file);
      ArrayList<Vec3d> vertices = new ArrayList<>();
      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        String[] splitData = data.split(" ");
        if (data.startsWith("mtllib ")) {
          loadTexture(splitData[1]);
        }
        if (data.startsWith("v ")) {
          vertices.add(new Vec3d(Float.parseFloat(splitData[1]), Float.parseFloat(splitData[2]), Float.parseFloat(splitData[3])));
        }
        else if (data.startsWith("f ")) {
          if (hasTexture) {
            triangles.add(new Triangle(
              vertices.get(Integer.parseInt(splitData[1].split("/")[0]) - 1),
              vertices.get(Integer.parseInt(splitData[2].split("/")[0]) - 1),
              vertices.get(Integer.parseInt(splitData[3].split("/")[0]) - 1),
              textureCoords.get(Integer.parseInt(splitData[1].split("/")[1]) - 1),
              textureCoords.get(Integer.parseInt(splitData[2].split("/")[1]) - 1),
              textureCoords.get(Integer.parseInt(splitData[3].split("/")[1]) - 1),
              new int[] {
                Integer.parseInt(splitData[1].split("/")[2]) - 1,
                Integer.parseInt(splitData[2].split("/")[2]) - 1,
                Integer.parseInt(splitData[3].split("/")[2]) - 1
              }
            ));
          }
          else {
            triangles.add(new Triangle(
              vertices.get(Integer.parseInt(splitData[1].split("/")[0]) - 1),
              vertices.get(Integer.parseInt(splitData[2].split("/")[0]) - 1),
              vertices.get(Integer.parseInt(splitData[3].split("/")[0]) - 1),
              new int[] {
                Integer.parseInt(splitData[1].split("/")[2]) - 1,
                Integer.parseInt(splitData[2].split("/")[2]) - 1,
                Integer.parseInt(splitData[3].split("/")[2]) - 1
              }
            ));
          }
        }
        else if (data.startsWith("vn ")) {
          vertexNormals.add(new Vec3d(Float.parseFloat(splitData[1]), Float.parseFloat(splitData[2]), Float.parseFloat(splitData[3])));
        }
        else if (data.startsWith("vt")) {
          textureCoords.add(new Vec2d(Float.parseFloat(splitData[1]), Float.parseFloat(splitData[2])));
        }
      }
      reader.close();
    }
    catch (FileNotFoundException exception) {
      exception.printStackTrace();
    }
  }

  public void loadTexture(String fileName) {
    try {
      File file = new File(PATH + fileName);
      Scanner reader = new Scanner(file);

      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        String[] splitData = data.split(" ");

        if (data.startsWith("newmtl ")) {
          attributes.clear();
          textures.put(splitData[1], attributes);
        }
        else if (data.startsWith("Ka ")) {
          attributes.put("Ambient Color", new String[] {splitData[1], splitData[2], splitData[3]});
        }
        else if (data.startsWith("Kd ")) {
          attributes.put("Diffuse Color", new String[] {splitData[1], splitData[2], splitData[3]});
        }
        else if (data.startsWith("Ks ")) {
          attributes.put("Specular Color", new String[] {splitData[1], splitData[2], splitData[3]});
        }
        else if (data.startsWith("Ns ")) {
          attributes.put("Shininess", new String[] {splitData[1]});
        }
        else if (data.startsWith("d ")) {
          attributes.put("Dissolve", new String[] {splitData[1]});
        }
        else if (data.startsWith("map_Kd ")) {
          sprite = new Sprite(splitData[1]);
          // attributes.put("Diffuse Texture Map", new String[] {splitData[1]});
        }
      }
      reader.close();
    }
    catch (FileNotFoundException exception) {
      exception.printStackTrace();
    }
  }

  public Sprite getSprite() {
    return this.sprite;
  }

  public ArrayList<Triangle> getTriangles() {
    return this.triangles;
  }

  public ArrayList<Vec3d> getVertexNormals() {
    return this.vertexNormals;
  }

  public ArrayList<Vec2d> getTextureCoords() {
    return this.textureCoords;
  }

  public Map<String, Map<String, String[]>> getTextures() {
    return this.textures;
  }
}