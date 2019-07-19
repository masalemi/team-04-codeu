package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Marker;
import com.google.codeu.data.Message;
import com.google.codeu.data.Restaurant;
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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Handles fetching and saving user data.
 */
@WebServlet("/restaurant")
public class RestaurantServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with the information about a specific restaurant.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    response.setContentType("application/json");

    String restaurantId = request.getParameter("restaurantId");

    if(restaurantId == null || restaurantId.equals("")) {
      // Request is invalid, return empty response
      return;
    }

    Restaurant restaurantData = datastore.getRestaurant(UUID.fromString(restaurantId));

    if(restaurantData == null) {
      return;
    }

    Gson gson = new Gson();
    String json = gson.toJson(restaurantData);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Restaurant}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String restaurantId = request.getParameter("restaurantId");
    if (restaurantId == null || restaurantId.equals("null") || restaurantId.equals("")){
      restaurantId = UUID.randomUUID().toString();
    }
    //String restaurantId = UUID.randomUUID().toString();
    String name = request.getParameter("name");
    String description = request.getParameter("description");
    // String image_string = request.getParameter("images");
    // String[] items = image_string.split(",");
    // List<String> images = Arrays.asList(items);
    ArrayList<String> upload_urls = new ArrayList<String>();

    // for (String image : images) {
    //   String uploadedFileUrl = getUploadedFileUrl(request, "image");
    //   upload_urls.add(uploadedFileUrl);
    // }

    String uploadedFileUrl = getUploadedFileUrl(request, "image");
    if (uploadedFileUrl != null){
      upload_urls.add(uploadedFileUrl.replace("<i>", "_").replace("</i>", "_"));
    }

    Restaurant restaurant = new Restaurant(UUID.fromString(restaurantId), name, description, upload_urls);
    datastore.storeRestaurant(restaurant);

    String cleanName = Jsoup.clean(name, Whitelist.none());
    String cleanDescription = Jsoup.clean(description, Whitelist.none());

    Entity markerEntity = new Entity("Marker");
    markerEntity.setProperty("restaurantId", restaurantId);
    markerEntity.setProperty("lat", Double.parseDouble(request.getParameter("lat")));
    markerEntity.setProperty("lng", Double.parseDouble(request.getParameter("lng")));
    markerEntity.setProperty("content", cleanName + ": " + cleanDescription);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);
    
    response.sendRedirect("/restaurant-page.html?restaurantId=" + restaurantId);
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
