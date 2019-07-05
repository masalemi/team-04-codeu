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

package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.apache.commons.validator.routines.UrlValidator;
import java.util.regex.*;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.util.Map;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/** Handles fetching and saving {@link Message} instances. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Message} data for a specific user. Responds with
   * an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    String user = request.getParameter("user");
    String restaurantId = request.getParameter("restaurantId");

    if ((user == null || user.equals("")) && (restaurantId == null || restaurantId.equals(""))) {
      // Request is invalid, return empty array
      response.getWriter().println("[]");
      return;
    }

    List<Message> messages;
    if ((user == null || user.equals(""))) {
      messages = datastore.getMessagesForRestaurant(UUID.fromString(restaurantId));
    }
    else {
      messages = datastore.getMessagesForUser(user);
    }

    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    // Redirect to index.html if not logged in
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() && request.getParameter("restaurantId") == null) {
      response.sendRedirect("/index.html");
      return;
    }

    // Get user, message text and image url
    ArrayList<String> labels = new ArrayList<String>();
    String user = userService.getCurrentUser().getEmail();
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.basicWithImages());
    String uploadedFileUrl = getUploadedFileUrl(request, "image");
    BlobKey blobKey = getBlobKey(request, "image");
    // Get image labels
    if (uploadedFileUrl != null) {
      userText += " <img src=\"" + uploadedFileUrl.replace("<i>", "_").replace("</i>", "_") + "\" />";
      byte[] blobBytes = getBlobBytes(blobKey);
      List<EntityAnnotation> imageLabels = getImageLabels(blobBytes);
      for(EntityAnnotation label : imageLabels){
        labels.add(label.getDescription());
      }
    }
    String text = new String();
    String regex = "(https?://\\S+\\.(png|jpg|gif))";
    String replacement = "<img src=\"$1\" />";
    String textWithImagesReplaced = userText.replaceAll(regex, replacement);

    regex = "(https?://www.youtube.com/\\S+)";
    replacement = "<iframe src=\"$1\" width=\"560\" height=\"315\"></iframe>";
    String textWithMediaReplaced = textWithImagesReplaced.replaceAll(regex, replacement);

    Pattern pattern = Pattern.compile("src=(.*?)/>");
    Matcher m = pattern.matcher(textWithMediaReplaced);
    UrlValidator validator = new UrlValidator();
    while (m.find()) {
        String url = m.group(1);
        url = url.substring(1, url.length() - 2);   //Strip off extra quotations
        if (!validator.isValid(url)) {
          System.out.println("URL " + url + " is not vaild.");
        }
    }

    // Message text sentiment analysis
    float sentimentScore = (float) 0.0;
    if (userText == "") sentimentScore = (float) 2.0;
    else{
      Document doc = Document.newBuilder()
        .setContent(userText).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      sentimentScore = sentiment.getScore();
      languageService.close();
    }

    if (labels.size() >= 3) {
      textWithMediaReplaced += "Top 3 Guesses: " + labels.get(0) + ", " + labels.get(1) + ", " + labels.get(2);
    }

    if (sentimentScore > -1.0 && sentimentScore < 1.0){
      if(sentimentScore > 0) textWithMediaReplaced += "<br/>Attitude Guess: Positive";
      else if (sentimentScore < 0) textWithMediaReplaced += "<br/>Attitude Guess: Negative";
      else textWithMediaReplaced += "<br/>Attitude Guess: Neutral";
    }

    text = makeMarkdown(textWithMediaReplaced);

    String restaurantId = request.getParameter("restaurantId");

    Message message = null;

    if (restaurantId == null) {
      message = new Message(user, text, labels, sentimentScore, null);
    }
    else {
      message = new Message(user, text, labels, sentimentScore, UUID.fromString(restaurantId));
    }
    // Store message in datastore
    datastore.storeMessage(message);
    if (restaurantId != null) {
      response.sendRedirect("/restaurant-page.html?restaurantId=" + restaurantId);
    }
    else {
      response.sendRedirect("/user-page.html?user=" + user);
    }
  }


  public static String makeMarkdown(String text) {
    int squiggleCount = 0;
    int dashCount = 0;
    int starCount = 0;
    //List<Character> list = new ArrayList<Character>();
    StringBuilder string = new StringBuilder();

    for (char c : text.toCharArray()) {
      if (c == '~') {
        squiggleCount++;
      } else if (c == '_') {
        dashCount++;
      } else if (c == '*') {
        starCount++;
      }
    }
    int squiggleNum = 0;
    int dashNum = 0;
    int starNum = 0;
    for (char c : text.toCharArray()) {

        if (c == '~') {
          squiggleNum++;
          if (squiggleNum % 2 != 0 && squiggleNum != squiggleCount) {
            string.append("<s>");

          } else if (squiggleNum % 2 == 0){
            string.append("</s>");

          }else {
            string.append('~');
          }
        } else if (c == '_') {
          dashNum++;
          if (dashNum % 2 != 0 && dashNum != dashCount) {
            string.append("<i>");

          } else if (dashNum % 2 == 0){
            string.append("</i>");

          } else {
            string.append('_');
          }
        } else if (c == '*') {
          starNum++;
          if (starNum % 2 != 0 && starNum != starCount) {
            string.append("<b>");

          } else if (starNum % 2 == 0) {
            string.append("</b>");
            //starNum++;
          } else {
            string.append('*');
          }
        }  else {
          string.append(c);
        }
    }
    return string.toString();
  }

  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName){
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (devserver)
    if(blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    return imagesService.getServingUrl(options);
  }

  private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName){
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a BlobKey. (devserver)
    if(blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so the BlobKey is empty. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey;
  }

  private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] b = blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(b);

      // if we read fewer bytes than we requested, then we reached the end
      if (b.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }

  /**
   * Uses the Google Cloud Vision API to generate a list of labels that apply to the image
   * represented by the binary data stored in imgBytes.
   */
  private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
    ByteString byteString = ByteString.copyFrom(imgBytes);
    Image image = Image.newBuilder().setContent(byteString).build();

    Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    requests.add(request);

    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
    client.close();
    List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
    AnnotateImageResponse imageResponse = imageResponses.get(0);

    if (imageResponse.hasError()) {
      System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
      return null;
    }

    return imageResponse.getLabelAnnotationsList();
  }

}
