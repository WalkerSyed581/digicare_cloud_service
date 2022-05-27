package cloud_conn_service.models;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;


public class CloudReadingRequest {

  public Long id;

  // public Map<Long,Double> readings;

  public double temperature;
  public double heart_rate;
  public double spo2;
  
  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  public Date timestamp;

  public static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  public CloudReadingRequest(){}
  
  public CloudReadingRequest(double temperature, double heart_rate, double spo2, Date timestamp) {
    this.temperature = temperature;
    this.heart_rate = heart_rate;
    this.spo2 = spo2;
    this.timestamp = timestamp;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public double getHeart_rate() {
    return heart_rate;
  }

  public void setHeart_rate(double heart_rate) {
    this.heart_rate = heart_rate;
  }

  public double getSpo2() {
    return spo2;
  }

  public void setSpo2(double spo2) {
    this.spo2 = spo2;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  
  
 

  

 
}