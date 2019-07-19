let map;
/* Editable marker that displays when a user clicks in the map. */
let editMarker;

function createMap(){
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 38.5949, lng: -94.8923},
    zoom: 4
  });

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function(position) {
      var pos = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      };

      map.setCenter(pos);
      map.setZoom(10);
    }, function() {
      handleLocationError(true, infoWindow, map.getCenter());
    });
  } else {
    // Browser doesn't support Geolocation
    handleLocationError(false, infoWindow, map.getCenter());
  }

  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });
  fetchMarkers();
}

/** Fetches markers from the backend and adds them to the map. */
function fetchMarkers(){
  fetch('/markers').then((response) => {
    return response.json();
  }).then((markers) => {
    markers.forEach((marker) => {
     createMarkerForDisplay(marker.restaurantId, marker.lat, marker.lng, marker.content)
    });

  });
}

/** Creates a marker that shows a read-only info window when clicked. */
function createMarkerForDisplay(restaurantId, lat, lng, content){
  console.log("creating marker");
  const marker = new google.maps.Marker({
    restaurantId: restaurantId,
    position: {lat: lat, lng: lng},
    map: map
  });

  const link = "/restaurant-page.html?restaurantId="+restaurantId;
  var infoWindow = new google.maps.InfoWindow({
    content: createLink(link, content)
  });
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/** Creates a marker that shows a textbox the user can edit. */
function createMarkerForEdit(lat, lng){
  // If we're already showing an editable marker, then remove it.
  if(editMarker){
    editMarker.setMap(null);
  }
  editMarker = new google.maps.Marker({
    position: {lat: lat, lng: lng},
    map: map
  });
  const infoWindow = new google.maps.InfoWindow({
    content: buildInfoWindowInput(lat, lng)
  });
  // When the user closes the editable info window, remove the marker.
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });
  infoWindow.open(map, editMarker);
}

/** Builds and returns HTML elements that show an editable textbox and a submit button. */
function buildInfoWindowInput(lat, lng){
  //const textBox = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Create Restaurant Here'));
  button.onclick = () => {
    postMarker(lat, lng);
    createMarkerForDisplay(lat, lng);
    editMarker.setMap(null);
  };
  const containerDiv = document.createElement('div');
  //containerDiv.appendChild(textBox);
  //containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(button);
  return containerDiv;
}

/** Sends a marker to the backend for saving. */
function postMarker(lat, lng){
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);

  window.location.replace('/add-restaurant.html?lat=' + lat+"&lng="+lng);
}

function handleLocationError(browserHasGeolocation, infoWindow, pos) {
  infoWindow.setPosition(pos);
  infoWindow.setContent(browserHasGeolocation ?
                        'Error: The Geolocation service failed.' :
                        'Error: Your browser doesn\'t support geolocation.');
  infoWindow.open(map);
}

function createLink(url, text) {
  const linkElement = document.createElement('a');
  linkElement.appendChild(document.createTextNode(text));
  linkElement.href = url;
  return linkElement;
}
