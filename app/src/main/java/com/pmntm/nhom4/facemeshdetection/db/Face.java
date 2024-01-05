package com.pmntm.nhom4.facemeshdetection.db;

import java.util.List;

public class Face {
  String name;
  List<Triangle> triangles;

  public Face(List<Triangle> triangles) {
    this.triangles = triangles;
  }

  public Face(String name, List<Triangle> triangles) {
    this.name = name;
    this.triangles = triangles;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Triangle> getTriangles() {
    return triangles;
  }

  public void setTriangles(List<Triangle> triangles) {
    this.triangles = triangles;
  }
}
