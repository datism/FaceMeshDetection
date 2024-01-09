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
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

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
import com.pmntm.nhom4.facemeshdetection.R;
import com.pmntm.nhom4.facemeshdetection.db.Face;
import com.pmntm.nhom4.facemeshdetection.db.FaceHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kotlin.collections.DoubleIterator;

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
      Pair<String, Double> faceID = getFaceID(facemesh);
      graphicOverlay.add(new FaceMeshGraphic(graphicOverlay, facemesh, faceID));
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }

  private Pair<String, Double>  getFaceID(FaceMesh faceMesh) {
    if (faceList.size() == 0) {
      return null;
    }

    // Get outline of face
    List<FaceMeshPoint> faceOutline = faceMesh.getPoints(FaceMesh.FACE_OVAL);

    // Calculate face perimeter
    double facePerimeter = 0;
    for (int i = 0; i < faceOutline.size() - 1; i++) {
      PointF3D point1 = faceOutline.get(i).getPosition();
      PointF3D point2 = faceOutline.get(i + 1).getPosition();
      facePerimeter += getPointsDistance(point1, point2);
    }

    // Calculate perimeter ratio of each triangles to face
    List<Triangle<FaceMeshPoint>> triangles = faceMesh.getAllTriangles();
    List<Double> perimeterRatio = new ArrayList<>();
    for (Triangle<FaceMeshPoint> triangle : triangles) {
      double trianglePerimeter = getTrianglePerimeter(triangle);
      perimeterRatio.add(trianglePerimeter / facePerimeter);
    }

    // Calculate score of each face's vector to frame's vector
    List<Double> scores = new ArrayList<>();
    for (Face face : faceList) {
      double dist = getEuclideanDistance(perimeterRatio, face.getPerimeterRatio());
      scores.add(getScore(dist, face.getAverageDistance()));
    }

    // Sort score list and get indices
    int[] sortedIndices = IntStream.range(0, scores.size())
            .boxed().sorted(Comparator.comparing(scores::get).reversed())
            .mapToInt(ele -> ele).toArray();

    return  new Pair<>(faceList.get(sortedIndices[0]).getName(), scores.get(sortedIndices[0]));
  }

  public static double getCosineSimilarity(List<Double> vector1, List<Double> vector2) {
    // Check if the vectors have the same length
    if (vector1.size() != vector2.size()) {
      Log.d(TAG,"Vector dimensions do not match.");
    }

    // Calculate the dot product of the two vectors
    double dotProduct = 0.0;
    for (int i = 0; i < vector1.size(); i++) {
      dotProduct += vector1.get(i) * vector2.get(i);
    }

    // Calculate the magnitudes of the two vectors
    double magnitudeVector1 = getMagnitude(vector1);
    double magnitudeVector2 = getMagnitude(vector2);

    // Calculate the cosine similarity
    return dotProduct / (magnitudeVector1 * magnitudeVector2);
  }

  public double getScore(double t, double a) {
    if (t > a) {
      return a / t;
    }
    else return 1;
  }


  public double getEuclideanDistance(List<Double> vector1, List<Double> vector2) {
    if (vector1.size() != vector2.size()) {
      Log.d(TAG,"Vector dimensions do not match");
    }

    double sumOfSquares = 0.0;
    for (int i = 0; i < vector1.size(); i++) {
      double diff = vector1.get(i) - vector2.get(i);
      sumOfSquares += diff * diff;
    }

    return Math.sqrt(sumOfSquares);
  }

  private static double getMagnitude(List<Double> vector) {
    double sumOfSquares = 0.0;
    for (double value : vector) {
      sumOfSquares += value * value;
    }
    return Math.sqrt(sumOfSquares);
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
}
