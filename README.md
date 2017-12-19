# ThaparCapstoneProject
This is the Android code of our capstone project at Thapar University

GitHub project link: https://github.com/kagarwal96/capstone

*Note: Install Android Studio with google Map and Firebase with it*
## How to use the application:
1. First changes need to be done in capstone/app/src/main/java/kushagra/capstone/MapsActivity.java. In mMap.setOnMarkerClickListener() at the Download image url, change URL at appropiate position.
2. Now install the application in your android phone.
3. There will be a map displayed where your quadcopter location and your current location will be showed.
4. On clicking at any point in map, a (red)marker will be added. You select the area by adding markers.
5. After that click send button to send the area selected to firebase database.
6. Once area is sent, the quadcopter will start flying. The logs of quadcopter are sent as Toasts to the android application. Aerial images taken by quadcopter at locations marked by blue markers can be seen by clicking the blue markers.
7. Movement of quadcopter can be seen in map as a quadcopter marker in android application.
8. To remove the markers just long click at any place in map.
