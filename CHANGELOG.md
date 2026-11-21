# Changelog

## 3.3.0-custom.3 - 2026-11-21

- Use the available debug JVM test task in the hosted release workflow. The release
  APK and Android sharing checks remain separate build steps.

## 3.3.0-custom.2 - 2026-11-21

- Added Android sharing for one or multiple images, with caption support and attachment previews.
- Added URI validation, duplicate removal, and tests for shared image handling.
- Kept camera shortcut sharing working with typed URI extras.
- Used a separate app package, signing identity, workspace label, and custom update feed.
- Removed Firebase Analytics and Crashlytics from this custom build.
- Prepared a daily upstream-release workflow that reapplies the patch, tests, signs,
  and publishes custom updates. Publication and its first live run remain pending.
- Verified Gallery and My Files sharing on Android 17, all nine sharing tests, and
  541 JVM tests (one additional test skipped).
