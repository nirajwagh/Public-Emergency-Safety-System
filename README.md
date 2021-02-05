# An SOS system for calling nearby cops using the android app.
[Detailed Medium Article on the Project](https://google.com) <br>

[Short Demo Video](https://youtu.be/b6CsQJGP6uM) <br>

Detailed Project video to be uploaded soon...<br>
## Introduction
A citizen can call a nearby cop by pressing the SOS button and the current location of the citizen will be used to search the nearest cop in the database.
Live location of all on-duty cops will be continuously updated in the Firebase database.<br><br>
After the cop is assigned to the citizen, the cop can view the live location of the citizen along with his/her name and phone number. The cop can also use the GPS Navigation button to get directions to the citizen's location with the help of the google maps app.

## Project Architecture and Firebase Database
<br>
<table>

 <tr>
 
  <td>
 <b>Android apps architecture </b> 
    <img src="/app-arch.jpg" align="top">
    
    
  </td>
 </tr>
                                       
  <tr>
 
  <td>
 <b> Admin Website architecture   </b> 
    <img src="/website-arch.jpg" align="top">
                                  
  </td>
 </tr>
                                       
  <tr>
 
  <td>
 <b>Firebase Database data   </b>   
    <img src="/db-ss.jpg" align="top">
                                 
  </td>
 </tr>

</table>

## Main Features

1. After pressing the SOS button, the citizen app will search the nearest cop with respect to the citizen's location.
2. If an on-duty and an unassigned cop is found in a 10km radius, then he/she will be assigned to the citizen's request.
3. Now, both can track each other using the maps and live location through their apps.
4. The citizen and the cop can make a phone call to each other by pressing the call button.
5. The cop will be shown the estimated distance and directions to reach the citizen.
6. The citizen will be shown the estimated time and direction to be taken by to cop to reach the location.
7. The cop can press the GPS navigation button to get navigation using google maps.
8. When the cop reaches in 100mtr radius of the citizen, the cop app will prompt a message to check if the cop has found the citizen or not.
9. when the citizen is found, the cop can end the complaint and the complaint gets completed.

## If the cop is not addressing the complaint.

1. If the cop is not responding to the complaint and is he/she is not approaching the requested location, then the citizen can report the cop using the SOS citizen app.
2. After reporting the cop, the SOS citizen app will automatically search the nearest police station to the citizen and will forward the complaint to the admin of that police station.
3. The admin will get an alert on the admin website and will receive all details about the complaint.
4. The admin will then assign a cop manually using the COP app's manual complaint feature and then the complaint will be addressed as usual.
5. The cop who was reported, has to mention the reason for not responding to the complaint in the cop app.

## If the cop is unable to reach the citizen's location.

1. If due to some circumstances the cop is not able to reach the citizen's location, then the cop can forward the complaint to the nearest police station to the citizen.
2. The police station admin will address the complaint as mentioned above.
3. The cop who forwarded the complaint has to mention the reason for forwarding the complaint and also need to upload a live pic as proof in the cop app.

## Heat Map

1. The admin website has an option for viewing a heat map containing all complaints of the city.
2. This heat map will help the police administration for getting an idea about which areas have more complaints.
3. Using this map, the authorities can make changes to the police patrolling system.

## Technologies, APIs, and Libraries used

1. Java for Andriod applications development.
2. [Google Firebase](https://firebase.google.com/) for Authentication, Real-time database, and storage.
3. Google Firebase Real-time database REST API for fetching database data.
4. [Google maps API](https://developers.google.com/maps/documentation) for Live location and Routing using Directions API.
5. [Lottie Animations](https://lottiefiles.com/) library for Animations.
6. [Glide Library](https://github.com/bumptech/glide) for fetching images from URL.
7. Admin website Server: NodeJS server.

## LICENSE

    MIT License

    Copyright (c) 2021 Niraj Wagh

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
