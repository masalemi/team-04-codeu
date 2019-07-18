package com.google.codeu.data;
import java.util.UUID;

public class Marker {

  private UUID restaurantId;
  private double lat;
  private double lng;
  private String content;


  public Marker(UUID restaurantId, double lat, double lng, String content) {
    this.restaurantId = restaurantId;
    this.lat = lat;
    this.lng = lng;
    this.content = content;
  }

  public UUID getRestaurant() {
    return restaurantId;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public String getContent() {
    return content;
  }


}
