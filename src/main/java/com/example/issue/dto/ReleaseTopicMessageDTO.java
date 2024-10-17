package com.example.issue.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;

public class ReleaseTopicMessageDTO {

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXISTING_PROPERTY,
      visible = true,
      property = "type")
  @JsonSubTypes({
      // In my real code there are more types here
      @Type(value = OvaReleaseEventDTO.class, name = "OVA")
  })
  public interface Payload {

  }

  private @Valid Payload payload;

  public Payload getPayload() {
    return payload;
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }

  @Override
  public String toString() {
    return "ReleaseTopicMessageDTO{" +
           "payload=" + payload +
           '}';
  }
}
