var uid, rootRef, police_station, cop_name;
const admin_name=document.getElementById('admin_name');
const station_name=document.getElementById('station_name');
const database = firebase.database();


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

      }); 
  }
});

const logout=document.querySelector('#logout');
logout.addEventListener('click', (e) => {
  console.log('LOG OUT CLICKED');
    e.preventDefault();
    auth.signOut(); 
})  

    function initMap() {
      var pune = { lat: 18.521522, lng: 73.855349 };
      var map = new google.maps.Map(
        document.getElementById('map'),
        {

          zoom: 12,
          center: pune,
          zoomControl: true,
          scaleControl: true,
          zoomControlOptions: {
            position: google.maps.ControlPosition.RIGHT_CENTER
          },

          styles: [
            { elementType: 'geometry', stylers: [{ color: '#242f3e' }] },
            { elementType: 'labels.text.stroke', stylers: [{ color: '#242f3e' }] },
            { elementType: 'labels.text.fill', stylers: [{ color: '#746855' }] },
            {
              featureType: 'administrative.locality',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#d59563' }]
            },
            {
              featureType: 'poi',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#d59563' }]
            },
            {
              featureType: 'poi.park',
              elementType: 'geometry',
              stylers: [{ color: '#263c3f' }]
            },
            {
              featureType: 'poi.park',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#6b9a76' }]
            },
            {
              featureType: 'road',
              elementType: 'geometry',
              stylers: [{ color: '#38414e' }]
            },
            {
              featureType: 'road',
              elementType: 'geometry.stroke',
              stylers: [{ color: '#212a37' }]
            },
            {
              featureType: 'road',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#9ca5b3' }]
            },
            {
              featureType: 'road.highway',
              elementType: 'geometry',
              stylers: [{ color: '#746855' }]
            },
            {
              featureType: 'road.highway',
              elementType: 'geometry.stroke',
              stylers: [{ color: '#1f2835' }]
            },
            {
              featureType: 'road.highway',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#f3d19c' }]
            },
            {
              featureType: 'transit',
              elementType: 'geometry',
              stylers: [{ color: '#2f3948' }]
            },
            {
              featureType: 'transit.station',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#d59563' }]
            },
            {
              featureType: 'water',
              elementType: 'geometry',
              stylers: [{ color: '#17263c' }]
            },
            {
              featureType: 'water',
              elementType: 'labels.text.fill',
              stylers: [{ color: '#515c6d' }]
            },
            {
              featureType: 'water',
              elementType: 'labels.text.stroke',
              stylers: [{ color: '#17263c' }]
            }
          ]
        });

        var locData = [];

        i = 0;


        //***********************STEP 3: Firebase realtime db url******************
        
        fetch(***PASTE YOUR URL HERE***/completed_complaints.json')
        .then(function (response) {
          console.log(response + 'abc');
          return response.json();
        })
        .then(data => {

          for (key in data) {

        
            locData[i] = new google.maps.LatLng(data[key]['complaint_create_loc_lat'], data[key]['complaint_create_loc_lng']);

            i++;
          }

          var heatmap = new google.maps.visualization.HeatmapLayer({
            data: locData,
            radius:12
          });
          heatmap.setMap(map);

        })

      
    }

  