# Android release signing

DawaDiary must not use the Android debug keystore to sign a production release.

The Gradle release build reads these environment variables:

- `DAWADIARY_RELEASE_STORE_FILE`: path to the private keystore file available to the build.
- `DAWADIARY_RELEASE_STORE_PASSWORD`: keystore password.
- `DAWADIARY_RELEASE_KEY_ALIAS`: private key alias.
- `DAWADIARY_RELEASE_KEY_PASSWORD`: private key password.

All four values are required for a signed release. If any are missing, Gradle deliberately creates an **unsigned** release APK. This is useful for CI build validation, but it is not ready for installation or distribution as a signed production update.

For GitHub Actions, store the passwords and alias as repository/environment secrets. Make the keystore file available to the job from a protected secret (for example, a base64-encoded keystore secret decoded to a temporary file), then set `DAWADIARY_RELEASE_STORE_FILE` to that file path. Never commit the keystore, passwords, or encoded keystore contents to the repository.

**Release blocker:** the project owner must create and securely retain the production keystore, configure the protected CI secrets, and verify a signed release before publishing. Losing the keystore can prevent updates to an already-published app.
