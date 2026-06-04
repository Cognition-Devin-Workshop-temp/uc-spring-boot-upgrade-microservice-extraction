package io.spring.shipment.api.response;

import io.spring.shipment.core.Shipment;
import lombok.Getter;

@Getter
public class ShipmentResponse {
  private final String id;
  private final String trackingNumber;
  private final String senderName;
  private final String senderAddress;
  private final String recipientName;
  private final String recipientAddress;
  private final String origin;
  private final String destination;
  private final double weight;
  private final String status;
  private final String createdAt;
  private final String updatedAt;

  public ShipmentResponse(Shipment shipment) {
    this.id = shipment.getId();
    this.trackingNumber = shipment.getTrackingNumber();
    this.senderName = shipment.getSenderName();
    this.senderAddress = shipment.getSenderAddress();
    this.recipientName = shipment.getRecipientName();
    this.recipientAddress = shipment.getRecipientAddress();
    this.origin = shipment.getOrigin();
    this.destination = shipment.getDestination();
    this.weight = shipment.getWeight();
    this.status = shipment.getStatus().name();
    this.createdAt = shipment.getCreatedAt().toString();
    this.updatedAt = shipment.getUpdatedAt().toString();
  }
}
