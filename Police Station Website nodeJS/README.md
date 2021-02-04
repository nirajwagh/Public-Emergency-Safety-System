# Screenshots

<table>

  <tr>
    <td>
      <img src="/Police%20Station%20Website%20nodeJS/Screenshots/loginn.png" align="top">
      Log In Page
    </td>
  </tr>

  <tr>
    <td>
      <img src="/Police%20Station%20Website%20nodeJS/Screenshots/complaints.png" align="top">
      Complaints for Police Station
    </td>
  </tr>
  
  <tr>
    <td>
      <img src="/Police%20Station%20Website%20nodeJS/Screenshots/heat%20map.png" align="top">
      Heat Map
    </td>
  </tr>
  
  <tr>
    <td>
      <img src="/Police%20Station%20Website%20nodeJS/Screenshots/server.png" align="top">
      Starting Server
    </td>
  </tr>
</table>


Police station website (nodeJS)

STEP 1: Adding Firebase web SDK to your website.

1. In Firebase console, select you project and click on "add app" option below your project name.
2. Now select the "web ( </> )" option.
3. Give a nickname to the app and click on "register app".
4. now copy the second script and paste it under "step 1" of "complaint.ejs", "map.ejs" and "login.ejs" file.

STEP 2: Adding google maps API key.

1. Paste your google maps api key in "step 2" of "map.ejs".

Step 3: Adding Firebase db Url.
1. Go to Realtime database section in Firebase Console.
2. Now, above your data, there is a link for your database, starting with "https://...firebaseio.com".
3. Copy this link and paste it in STEP 3 of "map.js" file. (its map.js and not map.ejs);



Step 4: Adding Admin users to Firebase console.
1. Go to Authentication in firebase console.
2. Click on "add user" and add a admin user as "email: admin1@abc.com" & "password: asasas".
3. details about this admin is already added in realtime db, and this admin belongs to "Alandi Police station".
4. If you want to add new user of some other police station, then follow the previous step and in raltime db add details of the admin. Refer to other admins data.

Step 5: Running the web app.
1. Install VS Code, nodejs and nodeom.
2. Open the website project folder in VS Code.
3. Open terminal in VS Code and execute command "nodemon".
4. It will start the server.
5. After the server is started, open your browser ad goto "localhost:3000".
6. In the admin login page, enter credentials as "email: admin1@abc.com" & "password: asasas";
7. If you want to add more admin users then go to firebase authentication in console and add the user. Also add details in realtime db as per the field mentioned in the "realtimeDbData.json" file.
