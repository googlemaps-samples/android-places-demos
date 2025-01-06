[![Build](https://github.com/googlemaps-samples/android-places-demos/actions/workflows/build.yml/badge.svg)](https://github.com/googlemaps-samples/android-places-demos/actions/workflows/build.yml)

![GitHub contributors](https://img.shields.io/github/contributors/googlemaps-samples/android-places-demos?color=green)
[![GitHub License](https://img.shields.io/github/license/googlemaps-samples/android-places-demos?color=blue)][license]
[![StackOverflow](https://img.shields.io/stackexchange/stackoverflow/t/google-places-api?color=orange&label=google-places-api&logo=stackoverflow)](https://stackoverflow.com/questions/tagged/google-places-api)
[![Discord](https://img.shields.io/discord/676948200904589322?color=6A7EC2&logo=discord&logoColor=ffffff)][Discord server]

# Google Places SDK for Android sample applications

## Description

This repo contains sample apps demonstrating use of the [Google Places SDK for Android][places-sdk].

## Samples in this repo

1. [demo-kotlin](demo-kotlin) Kotlin demo app for the Places SDK.
1. [demo-java](demo-java) Java demo app for the Places SDK.

Note that each folder contains a distinct sample and must be imported separately. Each project also contains two Gradle product flavors:

1. `gms`: Product flavor for samples using the Places SDK that uses Maps SDK for Android
2. `v3`: Product flavor for samples using the Places SDK that uses Maps SDK V3 BETA for Android

Additionally, the [snippets](snippets) app contains code snippets used for documentation found in https://developers.google.com/places/android-sdk

## Requirements

To run the samples, you will need:

- To [sign up with Google Maps Platform]
- A Google Maps Platform [project] with the **Places SDK for Android** enabled
- An [API key] associated with the project above ... follow the [API key instructions] if you're new to the process
- Java 21+ or Kotlin
- Android API level 23+ (35+ recommended)
- Gradle

## Running the sample(s)

These demos use the Gradle build system.

First download the demos by cloning this repository or downloading an archived snapshot. (See the options on the right hand side.)

In Android Studio, use the "Open an existing Android Studio project", and select one of the demo directories (`demo-kotlin` or `demo-java`).

Alternatively use the `./gradlew build` command to build the project directly.

1. Open the `local.properties` file in either demo project
1. Add a single line to `local.properties` that looks like `PLACES_API_KEY=YOUR_API_KEY`, where `YOUR_API_KEY` is the API key you obtained earlier. You can also take a look at `local.defaults.properties` as an example.
1. Build and run

## Contributing

Contributions are welcome and encouraged! If you'd like to contribute, send us a [pull request] and refer to our [code of conduct] and [contributing guide].

## Terms of Service

This sample uses Google Maps Platform services. Use of Google Maps Platform services through this sample is subject to the Google Maps Platform [Terms of Service].

This sample is not a Google Maps Platform Core Service. Therefore, the Google Maps Platform Terms of Service (e.g. Technical Support Services, Service Level Agreements, and Deprecation Policy) do not apply to the code in this sample.

## Support

This sample is offered via an open source [license]. It is not governed by the Google Maps Platform Support [Technical Support Services Guidelines], the [SLA], or the [Deprecation Policy]. However, any Google Maps Platform services used by the sample remain subject to the Google Maps Platform Terms of Service.

If you find a bug, or have a feature request, please [file an issue] on GitHub. If you would like to get answers to technical questions from other Google Maps Platform developers, ask through one of our [developer community channels]. If you'd like to contribute, please check the [contributing guide].

You can also discuss this sample on our [Discord server].

[places-sdk]: https://developers.google.com/places/android-sdk
[API key]: https://developers.google.com/places/documentation/android-sdk/get-api-key
[API key instructions]: https://developers.google.com/places/documentation/android-sdk/config#step_3_add_your_api_key_to_the_project

[code of conduct]: ?tab=coc-ov-file#readme
[contributing guide]: CONTRIBUTING.md
[Deprecation Policy]: https://cloud.google.com/maps-platform/terms
[developer community channels]: https://developers.google.com/maps/developer-community
[Discord server]: https://discord.gg/hYsWbmk
[file an issue]: https://github.com/googlemaps/android-places-demos/issues/new/choose
[license]: LICENSE
[pull request]: https://github.com/googlemaps/android-places-demos/compare
[project]: https://developers.google.com/maps/documentation/android-sdk/cloud-setup#enabling-apis
[Sign up with Google Maps Platform]: https://console.cloud.google.com/google/maps-apis/start
[SLA]: https://cloud.google.com/maps-platform/terms/sla
[Technical Support Services Guidelines]: https://cloud.google.com/maps-platform/terms/tssg
[Terms of Service]: https://cloud.google.com/maps-platform/terms

