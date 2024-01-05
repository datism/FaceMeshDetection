/*
 * Copyright 2022 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pmntm.nhom4.facemeshdetection.facemeshdetector;

import android.content.Context;
import android.media.FaceDetector;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.common.Triangle;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshDetection;
import com.google.mlkit.vision.facemesh.FaceMeshDetector;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;

import com.google.mlkit.vision.facemesh.FaceMeshPoint;
import com.pmntm.nhom4.facemeshdetection.GraphicOverlay;
import com.pmntm.nhom4.facemeshdetection.db.Face;
import com.pmntm.nhom4.facemeshdetection.db.FaceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Selfie Face Detector Demo. */
public class FaceMeshDetectorProcessor extends VisionProcessorBase<List<FaceMesh>> {

  private static final String TAG = "SelfieFaceProcessor";

  private final FaceMeshDetector detector;
  private final FaceHandler faceHandler;

  private List<Face> faceList;

  public FaceMeshDetectorProcessor(Context context, FaceHandler faceHandler) {
    super(context);
    FaceMeshDetectorOptions.Builder optionsBuilder = new FaceMeshDetectorOptions.Builder();

    detector = FaceMeshDetection.getClient(optionsBuilder.build());
    this.faceHandler = faceHandler;
    this.faceList = this.faceHandler.getAllFaces();
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<FaceMesh>> detectInImage(InputImage image) {
    return detector.process(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<FaceMesh> faces, @NonNull GraphicOverlay graphicOverlay) {
    for (FaceMesh facemesh : faces) {
      String faceID = getFaceID(facemesh);
      graphicOverlay.add(new FaceMeshGraphic(graphicOverlay, facemesh, faceID));
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }

  static List<Face> faces = new ArrayList<>();
  static Face face;
  private String getFaceID(FaceMesh faceMesh) {
    List<FaceMeshPoint> faceOutline = faceMesh.getPoints(FaceMesh.FACE_OVAL);
    double facePerimeter = 0;
    for (int i = 0; i < faceOutline.size() - 1; i++) {
      PointF3D point1 = faceOutline.get(i).getPosition();
      PointF3D point2 = faceOutline.get(i + 1).getPosition();
      facePerimeter += getPointsDistance(point1, point2);
    }

    List<Triangle<FaceMeshPoint>> triangles = faceMesh.getAllTriangles();
    List<com.pmntm.nhom4.facemeshdetection.db.Triangle> faceTriangle = new ArrayList<>();
    for (Triangle<FaceMeshPoint> triangle : triangles) {
      double trianglePerimeter = getTrianglePerimeter(triangle);
      faceTriangle.add(new com.pmntm.nhom4.facemeshdetection.db.Triangle(facePerimeter / trianglePerimeter));
    }

//    if (faces.size() < 200) {
//      faces.add(new Face(faceTriangle));
//      return null;
//    } else if (face == null) {
//      List<com.pmntm.nhom4.facemeshdetection.db.Triangle> varTriangles = new ArrayList<>();
//
//      List<List<com.pmntm.nhom4.facemeshdetection.db.Triangle>> trianglesList = new ArrayList<>();
//      for (Face faceT : faces) {
//        trianglesList.add(faceT.getTriangles());
//      }
//
//      for (int i = 0; i < trianglesList.get(0).size(); i++) {
//        double sum = 0;
//
//        //Cal mean
//        for (int j = 0; j < trianglesList.size(); j++) {
//          sum += trianglesList.get(j).get(i).getPerimeter();
//        }
//        double mean = sum / trianglesList.size();
//
//        //Cal variance
//        double diffSum = 0;
//        for (int j = 0; j < trianglesList.size(); j++) {
//          diffSum += Math.abs(trianglesList.get(j).get(i).getPerimeter() - mean);
//        }
//        double variance = diffSum / trianglesList.size();
//
//        varTriangles.add(new com.pmntm.nhom4.facemeshdetection.db.Triangle(mean, variance));
//      }
//
//      face = new Face("elon", varTriangles);
//      faceHandler.addFace(face);
//      faceList = faceHandler.getAllFaces();
//      return face.getName();
//
//    } else {
      String faceName = null;
      int maxValidCount = 0;

      for (Face faceT : faceList) {
        List<com.pmntm.nhom4.facemeshdetection.db.Triangle> varTriangles = faceT.getTriangles();
        int validCount = 0;
        for (int i = 0; i < varTriangles.size(); ++i) {

          double perimeter = faceTriangle.get(i).getPerimeter();
          double meanPerimeter = varTriangles.get(i).getPerimeter();
          double varPerimeter = varTriangles.get(i).getPerimeterVariance();

          if (meanPerimeter + varPerimeter > perimeter && meanPerimeter - varPerimeter < perimeter)
            validCount++;
        }

        if ((double) validCount / varTriangles.size() > 0.2 && validCount > maxValidCount) {
          faceName = faceT.getName();
          maxValidCount = validCount;
          Log.d("Feature property", faceT.getName());
        }
      }

      return faceName;
  }

  double getPointsDistance(PointF3D point1, PointF3D point2) {
    float p1x = point1.getX();
    float p2x = point2.getX();

    float p1y = point1.getY();
    float p2y = point2.getY();

    float p1z = point1.getZ();
    float p2z = point2.getZ();

    return Math.sqrt( Math.pow(p1x - p2x, 2) + Math.pow(p1y - p2y, 2) + Math.pow(p1z - p2z, 2));
  }

  double getTrianglePerimeter(Triangle<FaceMeshPoint> triangle) {
    List<FaceMeshPoint> faceMeshPoints = triangle.getAllPoints();
    PointF3D point1 = faceMeshPoints.get(0).getPosition();
    PointF3D point2 = faceMeshPoints.get(1).getPosition();
    PointF3D point3 = faceMeshPoints.get(2).getPosition();

    return getPointsDistance(point1, point2) + getPointsDistance(point2, point3) + getPointsDistance(point3, point1);
  }

  double calculateVariance(List<Double> values) {

    double mean = values.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

    double squaredDiffSum = values.stream()
            .mapToDouble(Double::doubleValue)
            .map(value -> value - mean)
            .map(diff -> diff * diff)
            .sum();

    return Math.sqrt(squaredDiffSum / values.size());
  }
}
