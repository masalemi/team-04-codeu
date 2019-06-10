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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.apache.commons.validator.routines.UrlValidator;

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
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.none());

    String regex = "(https?://\\S+\\.(png|jpg|gif))";
	String replacement = "<img src=\"$1\" />";
	String textWithImagesReplaced = userText.replaceAll(regex, replacement);

	int i = 0;
	String[] schemes = {"http","https"};
	UrlValidator urlValidator = new UrlValidator(schemes);
	while (true) {
		i = textWithImagesReplaced.indexOf("<img src=", i);
		if (i == -1) {
			break;
		}
		int end_index = textWithImagesReplaced.indexOf("/>", i);
		String url = textWithImagesReplaced.substring(i + 10, end_index - 2);
		if (!urlValidator.isValid(url)) {
			System.out.println("URL is not valid!");
		}
		i += 1;
	}

	regex = "(https?://www.youtube.com/\\S+)";
	replacement = "<iframe src=\"$1\"></iframe>";
	String textWithMediaReplaced = textWithImagesReplaced.replaceAll(regex, replacement);

    Message message = new Message(user, textWithMediaReplaced);
    datastore.storeMessage(message);

    response.sendRedirect("/user-page.html?user=" + user);
  }
}