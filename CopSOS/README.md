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
  
  
  # Instructions
  
  ERROR #1

		A problem occurred evaluating project ':app'.
		> C:\...\apikey.properties (The system cannot find the file specified)


 create file named "apikey.properties" in the root directory(Cop_SOS) and following line in the file:

 	GOOGLE_MAPS_API_KEY = "paste-your-google-maps-api-key-here"

 Here replace paste-your-google-maps-api-key-here with your google maps api key. Save the file  	


ERROR #2

		Execution failed for task ':app:processDebugGoogleServices'.
		> File google-services.json is missing. The Google Services Plugin cannot function without it. 
 	

 1. Create a google firebase account.
 2. Login the Android studio using the same google account used for Firebase in step 1.
 3. In android studio, go to Tools -> Firebase. Now Firebase assistant will open.
 4. Go to Authentication and click on "connect to Firebase"
 5. This will redirect you to your browser and will open the Firebase console.
 6. Create a new project and connect your android project to this Firebase console project.


Enable Firebase Realtime Database.

1. In android studio, open Firebase assistant and under "Realtime Database", select "Get started with realtime database".
1. Go to your Firebase Console and under Realtime Database, select "create Database".
2. Choose Database location and under "Security Rules" select "start in test mode".
. 

Register and Login users using Firebase Authentiaction

1. In android studio, open Firebase assistant and select "authentication using a custom authentication system".
2. Then select the "Add the Firebase Authentication SDK to your app"
1. Go to your Firebase console and choose your project.
2. Select Authentication and enable it.
3. Under Autentication -> Sign-in method, enable the "Email/Password" and "Phone" method.

