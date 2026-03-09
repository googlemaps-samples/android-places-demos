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

package com.example.placesuikit3d.utils

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnCameraAnimationEndListener
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Represents an update to the camera of a [GoogleMap3D].
 *
 * This sealed class provides different ways to update the camera, such as flying to a specific location,
 * flying around a point, or simply moving the camera to a new position.
 *
 * The main advantage is to allow creation of the [awaitCameraUpdate] method.
 *
 * Each subclass of [CameraUpdate] defines how the camera should be updated through its `invoke` method.
 *
 * Subclasses:
 * - [FlyTo]: Represents a camera fly-to animation.
 * - [FlyAround]: Represents a camera fly-around animation.
 * - [Move]: Represents a direct camera move without animation.
 */
sealed class CameraUpdate {
    abstract operator fun invoke(controller: GoogleMap3D)

    data class FlyTo(val options: FlyToOptions) : CameraUpdate() {
        override fun invoke(controller: GoogleMap3D) {
            controller.flyCameraTo(options)
        }
    }

    data class FlyAround(val options: FlyAroundOptions) : CameraUpdate() {
        override fun invoke(controller: GoogleMap3D) {
            controller.flyCameraAround(options)
        }
    }

    data class Move(val camera: Camera) : CameraUpdate() {
        override fun invoke(controller: GoogleMap3D) {
            controller.setCamera(camera)
        }
    }
}

fun FlyToOptions.toCameraUpdate(): CameraUpdate {
    return CameraUpdate.FlyTo(this.toValidFlyToOptions())
}

fun FlyAroundOptions.toCameraUpdate(): CameraUpdate {
    return CameraUpdate.FlyAround(this.toValidFlyAroundOptions())
}

fun FlyToOptions.toValidFlyToOptions(): FlyToOptions {
    return this.copy(
        endCamera = this.endCamera.toValidCamera()
    )
}

fun FlyAroundOptions.toValidFlyAroundOptions(): FlyAroundOptions {
    return this.copy(
        center = this.center.toValidCamera()
    )
}

/**
 * Suspends the coroutine until the camera update animation is finished.
 *
 * If the [cameraUpdate] is a [CameraUpdate.Move], it will be applied immediately without waiting.
 *
 * Otherwise, it will wait for the camera animation to finish, then it will resume the coroutine.
 *
 * You can pass in an existing [cameraChangedListener] that will be invoked when the camera
 * animation finishes and also will be restored afterwards.
 *
 * @param controller The [GoogleMap3D] instance to apply the camera update to.
 * @param cameraUpdate The [CameraUpdate] to apply.
 * @param cameraChangedListener An optional existing listener to invoke and restore
 */
suspend fun awaitCameraUpdate(
    controller: GoogleMap3D,
    cameraUpdate: CameraUpdate,
    cameraChangedListener: OnCameraAnimationEndListener? = null
) = suspendCancellableCoroutine { continuation ->
    // No need to wait if the update is a move
    if (cameraUpdate is CameraUpdate.Move) {
        cameraUpdate.invoke(controller)
        return@suspendCancellableCoroutine
    }

    // If the coroutine is canceled, stop the camera animation as well.
    continuation.invokeOnCancellation {
        controller.stopCameraAnimation()
    }

    controller.setCameraAnimationEndListener {
        cameraChangedListener?.onCameraAnimationEnd()
        controller.setCameraAnimationEndListener(cameraChangedListener)
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }

    cameraUpdate.invoke(controller)
}
