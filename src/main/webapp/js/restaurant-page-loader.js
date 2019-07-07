/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Get ?restaurant=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);

const parameterRestaurant = urlParams.get('restaurantId');

// URL must include ?restaurantId=XYZ parameter. If not, redirect to homepage.
if (!parameterRestaurant) {
  window.location.replace('/');
}

/** Sets the page title based on the URL parameter username. */
function setPageTitle() {
  document.title = parameterRestaurant + ' - Restaurant Page';
}

/** Fetches restaurant description and add to page.*/
function fetchRestaurantInfo(){
  const url = '/restaurant?restaurantId=' + parameterRestaurant;
  fetch(url).then((response) => {
    return response.text();
  }).then((restaurant) => {
  	let restaurant_data = JSON.parse(restaurant);
  	let description = restaurant_data.description;
    const descriptionContainer = document.getElementById('description-container');
    if(description === undefined || description == ''){
      description = 'This restaurant does not have a description yet.';
    }
    descriptionContainer.innerHTML = description;

    let name = restaurant_data.name;
    const nameContainer = document.getElementById('restaurantName');
    if(name === undefined || name == ''){
      name = 'This restaurant does not have a name yet.';
    }
    nameContainer.innerHTML = name;

  });
}


// /** Fetches messages and add them to the page. */
// function fetchReviews() {
//   const url = '/messages?restaurantId=' + parameterRestaurant;
//   fetch(url)
//       .then((response) => {
//         return response.json();
//       })
//       .then((messages) => {
//         const messagesContainer = document.getElementById('message-container');
//         console.log(messages.length);
//         if (messages.length == 0) {
//           messagesContainer.innerHTML = '<p>This restaurant has no reviews yet.</p>';
//         } else {
//           messagesContainer.innerHTML = '';
//         }
//         messages.forEach((message) => {
//           const messageDiv = buildReviewDiv(message);
//           messagesContainer.appendChild(messageDiv);
//         });
//       });
// }

  // Fetch messages and add them to the page.
  function fetchReviews(){
    const url = '/messages?restaurantId=' + parameterRestaurant;
    fetch(url).then((response) => {
      return response.json();
    }).then((messages) => {
      const messageContainer = document.getElementById('message-container');
      //console.log(messages.length);
      if(messages.length == 0){
       messageContainer.innerHTML = '<p>This restaurant has no reviews yet.</p>';
      }
      else{
       messageContainer.innerHTML = '';  
      }
      var messageCount = 0;
      var sentimentSum = 0;
      messages.forEach((message) => { 
       messageCount++;
       sentimentSum += message.sentimentScore;
       const messageDiv = buildReviewDiv(message);
       messageContainer.appendChild(messageDiv);
      });
      setScore(sentimentSum, messageCount);
    });
  }

  // Sets reviews to a given Number
  function setScore(score, count) {
    var average = score / count;
    if (Number.isNaN(average)) {
      document.getElementById("reviewScore").textContent = "No scores yet";
    } else {
    average = average * 5;
    average = average + 5;
    document.getElementById("reviewScore").textContent = "Average Score: " + average + "/10";
    }
  }

  // This is an alternative way to format a message, here for reference

  // function buildReviewDiv(message){
  //  const usernameDiv = document.createElement('div');
  //  usernameDiv.classList.add("left-align");
  //  usernameDiv.appendChild(document.createTextNode(message.user));
   
  //  const timeDiv = document.createElement('div');
  //  timeDiv.classList.add('right-align');
  //  timeDiv.appendChild(document.createTextNode(new Date(message.timestamp)));
   
  //  const headerDiv = document.createElement('div');
  //  headerDiv.classList.add('message-header');
  //  headerDiv.appendChild(usernameDiv);
  //  headerDiv.appendChild(timeDiv);
   
  //  const bodyDiv = document.createElement('div');
  //  bodyDiv.classList.add('message-body');
  //  bodyDiv.appendChild(document.createTextNode(message.text));
   
  //  const messageDiv = document.createElement('div');
  //  messageDiv.classList.add("message-div");
  //  messageDiv.appendChild(headerDiv);
  //  messageDiv.appendChild(bodyDiv);
   
  //  return messageDiv;
  // }

/**
 * Builds an element that displays the message.
 * @param {Message} message
 * @return {Element}
 */
function buildReviewDiv(message) {
  const headerDiv = document.createElement('div');
  headerDiv.classList.add('message-header');
  headerDiv.appendChild(document.createTextNode(
      message.user + ' - ' + new Date(message.timestamp)));

  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('message-body');
  bodyDiv.innerHTML = message.text;

  const messageDiv = document.createElement('div');
  messageDiv.classList.add('message-div');
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(bodyDiv);

  return messageDiv;
}

function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url?restaurantId=' + parameterRestaurant)
    .then((response) => {
      return response.text();
    })
    .then((imageUploadUrl) => {
      const messageForm = document.getElementById('review-form');
      messageForm.action = imageUploadUrl;
      messageForm.classList.remove('hidden');
    });
}

/** Fetches data and populates the UI of the page. */
function buildRestaurant() {
  const config = {removePlugins: [ 'ImageUpload' ]};
  ClassicEditor.create(document.getElementById('message-input'), config);
  setPageTitle();
  fetchRestaurantInfo();
  fetchReviews();
  fetchBlobstoreUrlAndShowForm();
}
