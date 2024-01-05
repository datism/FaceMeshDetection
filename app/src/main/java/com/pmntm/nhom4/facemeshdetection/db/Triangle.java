package com.pmntm.nhom4.facemeshdetection.db;

public class Triangle {
  private double perimeter;
  private double perimeterVariance;

  public Triangle(double perimeter, double perimeterVariance) {
    this.perimeter = perimeter;
    this.perimeterVariance = perimeterVariance;
  }

  public Triangle(double perimeter) {
    this.perimeter = perimeter;
  }

  public double getPerimeter() {
    return perimeter;
  }

  public void setPerimeter(double perimeter) {
    this.perimeter = perimeter;
  }

  public double getPerimeterVariance() {
    return perimeterVariance;
  }

  public void setPerimeterVariance(double perimeterVariance) {
    this.perimeterVariance = perimeterVariance;
  }
}
