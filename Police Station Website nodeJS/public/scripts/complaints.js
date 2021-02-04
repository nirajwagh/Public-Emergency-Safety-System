var uid, rootRef, stationRef, police_station, cop_name;
const card=document.getElementById('box');
const admin_name=document.getElementById('admin_name');
const station_name=document.getElementById('station_name');

const database = firebase.database();
var inner;
  
auth.onAuthStateChanged(user => {
    if(!user){
        console.log('Logged OUTTTT'); 
        window.location='/'
    }else{
      uid=user.uid;
      rootRef = database.ref('actors/admins/'+uid);
      rootRef.once('value', snapshot => {
        police_station=snapshot.val()["police_station"];
        cop_name=snapshot.val()["name"];
        
        admin_name.innerHTML=`Hello, ${cop_name}`;
        station_name.innerHTML=`${police_station}`;

        stationRef = database.ref('police_stations/'+police_station+'/assigned_complaints');
        
        stationRef.on('child_removed', snapshot => {
          location.reload();
      })

        stationRef.on('child_added', snapshot => {
          let complaint_id=snapshot.child('complaint_id').val();
      
          tempInner=`
              <div class='wrapper'>
              <div class="comp_id">
              <span>Complaint id: ${complaint_id}</span>
              </div>
              </div>
              `;

          inner+=tempInner;
          card.innerHTML=inner;  
      })

      }); 
      
    }
  });
  
  const logout=document.querySelector('#logout');
  logout.addEventListener('click', (e) => {
      e.preventDefault();
      auth.signOut(); 
  }) 

  



  

