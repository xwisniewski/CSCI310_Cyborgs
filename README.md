README (Copy-Paste Version)

CSCI 310 – BestLLM Android Application
Team Cyborgs (#20): Manas Garg, Veer Vora, Aditya Kabra, Kush Kumar, Xavier Wisniewski

Overview

BestLLM is an Android application that allows USC students to share, discuss, and vote on Large Language Model (LLM) prompts. The application includes user authentication, posts and prompts, comments, upvotes and downvotes, filtering and searching, and profile management.

This README explains how to run the app, how to configure Firebase, and how to execute both black-box (Espresso) and white-box (JUnit) tests.

Running the Application

Step 1: Open the project in Android Studio
– Select File → Open and choose the CSCI310_TeamProj directory.

Step 2: Configure Firebase (Required to build and run)

In Firebase Console, navigate to Project Settings → Your Apps → Android.

Download your google-services.json file.

Place it here in the project:

app/google-services.json

Important:
This file is ignored by Git for security. Each team member must provide their own.

A template reference file is included:

app/google-services.json.example

This example shows the necessary structure (project_number, project_id, mobilesdk_app_id, api_key, and package_name). Use it to verify your file is formatted correctly.

Step 3: Build and Run

Launch an emulator (recommended: Pixel 2, API 34).

Click Run in Android Studio.

The app should launch into the Login screen.

Running Test Cases

The project includes two test suites:

• Black-box tests (Espresso) – located under androidTest
• White-box tests (JUnit) – located under test

3.1 Black-Box Tests (Espresso UI Tests)

These tests require an emulator because they interact with the UI, Toasts, navigation, and authentication flows.

How to run black-box tests:

Open the project in Android Studio.

Start an emulator: Tools → Device Manager → Start Emulator.

Navigate to:

app/src/androidTest/java/com/example/csci310_teamproj/

Right-click the com.example.csci310_teamproj folder.

Select: Run 'Tests in com.example.csci310_teamproj'.

Espresso will launch the app automatically and execute all black-box tests.

Verification:
In the Run panel, you should see that all test cases run and pass.

3.2 White-Box Tests (JUnit Local Unit Tests)

These tests run on the JVM and do not require an emulator.

How to run white-box tests:

Open the project in Android Studio.

Navigate to:

app/src/test/java/com/example/csci310_teamproj/

Right-click the com.example.csci310_teamproj folder.

Select: Run 'Tests in com.example.csci310_teamproj'.

All white-box tests will execute instantly.

Verification:
In the Run panel, you should see that all test cases run and pass.

Project Structure

CSCI310_TeamProj/
app/
src/
main/
test/ (white-box tests, JUnit)
androidTest/ (black-box tests, Espresso)
build.gradle.kts
google-services.json (must be added manually)
gradle/
build.gradle.kts
README.md

Notes

• google-services.json must be added manually by each developer.
• Do not change the .gitignore to include Firebase credentials.
• All test suites assume TESTING_MODE is enabled (already implemented).
• The emulator must be running before executing Espresso tests.
• The app will not build without a valid google-services.json.
