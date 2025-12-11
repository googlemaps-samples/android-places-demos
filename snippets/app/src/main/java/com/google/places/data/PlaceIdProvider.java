// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.places.data;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Provides a list of sample Google Place IDs for testing.
 * The list is parsed from a raw string and lazily initialized on first access.
 * <p>
 * To generate a list of nearby places, you can use the following script with curl:
 * <pre>
 * #!/bin/bash
 *
 * API_KEY=$(cat ~/google_api_key.txt)
 *
 * curl -X POST -d '{
 *   "locationRestriction": {
 *     "circle": {
 *       "center": {
 *         "latitude": 40.0150,
 *         "longitude": -105.2705
 *       },
 *       "radius": 1500.0
 *     }
 *   }
 * }' \
 * -H 'Content-Type: application/json' \
 * -H 'X-Goog-FieldMask: places.id' \
 * "https://places.googleapis.com/v1/places:searchNearby?key=$API_KEY"
 * </pre>
 */
public class PlaceIdProvider {

    // The raw data is stored in a private static final string.
    // Using \n makes it compatible with older Java versions that lack text blocks.
    private static final String RAW_PLACE_IDS = """
            ChIJwR6cajTsa4cR2TH0qKTVKAM
            ChIJiTEGLibsa4cRepH7ZMFEcJ8
            ChIJ01j9ptfta4cRIKZGWw-Gkq4
            ChIJHzUT3tbta4cRGIqqS1UAjkE
            ChIJ4bbaBcnta4cR0LKO770ALRQ
            ChIJ4-dlTy_sa4cRd978vIqVG1Y
            ChIJvw3XCdHta4cREEupPNyRDg0
            ChIJ6bmRoybsa4cRF2M_QGtaSYY
            ChIJvQ9WKSnta4cR7n55uCAYPL4
            ChIJE6YJGNDta4cRF2x0W8c8DAI
            ChIJH68pxyfsa4cR-EIpOWL5Umc
            ChIJG3SvINLta4cR3PNcgxz9lLk
            ChIJ5TW3jDDsa4cRkSewKsCsNSE
            ChIJgbfb4dPta4cRlixOyQ-DOUo
            ChIJ2yMKRSTsa4cRSCSGwX1rRUI
            ChIJ4xyxGbTta4cRu-XnmlmC5EI
            ChIJWbbcvijsa4cR7bHu3lilcFA
            ChIJ00Gjeyjsa4cRvLYGmRIQ92o
            ChIJA8ksXNHta4cR8MkYLipiZL0
            ChIJc0Gm1tbta4cRxmXvsBRtU7M""";

    // The list is declared as 'volatile' and starts as null.
    // 'volatile' ensures changes are visible across all threads.
    private static volatile List<String> placeIds = null;

    // A single Random instance is more efficient than creating one each time.
    private static final Random RANDOM = new Random();

    /**
     * Gets the list of place IDs, initializing it only on the first call.
     * This method is thread-safe.
     *
     * @return A non-null, unmodifiable list of place IDs.
     */
    public static List<String> getPlaceIds() {
        // Use a common pattern called "double-checked locking" for lazy initialization.
        if (placeIds == null) {
            synchronized (PlaceIdProvider.class) {
                // Check again inside the lock in case another thread initialized it.
                if (placeIds == null) {
                    // This is the compatible way for older Java.
                    // The "\\R" regex splits on any universal newline sequence.
                    placeIds = Arrays.asList(RAW_PLACE_IDS.split("\\R"));
                }
            }
        }
        return placeIds;
    }

    /**
     * Gets a single random place ID from the list.
     *
     * @return A random place ID string.
     */
    public static String getRandomPlaceId() {
        List<String> ids = getPlaceIds(); // This ensures the list is initialized.
        if (ids.isEmpty()) {
            throw new IllegalStateException("Place ID list is empty.");
        }
        // Get a random element from the list.
        return ids.get(RANDOM.nextInt(ids.size()));
    }
}