Google Places SDK for Android Demos
====================================
![GitHub contributors](https://img.shields.io/github/contributors/googlemaps/android-places-demos)
![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)
[![Discord](https://img.shields.io/discord/676948200904589322)](https://discord.gg/hYsWbmk)

This repo contains the following apps that demonstrate use of the [Google Places SDK for Android](https://developers.google.com/places/android-sdk/):

1. [demo-kotlin](demo-kotlin) Kotlin demo app for the Places SDK.
2. [demo-java](demo-java) Java demo app for the Places SDK.
3. [PlaceDetailsCompose](PlaceDetailsCompose) Compose demo app for the Places SDK.
4. [PlaceDetailsUIKit](PlaceDetailsUIKit) UIKit demo app for the Places SDK.
5. [PlacesUIKit3D](PlacesUIKit3D) 3D demo app for the Places SDK.

Additionally, the [snippets](snippets) app contains code snippets used for documentation found in https://developers.google.com/places/android-sdk

Getting Started
---------------

These demos use the Gradle build system.

First download the demos by cloning this repository or downloading an archived snapshot. (See the options on the right hand side.)

In Android Studio, use the "Open an existing Android Studio project", and select one of the demo directories (`demo-kotlin` or `demo-java`).

Alternatively use the `./gradlew build` command to build the project directly.

The demos also require that you add your own API key:
1. [Get an API Key](https://developers.google.com/places/android-sdk/get-api-key).
2. Open the `local.properties` file in either demo project
3. Add a single line to `local.properties` that looks like `PLACES_API_KEY=YOUR_API_KEY`, where `YOUR_API_KEY` is the API key you obtained in the first step. You can also take a look at `local.defaults.properties` as an example.
4. Build and run.

Support
-------

- Stack Overflow: https://stackoverflow.com/questions/tagged/google-places-api

If you've found an error in these samples, please file an issue:
https://github.com/googlemaps/android-places-demos/issues

Patches are encouraged, and may be submitted according to the instructions in
CONTRIBUTING.md.

If your billing address is in the European Economic Area, effective on 8 July 2025, the [Google Maps Platform EEA Terms of Service](https://cloud.google.com/terms/maps-platform/eea) will apply to your use of the Services. Functionality varies by region. [Learn more](https://developers.google.com/maps/comms/eea/faq).

License
-------

Copyright 2019 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
