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

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;


/** A single message posted by a user. */
public class Message {

  private UUID id;
  private String user;
  private String text;
  //private String imageLabel;
  private long timestamp;
  private ArrayList<String> labels;
  private float sentimentScore;
  private int reviewScore;
  private UUID restaurantId;

  /**
   * Constructs a new {@link Message} posted by {@code user} with {@code text} content. Generates a
   * random ID and uses the current system time for the creation time.
   */

  public Message(String user, String text) {
    this(UUID.randomUUID(), user, text, System.currentTimeMillis(), new ArrayList<String>(), (float) 2.0, null, 0);
  }

  public Message(String user, String text, ArrayList<String> labels, float sentimentScore) {
    this(UUID.randomUUID(), user, text, System.currentTimeMillis(), labels, sentimentScore, null, 0);
  }

  public Message(String user, String text, ArrayList<String> labels, float sentimentScore, UUID restaurantId, int reviewScore) {
    this(UUID.randomUUID(), user, text, System.currentTimeMillis(), labels, sentimentScore, restaurantId, reviewScore);
  }

  public Message(UUID id, String user, String text, long timestamp) {
    this(id, user, text, timestamp, new ArrayList<String>(), (float) 2.0, null, 0);
  }

  public Message(UUID id, String user, String text, long timestamp, ArrayList<String> labels, float sentimentScore, UUID restaurantId, int reviewScore) {
    this.id = id;
    this.user = user;
    this.text = text;
    this.timestamp = timestamp;
    this.labels = labels;
    this.sentimentScore = sentimentScore;
    this.restaurantId = restaurantId;
    this.reviewScore = reviewScore;
  }

  public UUID getId() {
    return id;
  }

  public String getUser() {
    return user;
  }

  public String getText() {
    return text;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public ArrayList<String> getImageLabels() {
    return labels;
  }

  public float getSentimentScore(){
    return sentimentScore;
  }

  public UUID getRestaurant() {
    return restaurantId;
  }

  public int getReviewScore() {
    return reviewScore;
  }

  @Override
  public String toString() {
    String returnString = "";
    returnString += "UUID: " + id + "\n";
    returnString += "user: " + user + "\n";
    returnString += "text: " + text + "\n";
    returnString += "timestamp: " + timestamp + "\n";
    returnString += "labels: " + labels.toString() + "\n";
    returnString += "sentimentScore: " + sentimentScore + "\n";
    returnString += "restaurantID: " + restaurantId + "\n";

    return returnString;
  }
}
