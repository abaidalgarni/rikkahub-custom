# Custom Android build

This build adds Android image sharing to RikkaHub. The app appears as
**RikkaHub Custom**, with package `dev.abaidalgarni.rikkahub`. It keeps its own data
and signing identity. Installing it leaves the official RikkaHub app in place.

## Sharing

Select an image in Gallery or select several images in My Files, tap Share, then
choose RikkaHub Custom. Select an assistant or conversation to open a draft with
the attachments. Images are sent to a provider only when you send the message.

The selected model still needs image input support to interpret attachments.

## Updates

The prepared release workflow checks upstream stable releases daily at 06:17 UTC.
It applies `cx_image-sharing.patch`, updates the app identity and version, builds
the app, and runs JVM and Android sharing tests. A separate job signs and publishes
the APK only after those checks pass. A patch conflict or failed test stops the release.

The intended Obtainium source is:

`https://github.com/abaidalgarni/rikkahub-custom`

The app's own update feed is:

`https://github.com/abaidalgarni/rikkahub-custom/releases/latest/download/update.json`

These endpoints become available after the repository and first release are published.
Add the repository URL in Obtainium and enable background update checks. Android may
ask you to approve installation, depending on the installer permissions and system settings.
Use custom releases to retain image sharing.

Versions use `UPSTREAM_VERSION-custom.REVISION`; Android version codes use
`UPSTREAM_CODE * 10000 + REVISION`. The workflow skips any version at or below an
already published custom version, including a scheduled revision 1 after revision 2.

## Release setup

The GitHub repository needs two Actions secrets:

| Secret | Value |
| --- | --- |
| `SIGNING_KEY_BASE64` | Base64-encoded release PKCS12 keystore |
| `SIGNING_PASSWORD` | Keystore and key password |

The signing key is backed up in 1Password. Credentials, app backups, and user settings
are kept outside this repository. The build job has no signing credentials; the publish
job signs the tested APK.

Expected signing certificate SHA-256:

`30d281ceeaaf72ad1eb4cb47371f16f25da90ed97a3698a65dbd7565a713137a`

## Building

Use JDK 21, Gradle 9.6, the Android SDK required by the upstream build, Node.js 22,
and pnpm 11. Install `web-ui` dependencies with `pnpm install --frozen-lockfile`.
From the project root:

```sh
gradle :app:assembleGithubRelease :app:testGithubDebugUnitTest -PcustomRevision=2
gradle :app:connectedGithubDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=me.rerere.rikkahub.SharedImagesTest
```

The custom build removes Firebase Analytics and Crashlytics and does not require
`google-services.json`. Local signing uses the upstream `local.properties` fields;
never commit that file. CI builds an unsigned APK and signs it in the publish job.

To check a new upstream release, start with a clean checkout and run:

```sh
node /path/to/rikkahub-custom/custom/cx_apply_customizations.mjs /path/to/clean-upstream
```

After changing the app patch, update `cx_image-sharing.patch` and verify that applying
it to clean upstream source reproduces the intended app files. Build-script changes
belong in `cx_apply_customizations.mjs`.

## Migration and validation

Use Settings > Backup and Restore to transfer providers, models, chats, assistants,
search services, and MCP configuration from the official app. Workspace root filesystems
are excluded from the backup and must be recreated separately.

The 2026-11-21 local validation used a Samsung SM-S938B running Android 17:

- A Gallery image opened in a draft through the Android share sheet.
- Two images from My Files opened together in one draft.
- All nine sharing instrumentation tests passed in the debug test app.
- Release JVM tests reported 541 passed and one skipped, with no failures.
- Updating the custom app from revision 1 to revision 2 retained the migrated configuration.

No test images were submitted to an AI provider. The GitHub-hosted workflow and
Obtainium update path still need their first live run after publication.
