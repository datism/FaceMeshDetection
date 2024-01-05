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
    List<FaceMeshPoint> faceOutline = faceMesh.getPoints(FaceMesh.FACE_OVAL);
    double facePerimeter = 0;
    for (int i = 0; i < faceOutline.size() - 1; i++) {
      PointF3D point1 = faceOutline.get(i).getPosition();
      PointF3D point2 = faceOutline.get(i + 1).getPosition();
      facePerimeter += getPointsDistance(point1, point2);
    }

    List<Triangle<FaceMeshPoint>> triangles = faceMesh.getAllTriangles();
    List<Double> perimeterRatios = new ArrayList<>();
    for (Triangle<FaceMeshPoint> triangle : triangles) {
      double trianglePerimeter = getTrianglePerimeter(triangle);
      perimeterRatios.add(trianglePerimeter / facePerimeter);
    }

    if (vectorList.size() < 100)
    {
      vectorList.add(perimeterRatios);
    }
    else if (!done) {
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
