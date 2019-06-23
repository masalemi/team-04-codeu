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

    if (user == null || user.equals("")) {
      // Request is invalid, return empty array
      response.getWriter().println("[]");
      return;
    }

    List<Message> messages = datastore.getMessagesForUser(user);
    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.basicWithImages());
    String uploadedFileUrl = getUploadedFileUrl(request, "image").replace("<i>", "_").replace("</i>", "_");
    if (getUploadedFileUrl(request, "image") != null) {
      userText += " <img src=\"" + uploadedFileUrl + "\" />";
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

    ArrayList<String> labels = ArrayList<String>();

    labels = 

    if (labels.size() >= 3) {
      textWithMediaReplaced += "Top 3 Guesses:\n" + labels[0] + "\n" + labels[1] + "\n" + labels[2]; 
    }

    text = makeMarkdown(textWithMediaReplaced);

    Message message = new Message(user, text, labels);
    datastore.storeMessage(message);

    response.sendRedirect("/user-page.html?user=" + user);
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

}
