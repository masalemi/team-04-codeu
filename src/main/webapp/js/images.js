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
      const messageContainer = document.getElementById('message-container');
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
      all_images.forEach((url) => {
        list_html += '<li class=\'slides-in\'><img src=\'' + url + '\' alt=\'project image\'></li>'
      });
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