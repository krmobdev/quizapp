# Privacy Policy — AnimaQuiz

**Effective date:** 2026-06-28

## 1. Data collection

AnimaQuiz **does not collect, transmit, or store** any personal data on external servers. All data (progress, settings, statistics) is stored exclusively in the device's local database.

## 2. Data stored on device

The following data is stored locally (Room/SQLite):

| Data | Purpose |
|------|---------|
| Player name (entered manually) | Profile display |
| Game progress (level, coins, XP) | Progression saving |
| Quiz statistics | Statistics screen |
| Settings (language, theme, sound) | User preferences |
| Daily streak, achievements | Game mechanics |

Data is **never transmitted** outside the device.

## 3. Permissions

| Permission | Reason |
|------------|--------|
| `POST_NOTIFICATIONS` | Local daily streak reminders |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Export/import of progress backup |

## 4. Local notifications

The app uses WorkManager to send **local** reminders about the daily quiz. Notifications require no internet connection and transmit no data.

## 5. Android backup

Android may automatically back up app data to Google Drive (if enabled in device settings). The backup configuration is specified in `res/xml/backup_rules.xml`. Sensitive settings are excluded from backups.

## 6. Third-party content

Some English questions are sourced from **Open Trivia Database** (opentdb.com, CC BY-SA 4.0). Attribution is noted under Settings → Question Sources.

## 7. Children

The app is not directed to children under 13 and does not knowingly collect data from children.

## 8. Policy changes

If the privacy policy changes materially, the app version number will be updated.

## 9. Contact

For privacy inquiries: open an Issue in the project repository.
