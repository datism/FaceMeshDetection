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
  private String getFaceID(FaceMesh faceMesh){
    List<FaceMeshPoint> nose_bridge = faceMesh.getPoints(FaceMesh.NOSE_BRIDGE);
    List<FaceMeshPoint> left_eye = faceMesh.getPoints(FaceMesh.LEFT_EYE);
    List<FaceMeshPoint> right_eye = faceMesh.getPoints(FaceMesh.RIGHT_EYE);
    List<FaceMeshPoint> face_outline = faceMesh.getPoints(FaceMesh.FACE_OVAL);
    List<FaceMeshPoint> upper_lip_top = faceMesh.getPoints(FaceMesh.UPPER_LIP_TOP);
    List<FaceMeshPoint> upper_lip_bottom = faceMesh.getPoints(FaceMesh.UPPER_LIP_BOTTOM);
    List<FaceMeshPoint> lower_lip_top = faceMesh.getPoints(FaceMesh.LOWER_LIP_TOP);
    List<FaceMeshPoint> lower_lip_bottom = faceMesh.getPoints(FaceMesh.LOWER_LIP_BOTTOM);

//    Log.d("Feature property", "Nose 1: " + nose_bridge.get(0).getPosition().toString());
//    Log.d("Feature property", "Nose 2: " + nose_bridge.get(nose_bridge.size() - 1).getPosition().toString());
    double nose_bridge_length = getPointsDistance(
            nose_bridge.get(0).getPosition(),
            nose_bridge.get(nose_bridge.size() - 1).getPosition());
//    Log.d("Feature property", "Nose bridge length: " + nose_bridge_length);

    double face_length = getPointsDistance(face_outline.get(0).getPosition(), face_outline.get(face_outline.size()/2).getPosition());
//    Log.d("Feature property", "Face length: " + face_length);

//    Log.d("Feature property", "Left eye: " + left_eye.get(left_eye.size()/2).getPosition().toString());
//    Log.d("Feature property", "Right eye: " + right_eye.get(0).getPosition().toString());
    double eyes_distance = getPointsDistance(left_eye.get(left_eye.size()/2).getPosition(),
            right_eye.get(0).getPosition());
//    Log.d("Feature property", "Eyes distance: " + eyes_distance);

    double left_eye_width = getPointsDistance(left_eye.get(0).getPosition(), left_eye.get(left_eye.size()/2).getPosition());
    double right_eye_width = getPointsDistance(right_eye.get(0).getPosition(), right_eye.get(left_eye.size()/2).getPosition());
//    Log.d("Feature property", "Left eye width: " + left_eye_width);
//    Log.d("Feature property", "Right eye width: " + right_eye_width);
//    Log.d("Feature property", "Eye width: " + (right_eye_width + left_eye_width)/2);

    List<FaceMeshPoint> points = faceMesh.getAllPoints();
    double nose_width = getPointsDistance(points.get(48).getPosition(), points.get(278).getPosition());
    double face_width = getPointsDistance(face_outline.get(9).getPosition(), face_outline.get(27).getPosition());

    double upper_lip_height = getPointsDistance(upper_lip_top.get(5).getPosition(), lower_lip_bottom.get(5).getPosition());


    double eyes_dist_width_ratio = eyes_distance/ ((left_eye_width + right_eye_width)/2);
    double nose_face_height_ratio =  face_length / nose_bridge_length;
    double nose_face_width_ratio = face_width / nose_width;
    double lip_face_height_ratio = face_length / upper_lip_height;
//    Log.d("Feature property", "Eyes ratio: " + eyes_dist_width_ratio);
//    Log.d("Feature property", "Face nose ratio: " + nose_face_ratio);
//    Log.d("Feature property", "-----------------------------------------");


//    if (faces.size() < 200) {
//      faces.add(new Face(eyes_dist_width_ratio, nose_face_height_ratio, nose_face_width_ratio, lip_face_height_ratio));
//    }
//    else {
//      if (face == null)
//        face = new Face("dat",
//                      faces.stream().mapToDouble(Face::getEdwRatio).average().orElse(0),
//                      calculateVariance(faces.stream().map(Face::getEdwRatio).collect(Collectors.toList())),
//                      faces.stream().mapToDouble(Face::getNfhRatio).average().orElse(0),
//                      calculateVariance(faces.stream().map(Face::getNfhRatio).collect(Collectors.toList())),
//                      faces.stream().mapToDouble(Face::getNfwRatio).average().orElse(0),
//                      calculateVariance(faces.stream().map(Face::getNfwRatio).collect(Collectors.toList())),
//                      faces.stream().mapToDouble(Face::getLfhRatio).average().orElse(0),
//                      calculateVariance(faces.stream().map(Face::getLfhRatio).collect(Collectors.toList())));
//      else {
//        int validCount =  0;
//
//        if (face.getEdwRatio() - face.getEdwRatioVariance() < eyes_dist_width_ratio &&
//            face.getEdwRatio() + face.getEdwRatioVariance() > eyes_dist_width_ratio) {
//          validCount++;
//        }
//
//        if (face.getNfhRatio() - face.getNfhRatioVariance() < nose_face_height_ratio &&
//            face.getNfhRatio() + face.getNfhRatioVariance() > nose_face_height_ratio) {
//          validCount++;
//        }
//
//        if (face.getNfwRatio() - face.getNfwRatioVariance() < nose_face_width_ratio &&
//            face.getNfwRatio() + face.getNfwRatioVariance() > nose_face_width_ratio) {
//          validCount++;
//        }
//
//        if (face.getLfhRatio() - face.getLfhRatioVariance() < lip_face_height_ratio &&
//                face.getLfhRatio() + face.getLfhRatioVariance() > lip_face_height_ratio) {
//          validCount++;
//        }
//
//        if (validCount > 1) {
//          Log.d("Feature property", face.toString());
//          Log.d("Feature property", "-----------------------------------------");
//        }
//      }
//    }
//
//    return null;


    int maxValidCount = 0;
    Face faceId = null;
    for (Face face: faceList) {

      int validCount = 0;

      if (face.getEdwRatio() - face.getEdwRatioVariance() < eyes_dist_width_ratio &&
              face.getEdwRatio() + face.getEdwRatioVariance() > eyes_dist_width_ratio) {
        validCount++;
      }

      if (face.getNfhRatio() - face.getNfhRatioVariance() < nose_face_height_ratio &&
              face.getNfhRatio() + face.getNfhRatioVariance() > nose_face_height_ratio) {
        validCount++;
      }

      if (face.getNfwRatio() - face.getNfwRatioVariance() < nose_face_width_ratio &&
              face.getNfwRatio() + face.getNfwRatioVariance() > nose_face_width_ratio) {
        validCount++;
      }

      if (face.getLfhRatio() - face.getLfhRatioVariance() < lip_face_height_ratio &&
              face.getLfhRatio() + face.getLfhRatioVariance() > lip_face_height_ratio) {
        validCount++;
      }

      if (validCount >= 2 && validCount > maxValidCount) {
        maxValidCount = validCount;
        faceId = face;
      }
    }

    if (faceId == null)
      return null;
    else
      return faceId.getName();
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
