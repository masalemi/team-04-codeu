function fetchRestaurantImages(){
  const url = '/restaurant?restaurantId=' + parameterRestaurant;
  fetch(url).then((response) => {
    return response.text();
  }).then((restaurant) => {
  	let restaurant_data = JSON.parse(restaurant);
  	let images = restaurant_data.images;
  	console.log(1);
  	console.log(images);
    return images;
  });
}

function fetchReviewImages(){
  const url = '/messages?restaurantId=' + parameterRestaurant;
  fetch(url).then((response) => {
    return response.json();
  }).then((messages) => {
    const messageContainer = document.getElementById('message-container');
    let message_image = [];
    messages.forEach((message) => {
    	if (image.imageUrl != null) {
    		message_image.push(message.imageUrl);
    	}
    });
    console.log(2);
    console.log(message_image);
    return message_image;
  });
}


console.log("testing timing");
const imageContainer = document.getElementById('image-slider');
const a = '<ul class=\"cd-slider\"><li class=\'current\'>';
const restaurant_images = fetchRestaurantImages();
const review_images = fetchReviewImages();
console.log(3);
console.log(restaurant_images);
console.log(review_images);
const all_images = restaurant_images.concat(review_images);
console.log(all_images);
const b = '<img src=\'img/1.jpg\' alt=\'project image\'></li><li><img src=\'img/1.jpg\' alt=\'project image\'></li>';


const c = '</ul><div class=\'cd-slider-navigation\'><ul><li><a href=\'#\' class=\'prev inactive\'><i class=\'fas fa-chevron-left fa-3x\' style=\'color:white\'></i></a></li><li><a href=\'#\' class=\'next\'><i class=\'fas fa-chevron-right fa-3x\' style=\'color:white\'></i></a></li></ul></div>';
const html = a + b + c;
imageContainer.innerHTML = html;
console.log("done");