# CopSOS

Apk Download: https://github.com/nirajwagh/CopSOS/raw/master/Apk%20Download/Cop%20Sos.apk

A SOS Cop demo app for accepting the complaint by the cop.

Cop can go On-Duty for accepting the complaints.

Device location data is used for accepting the service and Live location will be shown to both, cop and citizen.

The estimated distance reach the citizen will be shown in the cop app.

Firebase realtime database is used for Live location service.


# Screenshots

<table>
        
  <tr>
  <td>
  <img src="/CopSOS/Screenshots/loading.png" align="top">
  
  Splash screen.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/Cop%20status.png" align="top">
  
  Home screen.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/COp%20Online.png" align="top">
  
  Cop Online.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/new%20complaint%20notification.png" align="top">

  New Complaint.
  </td>
  </tr>
  
  <tr>
  <td>
  <img src="/CopSOS/Screenshots/live%20tracking.png" align="top">
  
  Live Tracking.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/citizen%20found%20notification.png" align="top">
  
  Citizen Found Notification.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/complaint%20solved.png" align="top">
  
  Complaint Solved.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/navigation.png" align="top">
  
  Navigation for Citizen Location.
  </td>
  </tr>

  <tr>
  <td>
  <img src="/CopSOS/Screenshots/compliant%20canceldd.png" align="top">
  
  Complaint Cancelled.
  </td>
  <td>
  <img src="/CopSOS/Screenshots/cop%20profile.png" align="top">
  Cop Profile
  
  </td>
  <td>
  <img src="/CopSOS/Screenshots/cop%20reported.png" align="top">
  
  Cop Reported
  </td>
  <td>
  <img src="/CopSOS/Screenshots/reason%20pic.png" align="top">
  
  Reported reason Photo
  </td>
  
  <tr>
  <td>
  <img src="/CopSOS/Screenshots/forward%20comlaint.png" align="top">
  
  Forward Complaint
  </td>
  <td>
  <img src="/CopSOS/Screenshots/reason%20for%20forward.png" align="top">
  
  Reason For Forward
  </td>
  <td>
  <img src="/CopSOS/Screenshots/manual%20complaint.png" align="top">
  
  Manual Complaint
  </td>
  <td>
  <img src="/CopSOS/Screenshots/Signin.png" align="top">
  
  Sign In

  </td>
  </tr>
  
  
 
  </table>
  
  
  COP SOS

ERROR #1

		A problem occurred evaluating project ':app'.
		> C:\...\apikey.properties (The system cannot find the file specified)


 create file named "apikey.properties" in the root directory(Cop_SOS) and following line in the file:

 	GOOGLE_MAPS_API_KEY = "paste-your-google-maps-api-key-here"

 Here replace paste-your-google-maps-api-key-here with your google maps api key. Save the file  	


ERROR #2

		Execution failed for task ':app:processDebugGoogleServices'.
		> File google-services.json is missing. The Google Services Plugin cannot function without it. 
 	
(Ignore step 1,2 if already done with the SOS app)
 1. Create a google firebase account.
 2. Login the Android studio using the same google account used for Firebase in step 1.
 3. In android studio, go to Tools -> Firebase. Now Firebase assistant will open.
 4. Select "authentication using a custom authentication system".
 5. Click on "Connect to Firebase". This will redirect you to your browser and will open the Firebase console.
 6. Now, if you have already created Firebase project for the SOS app, then choose that project in the Firebase console. 
 Else if this is your first project then Create a new project and connect your android project to this Firebase console project.


Enable Firebase Realtime Database.

(Ignore step 1,2,3,4 if already done with the SOS app)
1. Go to your Firebase Console and under Realtime Database, select "create Database".
2. Choose Database location and under "Security Rules" select "start in test mode".
3. Download the "realtimeDbData.json" file from the project repository.
4. Under Firebase realtime database section, click on the three dots and import the JSON file by choosing the "import JSON" option. 
5. In android studio, open Firebase assistant and under "Realtime Database", select "Get started with realtime database". 
6. Select "Add the Realtime Database SDK to your app". Click on "accept changes" in the dialog box.


Register and Login users using Firebase Authentiaction

(Ignore step 1,2,3 if already done with the SOS app)
1. Go to your Firebase console and choose your project.
2. Select Authentication and enable it.
3. Under Autentication -> Sign-in method, enable the "Email/Password" and "Phone" method.
4. In android studio, open Firebase assistant and select "authentication using a custom authentication system".
5. Then select the "Add the Firebase Authentication SDK to your app". Click on "accept changes" in the dialog box.


Uploading Profile images and other images.

(Ignore step 1,2,3 if already done with the SOS app)
1. Go to firebase console and under "storage" click on "get started".
2. Click "next" and choose a location for the cloud server.
3. Now you will be able to store images on the Firebase cloud service.
4. Go to "profile" section in the app and try uploading profile pic.

Adding Google Maps API KEY

1. Go To "/app/src/debug/res/values/google_maps_api.xml" and replace "PASTE YOUR GOOGLE MAPS API KEY HERE" with you API Key.
2. Go To "/app/src/release/res/values/google_maps_api.xml" and replace "PASTE YOUR GOOGLE MAPS API KEY HERE" with you API Key.
