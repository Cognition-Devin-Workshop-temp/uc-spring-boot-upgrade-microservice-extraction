package io.spring.shipment.core;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class TrackingEvent {
  private String id;
  private String shipmentId;
  private ShipmentStatus status;
  private String location;
  private String description;
  private DateTime createdAt;

  public TrackingEvent(
      String shipmentId, ShipmentStatus status, String location, String description) {
    this.id = UUID.randomUUID().toString();
    this.shipmentId = shipmentId;
    this.status = status;
    this.location = location;
    this.description = description;
    this.createdAt = new DateTime();
  }
}
