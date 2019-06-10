function createMap(){
  const map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.422, lng: -122.084},
    zoom: 16,
  });

  const trexMarker = new google.maps.Marker({
    position: {lat: 37.421903, lng: -122.084674},
    map: map,
    title: 'Stan the T-Rex'
  });

  var trexInfoWindow = new google.maps.InfoWindow({
    content: 'This is Stan, the T-Rex statue.'
  });

  trexMarker.addListener('click', function() {
    trexInfoWindow.open(map, trexMarker);
  });
}
