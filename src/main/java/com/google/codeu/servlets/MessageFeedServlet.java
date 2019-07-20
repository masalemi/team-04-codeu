package com.google.codeu.servlets;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;

import java.util.UUID;

/**
 * Handles fetching all messages for the public feed.
 */
@WebServlet("/feed")
public class MessageFeedServlet extends HttpServlet{
  
 private Datastore datastore;

 @Override
 public void init() {
  datastore = new Datastore();
 }
 
 /**
  * Responds with a JSON representation of Message data for all users.
  */
 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response)
   throws IOException {

  response.setContentType("application/json");
  
  List<Message> messages = new ArrayList<>();
  
  String restaurantId = request.getParameter("restaurantId");
  String feed_type = request.getParameter("feedType");
  
  if (restaurantId == null || restaurantId.equals("null")) {
    if (feed_type == null || feed_type.equals("null")) {
      messages = datastore.getAllMessages();
    }
    else {
      if (feed_type.equals("posts")) {
        messages = datastore.getAllPosts();
      }
      else if (feed_type.equals("reviews")) {
        messages = datastore.getAllReviews();
      }
      else {
        System.out.println("Feed type not found!");
      }
    }
  } else {
    messages = datastore.getMessagesForRestaurant(UUID.fromString(restaurantId));
  }
  
  Gson gson = new Gson();
  String json = gson.toJson(messages);
  
  response.getOutputStream().println(json);
 }
}