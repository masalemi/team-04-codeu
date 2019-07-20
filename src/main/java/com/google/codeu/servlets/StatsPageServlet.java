package com.google.codeu.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.codeu.data.Datastore;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import com.google.gson.Gson;


/**
 * Handles fetching site statistics.
 */
@WebServlet("/stats")
public class StatsPageServlet extends HttpServlet{

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with site statistics in JSON.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    response.setContentType("application/json");

    int messageCount = datastore.getTotalMessageCount();
    double averageMessageLength = datastore.getAverageMessageLength();
    String longestMessage = datastore.getLongestMessage();
    int userCount = datastore.getTotalUserCount();
    String mostActiveUser = datastore.getMostActiveUser();
    ArrayList<ArrayList<String>> bestRestaurants = datastore.getBestRestaurants(10);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("messageCount", messageCount);
    jsonObject.addProperty("averageMessageLength", averageMessageLength);
    jsonObject.addProperty("longestMessage", longestMessage);
    jsonObject.addProperty("userCount", userCount);
    jsonObject.addProperty("mostActiveUser", mostActiveUser);
    String restString = new Gson().toJson(bestRestaurants);
    jsonObject.addProperty("bestRestaurants", restString);
    response.getOutputStream().println(jsonObject.toString());
  }
}