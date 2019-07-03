package com.google.codeu.data;

import java.util.UUID;
import java.util.ArrayList;

public class Restaurant {

  private UUID id;
  private String name;
  private String description;
  private ArrayList<String> images;

  public Restaurant(UUID id, String name, String description, ArrayList<String> images) {
  	this.id = id;
  	this.name = name;
  	this.description = description;
  	this.images = images;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public ArrayList<String> getImages() {
  	return images;
  }

}