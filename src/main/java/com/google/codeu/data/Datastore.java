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

package com.google.codeu.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.HashSet;
import java.util.HashMap;

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("timestamp", message.getTimestamp());
    messageEntity.setProperty("restaurantId", message.getRestaurant().toString());

    datastore.put(messageEntity);
  }

  /**
   * Gets messages posted by a specific user.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessages(String user) {
    List<Message> messages = new ArrayList<>();

    Query query =
        new Query("Message")
            .setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
            .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");

        Message message = new Message(id, user, text, timestamp);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }
    return messages;
  }

  /** Stores the User in Datastore. */
  public void storeUser(User user) {
    Entity userEntity = new Entity("User", user.getEmail());
    userEntity.setProperty("email", user.getEmail());
    userEntity.setProperty("aboutMe", user.getAboutMe());
    datastore.put(userEntity);
  }

 /**
  * Returns the User owned by the email address, or
  * null if no matching User was found.
  */
  public User getUser(String email) {
    Query query = new Query("User")
      .setFilter(new Query.FilterPredicate("email", FilterOperator.EQUAL, email));
    PreparedQuery results = datastore.prepare(query);
    Entity userEntity = results.asSingleEntity();
    if (userEntity == null) {
      return null;
    }
    String aboutMe = (String) userEntity.getProperty("aboutMe");
    User user = new User(email, aboutMe);

    return user;
  }

  public void storeRestaurant(Restaurant restaurant) {
    Entity restaurantEntity = new Entity("Restaurant", restaurant.getId().toString());
    restaurantEntity.setProperty("id", restaurant.getId().toString());
    restaurantEntity.setProperty("name", restaurant.getName());
    restaurantEntity.setProperty("description", restaurant.getDescription());
    restaurantEntity.setProperty("images", restaurant.getImages());

    datastore.put(restaurantEntity);
  }

  public Restaurant getRestaurant(UUID id) {
    Query query = new Query("Restaurant")
      .setFilter(new Query.FilterPredicate("id", FilterOperator.EQUAL, id.toString()));
    PreparedQuery results = datastore.prepare(query);
    Entity restaurantEntity = results.asSingleEntity();
    if (restaurantEntity == null) {
      return null;
    }

    String name = (String) restaurantEntity.getProperty("name");
    String description = (String) restaurantEntity.getProperty("description");
    ArrayList<String> images = (ArrayList<String>) restaurantEntity.getProperty("images");

    Restaurant restaurant = new Restaurant(id, name, description, images);

    return restaurant;
  }

  public Set<String> getUsers(){
    Set<String> users = new HashSet<>();
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    for(Entity entity : results.asIterable()) {
      users.add((String) entity.getProperty("user"));
    }
    return users;
  }

  /** Returns the total number of messages for all users. */
  public int getTotalMessageCount(){
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    return results.countEntities(FetchOptions.Builder.withLimit(1000));
  }

  /** Returns the average message length. */
  public double getAverageMessageLength(){
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    double total_length = 0.0;
    double num_messages = 0.0;
    for (Entity entity : results.asIterable()) {
      try {
        String text = (String) entity.getProperty("text");
        total_length += text.length();
        num_messages += 1;
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }
    return total_length / num_messages;
  }

  /** Returns the text of the longest message */
  public String getLongestMessage(){
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    int max_len = 0;
    String msg_contents = "";
    for (Entity entity : results.asIterable()) {
      try {
        String text = (String) entity.getProperty("text");
        int curr_len = text.length();
        if (curr_len > max_len){
          max_len = curr_len;
          msg_contents = text;
        }
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }
    return msg_contents;
  }

  /** Returns the total number of users. */
  public int getTotalUserCount(){
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    HashSet<String> users = new HashSet<String>();
    for (Entity entity : results.asIterable()) {
      try {
        String userName = (String) entity.getProperty("user");
        users.add(userName);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }
    return users.size();
  }

  /** Returns the most active user. */
  public String getMostActiveUser(){

    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    HashMap<String, Integer> messages_per_user = new HashMap<String, Integer>();
    for (Entity entity : results.asIterable()) {
      try {
        String userName = (String) entity.getProperty("user");
        int count = messages_per_user.containsKey(userName) ? messages_per_user.get(userName) : 0;
        messages_per_user.put(userName, count + 1);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }
    String most_active = "";
    int max_messages = 0;
    for (String user : messages_per_user.keySet()) {
      int curr_messages = messages_per_user.get(user);
      if (curr_messages > max_messages){
        max_messages = curr_messages;
        most_active = user;
      }
    }
    return most_active;
  }

  /**
   * Gets messages specified by a query
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessagesFromQuery(Query query) {
    List<Message> messages = new ArrayList<>();

    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");
        String user = entity.getKey().getName();

        Message message = new Message(id, user, text, timestamp);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

/**
   * Gets messages posted by a specific user by calling getMessagesFromQuery method.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessagesForUser(String user) {
    Query query =
        new Query("Message")
            .setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
            .addSort("timestamp", SortDirection.DESCENDING);
    return getMessagesFromQuery(query);
  }

  public List<Message> getMessagesForRestaurant(UUID restaurantId) {
    Query query =
        new Query("Message")
            .setFilter(new Query.FilterPredicate("restaurantId", FilterOperator.EQUAL, restaurantId.toString()))
            .addSort("timestamp", SortDirection.DESCENDING);
    return getMessagesFromQuery(query);
  }

/**
   * Gets all messages posted.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getAllMessages() {
    Query query =
        new Query("Message")
            //.setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
            .addSort("timestamp", SortDirection.DESCENDING);
    return getMessagesFromQuery(query);
  }
}
