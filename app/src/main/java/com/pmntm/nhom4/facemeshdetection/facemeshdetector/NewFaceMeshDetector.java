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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.FaceDetector;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.pmntm.nhom4.facemeshdetection.LivePreviewActivity;
import com.pmntm.nhom4.facemeshdetection.R;
import com.pmntm.nhom4.facemeshdetection.db.Face;
import com.pmntm.nhom4.facemeshdetection.db.FaceHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Selfie Face Detector Demo. */
public class NewFaceMeshDetector extends VisionProcessorBase<List<FaceMesh>> {

  private static final String TAG = "SelfieFaceProcessor";

  private final FaceMeshDetector detector;
  private final FaceHandler faceHandler;
  private final Context context;

  public NewFaceMeshDetector(Context context, FaceHandler faceHandler) {
    super(context);
    this.context = context;
    FaceMeshDetectorOptions.Builder optionsBuilder = new FaceMeshDetectorOptions.Builder();

    detector = FaceMeshDetection.getClient(optionsBuilder.build());
    this.faceHandler = faceHandler;
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
      graphicOverlay.add(new FaceMeshGraphic(graphicOverlay, facemesh, null));
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }

  private final List<List<Double>> vectorList = new ArrayList<>();
  boolean done = false;
  private String getFaceID(FaceMesh faceMesh) {

    if (vectorList.size() < 100)
    {
      List<Triangle<FaceMeshPoint>> triangles = new ArrayList<>();
      List<FaceMeshPoint> allPoints = faceMesh.getAllPoints();
      FaceMeshPoint nose = allPoints.get(4);
      FaceMeshPoint left_lip = allPoints.get(61);
      FaceMeshPoint right_lip = allPoints.get(291);
      FaceMeshPoint left_left_eye = allPoints.get(33);
      FaceMeshPoint right_left_eye = allPoints.get(133);
      FaceMeshPoint left_right_eye = allPoints.get(362);
      FaceMeshPoint right_right_eye = allPoints.get(263);

      triangles.add(new Triangle<>(left_left_eye, right_left_eye, nose));
      triangles.add(new Triangle<>(right_left_eye, left_right_eye, nose));
      triangles.add(new Triangle<>(left_right_eye, right_right_eye, right_lip));
      triangles.add(new Triangle<>(left_left_eye, nose, left_lip));
      triangles.add(new Triangle<>(left_right_eye, nose, right_lip));
      triangles.add(new Triangle<>(left_lip, nose, right_lip));

      List<Double> vector = new ArrayList<>();
      for (Triangle<FaceMeshPoint> triangle: triangles) {
        List<FaceMeshPoint> points = triangle.getAllPoints();
        double a = getPointsDistance(points.get(0), points.get(1));
        double b = getPointsDistance(points.get(0), points.get(2));
        double c = getPointsDistance(points.get(1), points.get(2));

        vector.add(a/b);
        vector.add(a/c);
        vector.add(b/c);
      }
      vectorList.add(vector);
    }
    else if (!done)
    {
      int dimensions = vectorList.get(0).size(); // Number of dimensions

      // Initialize the average vector with zeros
      List<Double> averageVector = new ArrayList<>(Collections.nCopies(dimensions, 0.0));

      // Compute the sum of vectors
      for (List<Double> vector : vectorList) {
        // Check if the vector dimensions match
        if (vector.size() != dimensions) {
          Log.d(TAG,"Vector dimensions do not match.");
        }

        // Add each element of the vector to the corresponding element in the average vector
        for (int i = 0; i < dimensions; i++) {
          averageVector.set(i, averageVector.get(i) + vector.get(i));
        }
      }

      // Divide each element of the average vector by the number of vectors
      int vectorCount = vectorList.size();
      for (int i = 0; i < dimensions; i++) {
        averageVector.set(i, averageVector.get(i) / vectorCount);
      }

      // Get average distance
      double averageDist = 0;
      for (List<Double> vector: vectorList) {
        averageDist += getEuclideanDistance(vector, averageVector);
      }
      averageDist /= vectorList.size();

      done = true;
      showPopupWindow(averageVector, averageDist);
    }

    return null;
  }

  void showPopupWindow(List<Double> averageVector, double averageDist)
  {
    View view = View.inflate(this.context, R.layout.popup_layout , null);

    Button ok = view.findViewById(R.id.ok);
    EditText nameEditText = view.findViewById(R.id.name);

    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
    int height = ViewGroup.LayoutParams.WRAP_CONTENT;
    PopupWindow popupWindow = new PopupWindow(view, width, height, true);

    ConstraintLayout parent =  (ConstraintLayout) ((Activity) context).findViewById(R.id.parent_layout);
    popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    ok.setOnClickListener(v -> {
      String name = nameEditText.getText().toString();

      Face face1 = new Face(name, averageDist, averageVector);
      faceHandler.addFace(face1);

      ((Activity) context).startActivity(new Intent(context, LivePreviewActivity.class));
    });

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

  double getPointsDistance(FaceMeshPoint point1, FaceMeshPoint point2) {
    float p1x = point1.getPosition().getX();
    float p2x = point2.getPosition().getX();

    float p1y = point1.getPosition().getY();
    float p2y = point2.getPosition().getY();

    float p1z = point1.getPosition().getZ();
    float p2z = point2.getPosition().getZ();

    return Math.sqrt( Math.pow(p1x - p2x, 2) + Math.pow(p1y - p2y, 2) + Math.pow(p1z - p2z, 2));
  }

  double getTrianglePerimeter(Triangle<FaceMeshPoint> triangle) {
    List<FaceMeshPoint> faceMeshPoints = triangle.getAllPoints();
    FaceMeshPoint point1 = faceMeshPoints.get(0);
    FaceMeshPoint point2 = faceMeshPoints.get(1);
    FaceMeshPoint point3 = faceMeshPoints.get(2);

    return getPointsDistance(point1, point2) + getPointsDistance(point2, point3) + getPointsDistance(point3, point1);
  }
}
