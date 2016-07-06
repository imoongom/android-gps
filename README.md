# android-gps
Location finding Android app that communicates to an Apache web server via TCP/IP

###Team 
- Krystle Bulalakaw
- Oscar Kwan
- Gabriel Lee
- Eunwon Moon

###Modules and Components
- Android
  - Contains the Android Studio project for the GpsTrack client application.
  - Includes Java source code, layout resources, Android manifest configuration, and APK. 
- Server
  - src contains Java source code for the UDP server to listen for datagrams, update the database and echo info back to the client.
  - lib contains jar executable.
- Webpage
  - Contains the web application files to view all GpsTrack clients on Google Maps.
  - Source code in HTML and PHP to connect to database (registration, login, user-specific data).
  - Additional fonts, images, CSS and JavaScript libraries for UI.
- Documentation
  - Design work (state diagrams and pseudocode)
  - Test Report
  - User Guide
