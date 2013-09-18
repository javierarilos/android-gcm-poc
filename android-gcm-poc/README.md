android-gcm-poc
===============

This is the Android application for the android-gcm-poc project.

Getting started, some references: 

For using this code, you will need to add to **MainActivity.java**:
* PROJECT_NUMBER
* AUTHORIZATION-KEY
see: http://developer.android.com/google/gcm/gs.html#libs for getting this keys.

 
* You will also need some phones registration-id's, which result from the android device register.

How to a gcm message from command line:
curl --header "Authorization:key=<YOUR-AUTHORIZATION-KEY-HERE>" --header "Content-Type:application/json" "https://android.googleapis.com/gcm/send" --data '{"data": { "hello": "android from sprayer 27" }, "registration_ids":["<A-DEVICE-REGISTRATION-ID>"]}'

