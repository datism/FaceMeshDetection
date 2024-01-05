/*
 * Copyright 2020 Google LLC. All rights reserved.
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

package com.pmntm.nhom4.facemeshdetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.annotation.KeepName;

import com.pmntm.nhom4.facemeshdetection.db.Face;
import com.pmntm.nhom4.facemeshdetection.db.FaceHandler;
import com.pmntm.nhom4.facemeshdetection.facemeshdetector.FaceMeshDetectorProcessor;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/** Live preview demo for ML Kit APIs. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
  private static final String FACE_MESH_DETECTION = "Face Mesh Detection (Beta)";

  private static final String TAG = "LivePreviewActivity";

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = FACE_MESH_DETECTION;

  private FaceHandler faceHandler;

  private static final String[] REQUIRED_RUNTIME_PERMISSIONS = {
          Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private static final int PERMISSION_REQUESTS = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_live_preview);

    preview = findViewById(R.id.preview_view);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
    facingSwitch.setOnCheckedChangeListener(this);

    if (!allRuntimePermissionsGranted()) {
      getRuntimePermissions();
    }

    faceHandler = new FaceHandler(this);
//    faceHandler.addFace(new Face("dat", 1.3804391083544503, 0.04412301738161332, 3.4659245515403616, 0.04537269091141589, 3.4954390953276935, 0.06732979847617482, 7.711298005979816, 0.25923570810855096));
//    faceHandler.addFace(new Face("elon", 1.3182220692846753, 0.030163448139193232, 3.555633597673497, 0.05557313756654942, 4.038038985246534, 0.0598075811706378, 10.43886577129314, 0.46108964181494105));
//    faceHandler.addFace(new Face("dat", 1.3792557533662047, 0.03445886410103186, 3.4428790387438037, 0.05250448389202977, 3.531250346179998, 0.059751168285400426));
//    faceHandler.addFace(new Face("elon", 1.2820342354093146, 0.04903901662508301, 3.603534160489981, 0.07814667290668638, 3.9254147264679125, 0.17964873435771858));
//    faceHandler.addFace(new Face("dat", 1.4283505765701627, 0.029274422068446236, 3.513794466702038, 0.02505434568330515));
//    faceHandler.addFace(new Face("elon musk", 1.3241561428033954, 0.05410470886188243, 3.600446506990943, 0.04235797584950561));
//    faceHandler.addFace(new Face("duc", 1.3316894236304788, 0.022135166389065784, 3.3248719339178776, 0.03231053715811754));
    createCameraSource(selectedModel);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource(String model) {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }

    try {
      switch (model) {
        case FACE_MESH_DETECTION:
          cameraSource.setMachineLearningFrameProcessor(new FaceMeshDetectorProcessor(this, faceHandler));
          break;
        default:
          Log.e(TAG, "Unknown model: " + model);
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Can not create image processor: " + model, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createCameraSource(selectedModel);
    startCameraSource();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  private boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }

  private boolean allRuntimePermissionsGranted() {
    for (String permission : REQUIRED_RUNTIME_PERMISSIONS) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    List<String> permissionsToRequest = new ArrayList<>();
    for (String permission : REQUIRED_RUNTIME_PERMISSIONS) {
      if (!isPermissionGranted(this, permission)) {
        permissionsToRequest.add(permission);
      }
    }

    if (!permissionsToRequest.isEmpty()) {
      ActivityCompat.requestPermissions(
              this,
              permissionsToRequest.toArray(new String[0]),
              PERMISSION_REQUESTS
      );
    }
  }

}
