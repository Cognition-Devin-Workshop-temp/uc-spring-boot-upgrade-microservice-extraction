package io.spring.shipment.api.response;

import io.spring.shipment.core.TrackingEvent;
import lombok.Getter;

@Getter
public class TrackingEventResponse {
  private final String id;
  private final String shipmentId;
  private final String status;
  private final String location;
  private final String description;
  private final String createdAt;

  public TrackingEventResponse(TrackingEvent event) {
    this.id = event.getId();
    this.shipmentId = event.getShipmentId();
    this.status = event.getStatus().name();
    this.location = event.getLocation();
    this.description = event.getDescription();
    this.createdAt = event.getCreatedAt().toString();
  }
}
