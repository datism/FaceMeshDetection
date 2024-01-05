package com.pmntm.nhom4.facemeshdetection.db;

import java.util.List;

public class Face {
  String name;
  double averageDistance;
  List<Double> perimeterRatio;

  public Face(String name, double averageDistance, List<Double> perimeterRatio) {
    this.name = name;
    this.averageDistance = averageDistance;
    this.perimeterRatio = perimeterRatio;
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

  public List<Double> getPerimeterRatio() {
    return perimeterRatio;
  }

  public void setPerimeterRatio(List<Double> perimeterRatio) {
    this.perimeterRatio = perimeterRatio;
  }
}
