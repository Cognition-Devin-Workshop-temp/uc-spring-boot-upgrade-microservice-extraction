package io.spring.shipment.core;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository {

  void save(Shipment shipment);

  Optional<Shipment> findById(String id);

  Optional<Shipment> findByTrackingNumber(String trackingNumber);

  List<Shipment> findAll(int offset, int limit);

  int count();

  void update(Shipment shipment);

  void delete(String id);

  void saveTrackingEvent(TrackingEvent event);

  List<TrackingEvent> findTrackingEvents(String shipmentId);
}
