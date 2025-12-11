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

## Terms of Service

This sample uses Google Maps Platform services. Use of Google Maps Platform services through this sample is subject to the Google Maps Platform [Terms of Service].

If your billing address is in the European Economic Area, effective on 8 July 2025, the [Google Maps Platform EEA Terms of Service](https://cloud.google.com/terms/maps-platform/eea) will apply to your use of the Services. Functionality varies by region. [Learn more](https://developers.google.com/maps/comms/eea/faq).

This sample is not a Google Maps Platform Core Service. Therefore, the Google Maps Platform Terms of Service (e.g. Technical Support Services, Service Level Agreements, and Deprecation Policy) do not apply to the code in this sample.

## Support

This sample is offered via an open source [license]. It is not governed by the Google Maps Platform Support [Technical Support Services Guidelines], the [SLA], or the [Deprecation Policy]. However, any Google Maps Platform services used by the sample remain subject to the Google Maps Platform Terms of Service.

If you find a bug, or have a feature request, please [file an issue] on GitHub. If you would like to get answers to technical questions from other Google Maps Platform developers, ask through one of our [developer community channels]. If you'd like to contribute, please check the [contributing guide].

You can also discuss this sample on our [Discord server].

[API key]: https://developers.google.com/maps/documentation/android-sdk/get-api-key
[API key instructions]: https://developers.google.com/maps/documentation/android-sdk/config#step_3_add_your_api_key_to_the_project

[code of conduct]: CODE_OF_CONDUCT.md
[contributing guide]: CONTRIBUTING.md
[Deprecation Policy]: https://cloud.google.com/maps-platform/terms
[developer community channels]: https://developers.google.com/maps/developer-community
[Discord server]: https://discord.gg/hYsWbmk
[file an issue]: https://github.com/googlemaps-samples/android-places-demos/issues/new/choose
[license]: LICENSE
[pull request]: https://github.com/googlemaps-samples/android-places-demos/compare
[project]: https://developers.google.com/maps/documentation/android-sdk/cloud-setup#enabling-apis
[Sign up with Google Maps Platform]: https://console.cloud.google.com/google/maps-apis/start
[SLA]: https://cloud.google.com/maps-platform/terms/sla
[Technical Support Services Guidelines]: https://cloud.google.com/maps-platform/terms/tssg
[Terms of Service]: https://cloud.google.com/maps-platform/terms
[Google Maps Platform EEA Terms of Service]: https://cloud.google.com/terms/maps-platform/eea
[Learn more]: https://developers.google.com/maps/comms/eea/faq
