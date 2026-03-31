# Gemini Code Assist Style Guide: android-places-demos

This guide defines the custom code review and generation rules for the `android-places-demos` project.

## Jetpack Compose Guidelines
- **API Guidelines**: Strictly follow the [Jetpack Compose API guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md).
- **Naming**: Composable functions must be PascalCase.
- **Modifiers**: The first optional parameter of any Composable should be `modifier: Modifier = Modifier`.

## Kotlin & Java Style
- **Naming**: Use camelCase for variables and functions.
- **Documentation**: Provide KDoc for all public classes, properties, and functions. Explain the "why" in comments, not just the "what".
- **Safety**: Use null-safe operators and avoid `!!` in Kotlin. In Java, use standard null checks or `@NonNull`/`@Nullable` annotations if available.
- **Imports vs FQCNs**: Avoid using Fully Qualified Class Names (FQCNs) in code if a standard `import` statement would suffice. Keep code readable.

## Places SDK Specifics
- **Secrets**: Never commit API keys. Ensure they are read from `secrets.properties` (or `local.properties`) via `BuildConfig` or injected into `AndroidManifest.xml` by the Secrets Gradle Plugin.
- **Initialization**:
  - Strongly recommend `Places.initializeWithNewPlacesApiEnabled` over the legacy `Places.initialize` for modern demos.
- **Literate Programming**: Write clear, well-documented code that functions as an example for developers. Explain architectural decisions.
