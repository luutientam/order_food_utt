# Auth Test Cases (Production-like)

## 1) Email/Password Sign Up

- **TC01 - Sign up success (customer)**
  - Input: valid email, password >= 6, role customer
  - Expected: account created, profile created in `/users/{uid}`, auto-login to customer app
- **TC02 - Sign up success (shipper)**
  - Input: valid email, password >= 6, role shipper
  - Expected: account created, role shipper persisted, redirect to shipper app
- **TC03 - Sign up admin with invalid domain**
  - Input: role admin, email not ending `@admin.com`
  - Expected: block submit, show `msg_email_invalid_admin`
- **TC04 - Sign up with short password**
  - Input: password < 6 chars
  - Expected: block submit, show `msg_password_too_short`
- **TC05 - Sign up with existing email**
  - Input: email already used
  - Expected: show `msg_sign_up_email_exists`

## 2) Email/Password Sign In

- **TC06 - Sign in success with correct role**
  - Input: valid email/password and selected role equals stored role
  - Expected: login success, redirect by role
- **TC07 - Sign in with wrong password**
  - Input: existing email + wrong password
  - Expected: show `msg_sign_in_invalid_credentials`
- **TC08 - Sign in with wrong selected role**
  - Input: valid account but selected role mismatch
  - Expected: block navigation, show `msg_sign_in_wrong_role`
- **TC09 - Sign in locked account**
  - Setup: set `isActive=false` in `/users/{uid}`
  - Expected: show `msg_account_locked`

## 3) Google Sign In / Sign Up

- **TC10 - Google sign in success**
  - Input: valid Google account with existing profile
  - Expected: login success and role routing works
- **TC11 - Google sign up success (new account)**
  - Input: Google account without profile, role customer/shipper
  - Expected: create profile and login success
- **TC12 - Google sign up existing profile**
  - Input: Google account already has profile
  - Expected: treat as login success (no fail loop)
- **TC13 - Google sign up admin blocked**
  - Input: role admin + tap sign up Google
  - Expected: block and show `msg_google_admin_not_supported`
- **TC14 - Google OAuth/SHA misconfiguration**
  - Setup: invalid SHA or wrong `google-services.json`
  - Expected: message indicates OAuth/SHA configuration issue

## 4) Robustness / Reliability

- **TC15 - Network disconnected**
  - Action: sign in/sign up with no internet
  - Expected: show `msg_auth_network_error`
- **TC16 - Too many failed attempts**
  - Action: trigger Firebase throttling
  - Expected: show `msg_auth_too_many_requests`
- **TC17 - App relaunch after login**
  - Action: close app then reopen
  - Expected: Splash restores session and routes by role
