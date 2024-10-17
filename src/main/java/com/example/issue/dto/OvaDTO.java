package com.example.issue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Generated code.
 */
public class OvaDTO {

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXISTING_PROPERTY,
      visible = true,
      property = "image_type")
  @JsonSubTypes({
      @Type(value = ImageUriDTO.class, name = "IMAGE_URI")
  })
  public interface Image {

  }

  private @Valid Image image;

  private @Valid String imageType;

  @JsonProperty("image")
  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  /**
   * The discriminator for the image field
   */
  @JsonProperty("image_type")
  @NotNull
  public String getImageType() {
    return imageType;
  }

  public void setImageType(String imageType) {
    this.imageType = imageType;
  }

  @Override
  public String toString() {
    return "OvaDTO{" +
           "image=" + image +
           ", imageType='" + imageType + '\'' +
           '}';
  }
}
