Google Places SDK Demos for Android
====================================

This repo contains the following apps that demonstrate use of the [Google Places SDK for Android](https://developers.google.com/places/android-sdk/):

1. [demo](https://github.com/googlemaps/android-places-demos/tree/master/demo):
Demo app for the static Places SDK.
2. [compat](https://github.com/googlemaps/android-places-demos/tree/master/compat):
Demo app for the old Google Play Services version of the Places SDK, using the new [compatibility library](https://developers.google.com/places/android-sdk/client-migration#compat).

Note that each folder contains a distinct sample and must be imported separately.

Getting Started
---------------

These demos uses the Gradle build system.

First download the demos by cloning this repository or downloading an archived
snapshot. (See the options on the right hand side.)

In Android Studio, use the "Open an existing Android Studio project", and select one of the demo directories ("demo" or "compat").

Alternatively use the "gradlew build" command to build the project directly.

Don't forget to add your API key to the gradle.properties file for each demo.

Support
-------

- Stack Overflow: https://stackoverflow.com/questions/tagged/google-places-api

If you've found an error in these samples, please file an issue:
https://github.com/googlemaps/android-places-demos/issues

Patches are encouraged, and may be submitted according to the instructions in
CONTRIBUTING.md.

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
