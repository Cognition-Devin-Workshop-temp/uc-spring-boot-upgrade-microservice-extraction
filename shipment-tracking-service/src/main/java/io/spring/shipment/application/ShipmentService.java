package io.spring.shipment.application;

import io.spring.shipment.core.Shipment;
import io.spring.shipment.core.ShipmentRepository;
import io.spring.shipment.core.ShipmentStatus;
import io.spring.shipment.core.TrackingEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

  private final ShipmentRepository shipmentRepository;

  public ShipmentService(ShipmentRepository shipmentRepository) {
    this.shipmentRepository = shipmentRepository;
  }

  public Shipment createShipment(
      String senderName,
      String senderAddress,
      String recipientName,
      String recipientAddress,
      String origin,
      String destination,
      double weight) {
    Shipment shipment =
        new Shipment(
            senderName,
            senderAddress,
            recipientName,
            recipientAddress,
            origin,
            destination,
            weight);
    shipmentRepository.save(shipment);

    TrackingEvent event =
        new TrackingEvent(
            shipment.getId(), ShipmentStatus.CREATED, origin, "Shipment created and registered");
    shipmentRepository.saveTrackingEvent(event);

    return shipment;
  }

  public Optional<Shipment> getShipmentById(String id) {
    return shipmentRepository.findById(id);
  }

  public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
    return shipmentRepository.findByTrackingNumber(trackingNumber);
  }

  public List<Shipment> getAllShipments(int offset, int limit) {
    return shipmentRepository.findAll(offset, limit);
  }

  public int getShipmentCount() {
    return shipmentRepository.count();
  }

  public Shipment updateShipmentStatus(
      String shipmentId, ShipmentStatus newStatus, String location, String description) {
    Shipment shipment =
        shipmentRepository
            .findById(shipmentId)
            .orElseThrow(
                () -> new IllegalArgumentException("Shipment not found with id: " + shipmentId));

    shipment.updateStatus(newStatus);
    shipmentRepository.update(shipment);

    TrackingEvent event = new TrackingEvent(shipmentId, newStatus, location, description);
    shipmentRepository.saveTrackingEvent(event);

    return shipment;
  }

  public List<TrackingEvent> getTrackingHistory(String shipmentId) {
    shipmentRepository
        .findById(shipmentId)
        .orElseThrow(
            () -> new IllegalArgumentException("Shipment not found with id: " + shipmentId));
    return shipmentRepository.findTrackingEvents(shipmentId);
  }

  public void deleteShipment(String id) {
    shipmentRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Shipment not found with id: " + id));
    shipmentRepository.delete(id);
  }
}
