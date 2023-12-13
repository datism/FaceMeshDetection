package com.pmntm.nhom4.facemeshdetection.db;

public class Face {
  String name;
  Double edwRatio;
  Double edwRatioVariance;
  Double nfRatio;
  Double nfRatioVariance;

  public Face(String name, Double edwRatio, Double edwRatioVariance, Double nfRatio, Double nfRatioVariance) {
    this.name = name;
    this.edwRatio = edwRatio;
    this.edwRatioVariance = edwRatioVariance;
    this.nfRatio = nfRatio;
    this.nfRatioVariance = nfRatioVariance;
  }

  public Face(Double edwRatio, Double nfRatio) {
    this.edwRatio = edwRatio;
    this.nfRatio = nfRatio;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getEdwRatio() {
    return edwRatio;
  }

  public void setEdwRatio(Double edwRatio) {
    this.edwRatio = edwRatio;
  }

  public Double getEdwRatioVariance() {
    return edwRatioVariance;
  }

  public void setEdwRatioVariance(Double edwRatioVariance) {
    this.edwRatioVariance = edwRatioVariance;
  }

  public Double getNfRatio() {
    return nfRatio;
  }

  public void setNfRatio(Double nfRatio) {
    this.nfRatio = nfRatio;
  }

  public Double getNfRatioVariance() {
    return nfRatioVariance;
  }

  public void setNfRatioVariance(Double nfRatioVariance) {
    this.nfRatioVariance = nfRatioVariance;
  }

  @Override
  public String toString() {
    return "Face{" +
            "name='" + name + '\'' +
            ", edwRatio=" + edwRatio +
            ", edwRatioVariance=" + edwRatioVariance +
            ", nfRatio=" + nfRatio +
            ", nfRatioVariance=" + nfRatioVariance +
            '}';
  }
}
