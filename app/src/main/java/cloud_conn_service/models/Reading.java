package cloud_conn_service.models;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.bson.types.BSONTimestamp;

public class Reading {

  private ObjectId id;

	@BsonProperty(value = "temperature")
	private double temperature;
	@BsonProperty(value = "heart_rate")
	private double heart_rate;
	@BsonProperty(value = "spo2")
	private double spo2;
	
	@BsonProperty(value="timestamp")
	private LocalDateTime timestamp;

	@BsonProperty(value ="sent")
	private boolean sent;

  @BsonProperty(value ="_class")
	private String className;


  public Reading(){

  }


  @Override
  public String toString() {
    return "Reading [id=" + id + ", heart_rate=" + heart_rate + ", sent=" + sent
        + ", spo2=" + spo2 + ", temperature=" + temperature + ", timestamp=" + timestamp + "]";
  }


  public ObjectId getId() {
    return id;
  }


  public void setId(ObjectId id) {
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


  public LocalDateTime getTimestamp() {
    return timestamp;
  }


  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }


  public boolean isSent() {
    return sent;
  }


  public void setSent(boolean sent) {
    this.sent = sent;
  }


  public String getClassName() {
    return className;
  }


  public void setClassName(String className) {
    this.className = className;
  }


}