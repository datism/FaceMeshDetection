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

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.common.Triangle;

import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMesh.ContourType;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import com.pmntm.nhom4.facemeshdetection.GraphicOverlay;
import com.pmntm.nhom4.facemeshdetection.GraphicOverlay.Graphic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Graphic instance for rendering face position and mesh info within the associated graphic overlay
 * view.
 */
public class NewFaceMeshGraphic extends Graphic {
    private static final int USE_CASE_CONTOUR_ONLY = 999;

    private static final float FACE_POSITION_RADIUS = 8.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final float ID_TEXT_SIZE = 40.0f;

    private final Paint positionPaint;
    private final Paint boxPaint;
    private final Paint idPaint;
    private final Paint labelPaint;
    private volatile FaceMesh faceMesh;
    private volatile String name;
    private final int useCase;
    private float zMin;
    private float zMax;

    @ContourType
    private static final int[] DISPLAY_CONTOURS = {
            FaceMesh.FACE_OVAL,
            FaceMesh.LEFT_EYEBROW_TOP,
            FaceMesh.LEFT_EYEBROW_BOTTOM,
            FaceMesh.RIGHT_EYEBROW_TOP,
            FaceMesh.RIGHT_EYEBROW_BOTTOM,
            FaceMesh.LEFT_EYE,
            FaceMesh.RIGHT_EYE,
            FaceMesh.UPPER_LIP_TOP,
            FaceMesh.UPPER_LIP_BOTTOM,
            FaceMesh.LOWER_LIP_TOP,
            FaceMesh.LOWER_LIP_BOTTOM,
            FaceMesh.NOSE_BRIDGE
    };

    NewFaceMeshGraphic(GraphicOverlay overlay, FaceMesh faceMesh) {
        super(overlay);

        this.faceMesh = faceMesh;
        final int selectedColor = Color.WHITE;

        positionPaint = new Paint();
        positionPaint.setColor(selectedColor);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        labelPaint = new Paint();
        labelPaint.setColor(selectedColor);
        labelPaint.setStyle(Paint.Style.FILL);

        idPaint = new Paint();
        idPaint.setColor(Color.BLACK);
        idPaint.setTextSize(ID_TEXT_SIZE);

//    useCase = FaceMeshDetectorOptions.FACE_MESH;
        useCase = USE_CASE_CONTOUR_ONLY;
    }

    NewFaceMeshGraphic(GraphicOverlay overlay, FaceMesh faceMesh, String name) {
        this(overlay, faceMesh);
        this.name = name;
    }

    /** Draws the face annotations for position on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        if (faceMesh == null) {
            return;
        }

        // Draws the bounding box.
        RectF rect = new RectF(faceMesh.getBoundingBox());
        // If the image is flipped, the left will be translated to right, and the right to left.
        float x0 = translateX(rect.left);
        float x1 = translateX(rect.right);
        rect.left = min(x0, x1);
        rect.right = max(x0, x1);
        rect.top = translateY(rect.top);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, boxPaint);

        if (name == null) {
            float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
            float yLabelOffset = -lineHeight;
            String idString = "ID: " + name;
            EditText input_name;

            float textWidth = idPaint.measureText(idString);
            // Draw labels
            canvas.drawRect(
                    rect.left - BOX_STROKE_WIDTH,
                    rect.top + yLabelOffset,
                    rect.left + textWidth + (2 * BOX_STROKE_WIDTH),
                    rect.top,
                    labelPaint);

            canvas.drawText(
                    idString, rect.left, rect.top, idPaint);
        }

        // Draw face mesh
        List<FaceMeshPoint> points =
                useCase == USE_CASE_CONTOUR_ONLY ? getContourPoints(faceMesh) : faceMesh.getAllPoints();
        List<Triangle<FaceMeshPoint>> triangles = faceMesh.getAllTriangles();

        zMin = Float.MAX_VALUE;
        zMax = Float.MIN_VALUE;
        for (FaceMeshPoint point : points) {
            zMin = min(zMin, point.getPosition().getZ());
            zMax = max(zMax, point.getPosition().getZ());
        }

        // Draw face mesh points
        for (FaceMeshPoint point : points) {
            updatePaintColorByZValue(
                    positionPaint,
                    canvas,
                    /* visualizeZ= */ true,
                    /* rescaleZForVisualization= */ true,
                    point.getPosition().getZ(),
                    zMin,
                    zMax);
            canvas.drawCircle(
                    translateX(point.getPosition().getX()),
                    translateY(point.getPosition().getY()),
                    FACE_POSITION_RADIUS,
                    positionPaint);
        }


        if (useCase == FaceMeshDetectorOptions.FACE_MESH) {
            // Draw face mesh triangles
            for (Triangle<FaceMeshPoint> triangle : triangles) {
                List<FaceMeshPoint> faceMeshPoints = triangle.getAllPoints();
                PointF3D point1 = faceMeshPoints.get(0).getPosition();
                PointF3D point2 = faceMeshPoints.get(1).getPosition();
                PointF3D point3 = faceMeshPoints.get(2).getPosition();

                drawLine(canvas, point1, point2);
                drawLine(canvas, point2, point3);
                drawLine(canvas, point3, point1);
            }
        }
    }

    private List<FaceMeshPoint> getContourPoints(FaceMesh faceMesh) {
        List<FaceMeshPoint> contourPoints = new ArrayList<>();
        for (int type : DISPLAY_CONTOURS) {
            contourPoints.addAll(faceMesh.getPoints(type));
        }

//    List<FaceMeshPoint> pointList = new ArrayList<>(faceMesh.getAllPoints());
//    for (FaceMeshPoint point: pointList) {
//      if (point.getPosition().getZ() == 0) {
//        contourPoints.add(point);
//      }
//    }

//    FaceMeshPoint smallestZPoint = null;
//    FaceMeshPoint largestZPoint = null;
//    float smallestZ = Float.MAX_VALUE;
//    float largestZ = Float.MIN_VALUE;
//
//    for (FaceMeshPoint point: pointList) {
//      float z = point.getPosition().getZ();
//
//      if (z < smallestZ) {
//        smallestZ = z;
//        smallestZPoint = point;
//      }
//
//      if (z > largestZ) {
//        largestZ = z;
//        largestZPoint = point;
//      }
//    }
//
//    contourPoints.add(smallestZPoint);
//    contourPoints.add(largestZPoint);

//    Log.d("Feature property", "Largest: " + largestZPoint.getPosition().toString());
//    Log.d("Feature property", "Smallest: " + smallestZPoint.getPosition().toString());

//
//    List<FaceMeshPoint> nose_bridge = faceMesh.getPoints(FaceMesh.NOSE_BRIDGE);
//
//    contourPoints.add(nose_bridge.get(0));
//    contourPoints.add(nose_bridge.get(nose_bridge.size() - 1));
//    Log.d("Feature property", "Nose 1: " + nose_bridge.get(0).getPosition().toString());
//    Log.d("Feature property", "Nose 2: " + nose_bridge.get(nose_bridge.size() - 1).getPosition().toString());
//
//    double nose_bridge_length = getPointsDistance(
//            nose_bridge.get(0).getPosition(),
//            nose_bridge.get(nose_bridge.size() - 1) .getPosition());
//    Log.d("Feature property", "Nose bridge length: " + nose_bridge_length);
//
//    List<FaceMeshPoint> left_eye = faceMesh.getPoints(FaceMesh.LEFT_EYE);
//    List<FaceMeshPoint> right_eye = faceMesh.getPoints(FaceMesh.RIGHT_EYE);
//
//
//    Log.d("Feature property", "Left eye: " + left_eye.get(left_eye.size()/2).getPosition().toString());
//    Log.d("Feature property", "Right eye: " + right_eye.get(0).getPosition().toString());
//
//    contourPoints.add(left_eye.get(left_eye.size()/2));
//    contourPoints.add(right_eye.get(0));
//
//    double eyes_distance = getPointsDistance(left_eye.get(left_eye.size()/2).getPosition(),
//            right_eye.get(0).getPosition());
//    Log.d("Feature property", "Eyes distance: " + eyes_distance);
//    Log.d("Feature property", "Ratio: " + nose_bridge_length / eyes_distance);
//
//    Log.d("Feature property", "-----------------------------------------");

//    List<FaceMeshPoint> face_outline = faceMesh.getPoints(FaceMesh.FACE_OVAL);
//    contourPoints.add(face_outline.get(0));
//    contourPoints.add(face_outline.get(face_outline.size()/2));


        return contourPoints;
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

    private void drawLine(Canvas canvas, PointF3D point1, PointF3D point2) {
        updatePaintColorByZValue(
                positionPaint,
                canvas,
                /* visualizeZ= */ true,
                /* rescaleZForVisualization= */ true,
                (point1.getZ() + point2.getZ()) / 2,
                zMin,
                zMax);
        canvas.drawLine(
                translateX(point1.getX()),
                translateY(point1.getY()),
                translateX(point2.getX()),
                translateY(point2.getY()),
                positionPaint);
    }
}
