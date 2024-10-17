package com.example.issue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Generated code.
 */
public class ImageUriDTO implements OvaDTO.Image {

  private @Valid String location;

  @JsonProperty("location")
  @NotNull
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public String toString() {
    return "ImageUriDTO{" +
           "location='" + location + '\'' +
           '}';
  }
}
