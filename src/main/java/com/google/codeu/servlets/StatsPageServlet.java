package com.google.codeu.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Datastore;
import com.google.gson.JsonObject;

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

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("messageCount", messageCount);
    jsonObject.addProperty("averageMessageLength", averageMessageLength);
    jsonObject.addProperty("longestMessage", longestMessage);
    jsonObject.addProperty("userCount", userCount);
    response.getOutputStream().println(jsonObject.toString());
  }
}