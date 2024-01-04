package com.pmntm.nhom4.facemeshdetection.db;

public class Face {
  String name;
  Double edwRatio;
  Double edwRatioVariance;
  Double nfhRatio;
  Double nfhRatioVariance;
  Double nfwRatio;
  Double nfwRatioVariance;
  Double lfhRatio;
  Double lfhRatioVariance;

  public Face(Double edwRatio, Double nfhRatio, Double nfwRatio, Double lfhRatio) {
    this.edwRatio = edwRatio;
    this.nfhRatio = nfhRatio;
    this.nfwRatio = nfwRatio;
    this.lfhRatio = lfhRatio;
  }

  public Face(String name, Double edwRatio, Double edwRatioVariance, Double nfhRatio, Double nfhRatioVariance, Double nfwRatio, Double nfwRatioVariance, Double lfhRatio, Double lfhRatioVariance) {
    this.name = name;
    this.edwRatio = edwRatio;
    this.edwRatioVariance = edwRatioVariance;
    this.nfhRatio = nfhRatio;
    this.nfhRatioVariance = nfhRatioVariance;
    this.nfwRatio = nfwRatio;
    this.nfwRatioVariance = nfwRatioVariance;
    this.lfhRatio = lfhRatio;
    this.lfhRatioVariance = lfhRatioVariance;
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

  public Double getNfhRatio() {
    return nfhRatio;
  }

  public void setNfhRatio(Double nfhRatio) {
    this.nfhRatio = nfhRatio;
  }

  public Double getNfhRatioVariance() {
    return nfhRatioVariance;
  }

  public void setNfhRatioVariance(Double nfhRatioVariance) {
    this.nfhRatioVariance = nfhRatioVariance;
  }

  public Double getNfwRatio() {
    return nfwRatio;
  }

  public void setNfwRatio(Double nfwRatio) {
    this.nfwRatio = nfwRatio;
  }

  public Double getNfwRatioVariance() {
    return nfwRatioVariance;
  }

  public void setNfwRatioVariance(Double nfwRatioVariance) {
    this.nfwRatioVariance = nfwRatioVariance;
  }

  public Double getLfhRatio() {
    return lfhRatio;
  }

  public void setLfhRatio(Double lfhRatio) {
    this.lfhRatio = lfhRatio;
  }

  public Double getLfhRatioVariance() {
    return lfhRatioVariance;
  }

  public void setLfhRatioVariance(Double lfhRatioVariance) {
    this.lfhRatioVariance = lfhRatioVariance;
  }

  @Override
  public String toString() {

    return "Face(\"" + name + '"' +
            ", " + edwRatio +
            ", " + edwRatioVariance +
            ", " + nfhRatio +
            ", " + nfhRatioVariance +
            ", " + nfwRatio +
            ", " + nfwRatioVariance +
            ", " + lfhRatio +
            ", " + lfhRatioVariance +
            ')';
  }
}
