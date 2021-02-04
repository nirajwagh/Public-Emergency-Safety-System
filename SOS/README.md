# SOS

Apk Download: https://github.com/nirajwagh/SOS/raw/master/Apk%20Download/SOS.apk

An SOS demo app for requesting emergency services when in an emergency.

Citizens can press the SOS button for calling a cop who is present nearest to the citizen's location.

Device location data is used for requesting the service and Live location will be shown to both, cop and citizen of each other.

The estimated time for the cop to reach the citizen will be shown in the citizen app and the estimated distance to reach the citizen will be shown in the cop app.

Firebase realtime database is used for Live location service.

## Screenshots
<table>
        
  <tr>
  <td>
  <img src="/SOS/Screenshots/loading.png" align="top">
  
  Splash screen.
  </td>
  <td>
  <img src="/SOS/Screenshots/home.png" align="top">
  
  Home screen
  </td>
  <td>
  <img src="/SOS/Screenshots/finding%20cop.png" align="top">
  
  Finding Cop
  </td>
  <td>
  <img src="/SOS/Screenshots/live%20tracking.png" align="top">

  Live Tracking
  </td>
  </tr>
  
  <tr>
  <td>
  <img src="/SOS/Screenshots/solved.png" align="top">
  
  Complaint Solved
  </td>
  <td>
  <img src="/SOS/Screenshots/reporting%20cop.png" align="top">
  
  Reporting the Cop
  </td>
  <td>
  <img src="/SOS/Screenshots/report.png" align="top">
  
  Finding Nearest police station
  </td>
  <td>
  <img src="/SOS/Screenshots/previous%20complaints.png" align="top">
  
  Previous Complaints
  </td>
  </tr>

  <tr>
  <td>
  <img src="/SOS/Screenshots/reporting%20cop.png" align="top">
  
  Reporting Cop
  </td>
  <td>
  <img src="/SOS/Screenshots/sign%20in.png" align="top">
  Sign In
  
  </td>
  <td>
  <img src="/SOS/Screenshots/register.png" align="top">
  
  Register
  </td>
  <td>
  <img src="/SOS/Screenshots/navbar.png" align="top">
  
  Navbar
  </td>
  
  <tr>
        
 </table>
 
 
 SOS

ERROR #1

		A problem occurred evaluating project ':app'.
		> C:\...\apikey.properties (The system cannot find the file specified)


 create file named "apikey.properties" in the root directory(Cop_SOS) and following line in the file:

 	GOOGLE_MAPS_API_KEY = "paste-your-google-maps-api-key-here"

 Here replace paste-your-google-maps-api-key-here with your google maps api key. Save the file  


 ERROR #2

		Execution failed for task ':app:processDebugGoogleServices'.
		> File google-services.json is missing. The Google Services Plugin cannot function without it. 
 	
(Ignore step 1,2 if already done with the COP-SOS app)
 1. Create a google firebase account.
 2. Login the Android studio using the same google account used for Firebase in step 1.
 3. In android studio, go to Tools -> Firebase. Now Firebase assistant will open.
 4. Select "authentication using a custom authentication system".
 5. Click on "Connect to Firebase". This will redirect you to your browser and will open the Firebase console.
 6. Now, if you have already created Firebase project for the SOS app, then choose that project in the Firebase console. 
 Else if this is your first project then Create a new project and connect your android project to this Firebase console project.


Enable Firebase Realtime Database.

(Ignore step 1,2,3,4 if already done with the COP-SOS app)
1. Go to your Firebase Console and under Realtime Database, select "create Database".
2. Choose Database location and under "Security Rules" select "start in test mode".
3. Download the "realtimeDbData.json" file from the project repository.
4. Under Firebase realtime database section, click on the three dots and import the JSON file by choosing the "import JSON" option. 
5. In android studio, open Firebase assistant and under "Realtime Database", select "Get started with realtime database". 
6. Select "Add the Realtime Database SDK to your app". Click on "accept changes" in the dialog box.


Register and Login users using Firebase Authentiaction

(Ignore step 1,2,3 if already done with the COP-SOS app)
1. Go to your Firebase console and choose your project.
2. Select Authentication and enable it.
3. Under Autentication -> Sign-in method, enable the "Email/Password" and "Phone" method.
4. In android studio, open Firebase assistant and select "authentication using a custom authentication system".
5. Then select the "Add the Firebase Authentication SDK to your app". Click on "accept changes" in the dialog box.


Uploading Profile images and other images.

(Ignore step 1,2,3 if already done with the COP-SOS app)
1. Go to firebase console and under "storage" click on "get started".
2. Click "next" and choose a location for the cloud server.
3. Now you will be able to store images on the Firebase cloud service.
4. Go to "profile" section in the app and try uploading profile pic.

Adding Google Maps API KEY

1. Go To "/app/src/debug/res/values/google_maps_api.xml" and replace "PASTE YOUR GOOGLE MAPS API KEY HERE" with you API Key.
2. Go To "/app/src/release/res/values/google_maps_api.xml" and replace "PASTE YOUR GOOGLE MAPS API KEY HERE" with you API Key.

        
