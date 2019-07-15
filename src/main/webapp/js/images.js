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
      const a = '<ul class=\"cd-slider\">';
      const b = '<li class=\'current\'><img src=\'img/1.jpg\' alt=\'project image\'></li><li><img src=\'img/1.jpg\' alt=\'project image\'></li>';
      const c = '</ul><div class=\'cd-slider-navigation\'><ul><li><a href=\'#\' class=\'prev inactive\'><i class=\'fas fa-chevron-left fa-3x\' style=\'color:white\'></i></a></li><li><a href=\'#\' class=\'next\'><i class=\'fas fa-chevron-right fa-3x\' style=\'color:white\'></i></a></li></ul></div>';
      const html = a + b + c;
      imageContainer.innerHTML = html;
    });
  });
}


function runCode() {
  // const imageContainer = document.getElementById('image-slider');
  // const a = '<ul class=\"cd-slider\">';
  // const b = '<li class=\'current\'><img src=\'img/1.jpg\' alt=\'project image\'></li><li><img src=\'img/1.jpg\' alt=\'project image\'></li>';
  // const c = '</ul><div class=\'cd-slider-navigation\'><ul><li><a href=\'#\' class=\'prev inactive\'><i class=\'fas fa-chevron-left fa-3x\' style=\'color:white\'></i></a></li><li><a href=\'#\' class=\'next\'><i class=\'fas fa-chevron-right fa-3x\' style=\'color:white\'></i></a></li></ul></div>';
  // const html = a + b + c;
  // imageContainer.innerHTML = html;

  fetchRestaurantImages();
  console.log("done");
}

runCode();