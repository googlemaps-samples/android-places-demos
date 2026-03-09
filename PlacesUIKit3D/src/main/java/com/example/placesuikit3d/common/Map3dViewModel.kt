// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesuikit3d.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placesuikit3d.utils.CameraUpdate
import com.example.placesuikit3d.utils.copy
import com.example.placesuikit3d.utils.toCameraUpdate
import com.example.placesuikit3d.utils.toHeading
import com.example.placesuikit3d.utils.toRange
import com.example.placesuikit3d.utils.toRoll
import com.example.placesuikit3d.utils.toTilt
import com.example.placesuikit3d.utils.toValidCamera
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnCameraChangedListener
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Camera.DEFAULT_CAMERA
import com.google.android.gms.maps3d.model.CameraRestriction
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.flyAroundOptions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration

abstract class Map3dViewModel : ViewModel() {
  abstract val TAG: String

  /**
   * The internal state flow holding the GoogleMap3D controller instance.
   *
   * This flow is used internally to manage the lifecycle and access to the
   * GoogleMap3D object provided by the MapView.
   * It's updated via the `setGoogleMap3D` function.
   *
   * Consumers should use the `mapReady` flow to react to the availability of the map.
   */
  private var _googleMap3D = MutableStateFlow<GoogleMap3D?>(null)

  private val _cameraRestriction = MutableStateFlow<CameraRestriction?>(null)
  val cameraRestriction = _cameraRestriction.asStateFlow()

  private val _mapMode = MutableStateFlow(Map3DMode.SATELLITE)
  val mapMode = _mapMode.asStateFlow()

  // --- Camera Position from Map & Pending Requests ---
  // This is guaranteed to always be a valid camera
  private val _currentCamera = MutableStateFlow(DEFAULT_CAMERA)
  val currentCamera = _currentCamera.asStateFlow()

  private val mapObjects = mutableMapOf<String, MapObject>()

  /**
   * A [MutableSharedFlow] that buffers [CameraUpdate] requests.
   *
   * This flow is used to queue camera updates requested by the ViewModel's consumers.
   * When a new camera update is emitted to this flow, it's buffered with a replay of 1,
   * meaning the latest update is available to new collectors. If a new update arrives
   * before the previous one is processed, the older one is dropped (`BufferOverflow.DROP_OLDEST`).
   *
   * This allows the ViewModel to handle camera update requests asynchronously and
   * ensures that only the most recent request is processed if updates occur rapidly.
   * The actual camera update is performed within a separate coroutine that collects
   * from this flow.
   */
  private val _pendingCameraUpdate = MutableSharedFlow<CameraUpdate?>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private val activeMapObjects = mutableMapOf<String, ActiveMapObject>()

  val mapReady = _googleMap3D.map { it != null }

  init {
    viewModelScope.launch {
      _googleMap3D.collect { controller ->
        stopAnimations()
        clearObjects()
        Log.d(TAG, "Map3D Controller attached")
        if (controller != null) {
          launch {
            Log.d(TAG, "Getting camera flow")
            getCameraFlow(controller).collect { camera ->
              _currentCamera.value = camera
            }
          }
          addMapObjects(mapObjects, controller)

          // Return to the last camera position if available
          controller.setCamera(currentCamera.value)

          // Process pending camera updates
          launch {
            _pendingCameraUpdate
              .filterNotNull()
              .collect { cameraUpdate ->
                Log.d(TAG, "Received camera update request: $cameraUpdate")
                cameraUpdate(controller)
              }
          }

          launch {
            _mapMode.collect { mapMode ->
              controller.setMapMode(mapMode)
            }
          }

          launch {
            _cameraRestriction.collect { cameraRestriction ->
              controller.setCameraRestriction(cameraRestriction)
            }
          }
        }
      }
    }
  }

  /**
   * Returns a Flow that emits the current camera position whenever it changes on the GoogleMap3D.
   *
   * This Flow is created using `callbackFlow` to bridge the callback-based API of
   * `OnCameraChangedListener` with Kotlin's coroutine Flows. It automatically attaches and
   * detaches the listener when collectors subscribe and unsubscribe.
   *
   * The Flow emits a validated `Camera` object, ensuring that the pitch, range, and bearing
   * are within acceptable limits using the `toValidCamera()` extension function.
   *
   * @param controller The GoogleMap3D instance to listen for camera changes on.
   * @return A Flow of `Camera` objects representing the current camera position.
   */
  private fun getCameraFlow(controller: GoogleMap3D): Flow<Camera> {
    // Public Flow that manages the listener lifecycle
    return callbackFlow {
      val cameraChangedListener = OnCameraChangedListener { cameraPosition ->
        val newPosition = cameraPosition.toValidCamera()
        // Send the new camera position to the flow's channel
        trySend(newPosition)
        // Also update the private state
        _currentCamera.value = newPosition
      }

      // Get the current map instance (ensure it's not null before setting listener)
      Log.d(TAG, "Attaching CameraChangeListener")
      controller.setCameraChangedListener(cameraChangedListener)

      // Ensure the initial camera position is emitted when the flow is collected
      // This handles cases where the map is ready before the flow is collected
      controller.getCamera()?.let { initial ->
        val newPosition = initial.toValidCamera()
        trySend(newPosition)
        _currentCamera.value = newPosition // Also update private state on collection
      }

      // The awaitClose block runs when the collector is cancelled
      awaitClose {
        // Remove the listener when the flow collection stops
        Log.d(TAG, "Detaching CameraChangeListener")
        controller.setCameraChangedListener(null)
      }
    }
  }

  /**
   * Adds a collection of map objects to the GoogleMap3D controller.
   *
   * This function iterates through a mutable map of MapObject instances and adds each one
   * to the provided `GoogleMap3D` controller. For each successfully added object,
   * it stores the resulting active map object in the `activeMapObjects` map
   * for later management (like removal).
   *
   * @param mapObjects A mutable map where keys are object IDs (String) and values
   *   are MapObject instances to be added to the map.
   * @param controller The GoogleMap3D controller to which the objects will be added.
   */
  private fun addMapObjects(
    mapObjects: MutableMap<String, MapObject>,
    controller: GoogleMap3D
  ) {
    mapObjects.forEach { (_, mapObject) ->
      mapObject.addToMap(controller)?.also { activeObject ->
        activeMapObjects[mapObject.id] = activeObject
      }
    }
  }

  /**
   * Sets the Map3DController instance.
   *
   * @param googleMap3d The GoogleMap3D instance, or null if it's being detached.
   */
  open fun setGoogleMap3D(googleMap3d: GoogleMap3D?) {
    _googleMap3D.value = googleMap3d
  }

  private fun stopAnimations() {
    Log.d("Map3dViewModel", "stopAnimations: ")
    _googleMap3D.value?.stopCameraAnimation()
  }

  open fun releaseGoogleMap3D() {
    _googleMap3D.value = null
  }

  /**
   * Clears the ViewModel's internal tracking of active SDK map objects.
   * This is called when the controller is detached or changed, as the underlying
   * map instance those objects belonged to is no longer relevant.
   */
  fun clearObjects() {
    activeMapObjects.forEach { (_, activeObject) ->
      activeObject.remove()
    }
    activeMapObjects.clear()
  }

  private fun addMapObject(mapObject: MapObject) {
    mapObjects[mapObject.id] = mapObject // No need to remove the old as the map will replace it
    _googleMap3D.value?.also { controller ->
      mapObject.addToMap(controller)?.also { activeObject ->
        activeMapObjects[mapObject.id] = activeObject
      }
    }
  }

  fun addMarker(options: MarkerOptions) {
    addMapObject(MapObject.Marker(options))
  }

  fun removeMapObject(id: String) {
    mapObjects.remove(id)
    activeMapObjects.remove(id)?.also { activeObject ->
      activeObject.remove()
    }
  }

  fun addPolyline(polylineOptions: PolylineOptions) {
    addMapObject(MapObject.Polyline(polylineOptions))
  }

  fun addPolygon(polygonOptions: PolygonOptions) {
    addMapObject(MapObject.Polygon(polygonOptions))
  }

  fun addModel(modelOptions: ModelOptions) {
    addMapObject(MapObject.Model(modelOptions))
  }

  fun setCamera(camera: Camera) {
    CameraUpdate.Move(camera).also { _pendingCameraUpdate.tryEmit(it) }
  }

  fun flyTo(flyToOptions: FlyToOptions) {
    CameraUpdate.FlyTo(flyToOptions).also { _pendingCameraUpdate.tryEmit(it) }
  }

  fun flyAround(flyAroundOptions: FlyAroundOptions) {
    CameraUpdate.FlyAround(flyAroundOptions).also { _pendingCameraUpdate.tryEmit(it) }
  }

  fun setCameraRestriction(cameraRestriction: CameraRestriction?) {
    _cameraRestriction.value = cameraRestriction
  }

  fun setMapMode(@Map3DMode mode: Int) {
    _mapMode.value = mode
  }

  override fun onCleared() {
    _googleMap3D.value = null
    super.onCleared()
  }

  open fun updateCameraAndMove(block: Camera.() -> Camera) {
    currentCamera.value.let { camera ->
      _pendingCameraUpdate.tryEmit(
        CameraUpdate.Move(
          camera.block() // .also { _currentCamera.value = it }
        )
      )
    }
  }

  open fun setCameraHeading(heading: Number) {
    updateCameraAndMove {
      copy(heading = heading.toHeading())
    }
  }

  open fun setCameraTilt(tilt: Number) {
    updateCameraAndMove {
      copy(heading = tilt.toTilt())
    }
  }

  open fun setCamaraRange(range: Number) {
    updateCameraAndMove {
      copy(range = range.toRange())
    }
  }

  open fun setCamaraRoll(roll: Number) {
    updateCameraAndMove {
      copy(roll = roll.toRoll())
    }
  }

  fun flyAroundCurrentCenter(rounds: Double, duration: Duration) {
    currentCamera.value.let { camera ->
      flyAround(
        flyAroundOptions {
          center = camera
          durationInMillis = duration.inWholeMilliseconds
          this.rounds = rounds
        }
      )
    }
  }

  fun getModel(key: String): Model? {
    activeMapObjects[key]?.let { activeObject ->
      if (activeObject is ActiveMapObject.ActiveModel) {
        return activeObject.model
      }
    }
    return null
  }

  fun nextMapMode() {
    val newMapType = when (mapMode.value) {
      Map3DMode.SATELLITE -> Map3DMode.HYBRID
      else -> Map3DMode.SATELLITE
    }
    setMapMode(newMapType)
  }

  suspend fun awaitFlyTo(flyToOptions: FlyToOptions) {
    awaitCameraUpdate(flyToOptions.toCameraUpdate())
  }

  suspend fun awaitFlyAround(flyAroundOptions: FlyAroundOptions) {
    awaitCameraUpdate(flyAroundOptions.toCameraUpdate())
  }

  suspend fun awaitCameraUpdate(cameraUpdate: CameraUpdate) {
    _googleMap3D.value?.let { controller ->
      com.example.placesuikit3d.utils.awaitCameraUpdate(controller, cameraUpdate)
    }
  }
}
