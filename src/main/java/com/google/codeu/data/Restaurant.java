package com.google.codeu.data;

import java.util.UUID;

public class Restaurant {

  private UUID id;
  private String name;
  private String description;
  private ArrayList<String> images;
  // private String url;		url to restaurant website?
  // private String menu;		url to menu?
  // private String maps;		url to google maps/link to navigation page?

  // How should the id of the restaurant be determined? (location, hash of name, maps ID, etc?)
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