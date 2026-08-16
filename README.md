# Royal Coupon Automation

A small Android Studio starter project for sequentially processing up to 200 user-provided codes through a visible UI workflow.

## What it does

- Accepts up to 200 codes, one per line.
- Start / Pause / Stop.
- Shows progress.
- Uses Android AccessibilityService to find an editable field and a clearly labelled OK button.
- Waits a configurable number of seconds before moving to the next code.

## Setup

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Build and install the debug APK.
4. Open the app and tap **OPEN ACCESSIBILITY SETTINGS**.
5. Enable **Royal Coupon Automation**.
6. Return to the app and paste the codes, one per line.
7. Open the target app's visible coupon/redeem screen.
8. Press START.

## Important limitations

This starter intentionally does not:
- bypass CAPTCHA or anti-bot protections;
- defeat login/security controls;
- scrape private Telegram content;
- attempt hidden/background actions;
- guarantee compatibility with a third-party app whose UI changes.

The automation relies on the target app exposing standard Android accessibility nodes. If its coupon field or OK button is not exposed that way, the selectors need to be adapted to that app's UI.

Use only with accounts, codes, and services you are authorized to use and within the service's rules.


## Online APK build (no Android Studio)

This project includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.

1. Create a GitHub repository.
2. Upload all files and folders from this project.
3. Open the repository's **Actions** tab.
4. Select **Build Android APK**.
5. Choose **Run workflow** (or push to `main`/`master`).
6. After the workflow finishes, open the run and download the artifact named `RoyalCouponAutomation-debug`.
7. Extract the artifact and install the APK on your Android phone.

### Important
The workflow builds the current debug version. A production/release APK should be signed with your own keystore before distribution.
