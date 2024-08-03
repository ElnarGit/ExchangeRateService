package com.example.ExchangeRateService.util;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "channel")
public class Channel {
  private List<Item> item;

  @XmlElement(name = "item")
  public List<Item> getItem() {
    return item;
  }

  public void setItem(List<Item> item) {
    this.item = item;
  }
}
