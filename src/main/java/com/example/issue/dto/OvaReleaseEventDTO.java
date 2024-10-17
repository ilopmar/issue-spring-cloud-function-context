package com.example.issue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Generated code.
 */
public class OvaReleaseEventDTO implements ReleaseTopicMessageDTO.Payload {

  private @Valid OvaDTO ova;

  private @Valid TypeEnum type;

  private @Valid java.time.OffsetDateTime emittedOn;

  @JsonProperty("type")
  @NotNull
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  @JsonProperty("ova")
  public OvaDTO getOva() {
    return ova;
  }

  public void setOva(OvaDTO ova) {
    this.ova = ova;
  }

  @JsonProperty("emitted_on")
  @NotNull
  public OffsetDateTime getEmittedOn() {
    return emittedOn;
  }

  public void setEmittedOn(OffsetDateTime emittedOn) {
    this.emittedOn = emittedOn;
  }

  public enum TypeEnum {
    // In my real code there are more types here
    OVA(String.valueOf("OVA"));

    private String value;

    TypeEnum(String v) {
      value = v;
    }

    public String value() {
      return value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (Objects.equals(b.value, value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Override
  public String toString() {
    return "OvaReleaseEventDTO{" +
           "ova=" + ova +
           ", type=" + type +
           ", emittedOn=" + emittedOn +
           '}';
  }
}
