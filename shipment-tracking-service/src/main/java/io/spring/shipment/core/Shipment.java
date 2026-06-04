package io.spring.shipment.core;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Shipment {
  private String id;
  private String trackingNumber;
  private String senderName;
  private String senderAddress;
  private String recipientName;
  private String recipientAddress;
  private String origin;
  private String destination;
  private double weight;
  private ShipmentStatus status;
  private DateTime createdAt;
  private DateTime updatedAt;

  public Shipment(
      String senderName,
      String senderAddress,
      String recipientName,
      String recipientAddress,
      String origin,
      String destination,
      double weight) {
    this.id = UUID.randomUUID().toString();
    this.trackingNumber = generateTrackingNumber();
    this.senderName = senderName;
    this.senderAddress = senderAddress;
    this.recipientName = recipientName;
    this.recipientAddress = recipientAddress;
    this.origin = origin;
    this.destination = destination;
    this.weight = weight;
    this.status = ShipmentStatus.CREATED;
    this.createdAt = new DateTime();
    this.updatedAt = new DateTime();
  }

  public void updateStatus(ShipmentStatus newStatus) {
    this.status = newStatus;
    this.updatedAt = new DateTime();
  }

  private static String generateTrackingNumber() {
    return "SHP"
        + System.currentTimeMillis()
        + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
  }
}
