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
const parameterLat = urlParams.get('lat');
const parameterLng = urlParams.get('lng');

// URL must include ?restaurantId=XYZ parameter. If not, redirect to homepage.
if (!parameterRestaurant) {
  //window.location.replace('/');
}

/** Sets the page title based on the URL parameter username. */
function setPageTitle() {
  document.title = 'Add Restaurant Page For Restaurant ' + parameterRestaurant;
}

function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url-r?restaurantId=' + parameterRestaurant)
    .then((response) => {
      return response.text();
    })
    .then((imageUploadUrl) => {
      const messageForm = document.getElementById('restaurant-form');
      messageForm.action = imageUploadUrl;
      messageForm.classList.remove('hidden');
    });
}

/** Fetches data and populates the UI of the page. */
function addRestaurantUI() {
  const config = {removePlugins: [ 'ImageUpload' ]};
  ClassicEditor.create(document.getElementById('restaurant-name'), config );
  ClassicEditor.create(document.getElementById('restaurant-description'), config );

  if (parameterLat){
    document.getElementById("latId").setAttribute("value", parameterLat)
  }
  
  if (parameterLng){
    document.getElementById("lngId").setAttribute("value", parameterLng)
  }
  setPageTitle();
  fetchBlobstoreUrlAndShowForm();
}
