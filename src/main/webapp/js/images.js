function fetchRestaurantImages(){
  const url = '/restaurant?restaurantId=' + parameterRestaurant;
  fetch(url).then((response) => {
    return response.text();
  }).then((restaurant) => {
  	let restaurant_data = JSON.parse(restaurant);
  	let images = restaurant_data.images;
    const url = '/messages?restaurantId=' + parameterRestaurant;
    fetch(url).then((response) => {
      return response.json();
    }).then((messages) => {
      let message_image = [];
      messages.forEach((message) => {
        if (message.imageUrl != null) {
          message_image.push(message.imageUrl);
        }
      });
      const imageContainer = document.getElementById('image-slider');
      const start = '<ul class=\"cd-slider\">';
      let list_html = '';
      let all_images = images.concat(message_image);
      for (var i = 0; i < all_images.length; i++) {
        let url = all_images[i];
        if (i == 0) {
          // list_html += '<li class=\"current\"><img src=\'' + url + '\' alt=\'project image\'></li>'
          list_html += '<li class=\'slides-in\'><img src=\'' + url + '\' alt=\'project image\'></li>'
        }
        else {
          // list_html += '<li><img src=\'' + url + '\' alt=\'project image\'></li>'
          list_html += '<li class=\'slides-in\'><img src=\'' + url + '\' alt=\'project image\'></li>'
        }
      }
      const end = '</ul><div class=\'cd-slider-navigation\'><ul><li><a href=\'#\' class=\'prev inactive\'><i class=\'fas fa-chevron-left fa-3x\' style=\'color:white\'></i></a></li><li><a href=\'#\' class=\'next\'><i class=\'fas fa-chevron-right fa-3x\' style=\'color:white\'></i></a></li></ul></div>';
      const html = start + list_html + end;
      imageContainer.innerHTML = html;
    });
  });
}


function runCode() {
  fetchRestaurantImages();
}

runCode();