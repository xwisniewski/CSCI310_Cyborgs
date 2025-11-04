⚙️ Firebase Configuration

This project uses Firebase for authentication and data storage.
Before building or running the app, you’ll need to configure Firebase locally.

🧩 Step 1: Add your Firebase configuration file

In your Firebase Console, go to Project Settings → Your apps → Android.

Download your app’s google-services.json file.

Place it in the following folder:

app/google-services.json


Do not commit this file — it’s ignored by Git for security reasons.
Instead, reference the included example file below.

📄 Step 2: Use the example template

A placeholder file is included for reference:

app/google-services.json.example


It shows the structure and key names your real configuration should have:

{
  "project_info": {
    "project_number": "YOUR_PROJECT_NUMBER_HERE",
    "project_id": "your-firebase-project-id",
    "storage_bucket": "your-project.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "YOUR_FIREBASE_APP_ID_HERE",
        "android_client_info": {
          "package_name": "com.example.csci310_teamproj"
        }
      },
      "api_key": [
        {
          "current_key": "YOUR_FIREBASE_API_KEY_HERE"
        }
      ]
    }
  ],
  "configuration_version": "1"
}


Note: Each team member must use their own Firebase project credentials.
The app will not build without a valid google-services.json.
