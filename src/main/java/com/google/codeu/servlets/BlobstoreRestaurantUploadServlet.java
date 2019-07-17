package com.google.codeu.servlets;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the fetch() function requests the /blobstore-upload-url URL, the content of the
 * response is the URL that allows a user to upload a file to Blobstore.
 * If this sounds confusing, try running a devserver and navigating to /blobstore-upload-url
 * to see the Blobstore URL.
 */
@WebServlet("/blobstore-upload-url-r")
public class BlobstoreRestaurantUploadServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl("/restaurant");

    response.setContentType("text/html");
    response.getOutputStream().println(uploadUrl);
  }
}
