package io.spring.shipment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.shipment.core.Shipment;
import io.spring.shipment.core.ShipmentStatus;
import io.spring.shipment.core.TrackingEvent;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShipmentServiceTest {

  @Autowired private ShipmentService shipmentService;

  @Test
  void createShipment_shouldCreateWithInitialStatus() {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 5.0);

    assertNotNull(shipment.getId());
    assertNotNull(shipment.getTrackingNumber());
    assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    assertEquals("NYC", shipment.getOrigin());
    assertEquals("LA", shipment.getDestination());
  }

  @Test
  void getShipmentById_shouldReturnShipment() {
    Shipment created =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 3.0);

    Optional<Shipment> found = shipmentService.getShipmentById(created.getId());

    assertTrue(found.isPresent());
    assertEquals(created.getId(), found.get().getId());
  }

  @Test
  void getShipmentByTrackingNumber_shouldReturnShipment() {
    Shipment created =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 2.0);

    Optional<Shipment> found =
        shipmentService.getShipmentByTrackingNumber(created.getTrackingNumber());

    assertTrue(found.isPresent());
    assertEquals(created.getTrackingNumber(), found.get().getTrackingNumber());
  }

  @Test
  void updateShipmentStatus_shouldUpdateStatusAndCreateEvent() {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 1.0);

    Shipment updated =
        shipmentService.updateShipmentStatus(
            shipment.getId(), ShipmentStatus.PICKED_UP, "NYC Warehouse", "Picked up");

    assertEquals(ShipmentStatus.PICKED_UP, updated.getStatus());

    List<TrackingEvent> events = shipmentService.getTrackingHistory(shipment.getId());
    assertEquals(2, events.size());
    assertEquals(ShipmentStatus.CREATED, events.get(0).getStatus());
    assertEquals(ShipmentStatus.PICKED_UP, events.get(1).getStatus());
  }

  @Test
  void updateShipmentStatus_nonExistent_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            shipmentService.updateShipmentStatus(
                "non-existent", ShipmentStatus.IN_TRANSIT, "Hub", "In transit"));
  }

  @Test
  void getTrackingHistory_shouldReturnOrderedEvents() {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 4.0);
    shipmentService.updateShipmentStatus(
        shipment.getId(), ShipmentStatus.PICKED_UP, "NYC", "Picked up");
    shipmentService.updateShipmentStatus(
        shipment.getId(), ShipmentStatus.IN_TRANSIT, "Hub", "In transit");
    shipmentService.updateShipmentStatus(
        shipment.getId(), ShipmentStatus.DELIVERED, "LA", "Delivered");

    List<TrackingEvent> events = shipmentService.getTrackingHistory(shipment.getId());

    assertEquals(4, events.size());
    assertEquals(ShipmentStatus.CREATED, events.get(0).getStatus());
    assertEquals(ShipmentStatus.PICKED_UP, events.get(1).getStatus());
    assertEquals(ShipmentStatus.IN_TRANSIT, events.get(2).getStatus());
    assertEquals(ShipmentStatus.DELIVERED, events.get(3).getStatus());
  }

  @Test
  void deleteShipment_nonExistent_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class, () -> shipmentService.deleteShipment("non-existent-id"));
  }

  @Test
  void getAllShipments_shouldReturnPaginatedResults() {
    shipmentService.createShipment("S1", "A1", "R1", "A2", "NYC", "LA", 1.0);
    shipmentService.createShipment("S2", "A3", "R2", "A4", "CHI", "SF", 2.0);

    List<Shipment> shipments = shipmentService.getAllShipments(0, 10);

    assertTrue(shipments.size() >= 2);
  }
}
