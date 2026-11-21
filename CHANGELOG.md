# Changelog

## 3.3.0-custom.3 - 2026-11-21

- Use the available debug JVM test task in the hosted release workflow. The release
  APK and Android sharing checks remain separate build steps.
- Build an unsigned APK when no local keystore is configured, so signing credentials
  remain confined to the publishing job.
- Published the first fully automated signed release after 541 JVM tests passed
  (one skipped) and all nine Android sharing tests passed on the hosted emulator.
- Verified the revision 2 to 3 update through Obtainium on Android 17, including
  the installed APK checksum, retained configuration and chats, and Gallery sharing.

## 3.3.0-custom.2 - 2026-11-21

- Added Android sharing for one or multiple images, with caption support and attachment previews.
- Added URI validation, duplicate removal, and tests for shared image handling.
- Kept camera shortcut sharing working with typed URI extras.
- Used a separate app package, signing identity, workspace label, and custom update feed.
- Removed Firebase Analytics and Crashlytics from this custom build.
- Added a daily upstream-release workflow that reapplies the patch, tests, signs,
  and publishes custom updates.
- Verified Gallery and My Files sharing on Android 17, all nine sharing tests, and
  541 JVM tests (one additional test skipped).
