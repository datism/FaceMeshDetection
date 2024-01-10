package com.pmntm.nhom4.facemeshdetection.db;

import java.util.List;

public class Face {
  String name;
  double averageDistance;
  List<Double> vector;

  public Face(String name, double averageDistance, List<Double> vector) {
    this.name = name;
    this.averageDistance = averageDistance;
    this.vector = vector;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getAverageDistance() {
    return averageDistance;
  }

  public void setAverageDistance(double averageDistance) {
    this.averageDistance = averageDistance;
  }

  public List<Double> getVector() {
    return vector;
  }

  public void setVector(List<Double> vector) {
    this.vector = vector;
  }
}
