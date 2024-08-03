package com.example.ExchangeRateService.util;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "item")
@Getter
@Setter
public class Item {
  private String title;
  private String description;
  private String quant;

  @XmlElement(name = "title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @XmlElement(name = "description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @XmlElement(name = "quant")
  public String getQuant() {
    return quant;
  }

  public void setQuant(String quant) {
    this.quant = quant;
  }
}
