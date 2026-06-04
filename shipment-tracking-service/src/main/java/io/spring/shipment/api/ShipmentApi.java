package io.spring.shipment.api;

import io.spring.shipment.api.exception.ResourceNotFoundException;
import io.spring.shipment.api.request.CreateShipmentRequest;
import io.spring.shipment.api.request.UpdateStatusRequest;
import io.spring.shipment.api.response.ShipmentResponse;
import io.spring.shipment.api.response.TrackingEventResponse;
import io.spring.shipment.application.ShipmentService;
import io.spring.shipment.core.Shipment;
import io.spring.shipment.core.ShipmentStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/shipments")
public class ShipmentApi {

  private final ShipmentService shipmentService;

  public ShipmentApi(ShipmentService shipmentService) {
    this.shipmentService = shipmentService;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createShipment(
      @Valid @RequestBody CreateShipmentRequest request) {
    Shipment shipment =
        shipmentService.createShipment(
            request.getSenderName(),
            request.getSenderAddress(),
            request.getRecipientName(),
            request.getRecipientAddress(),
            request.getOrigin(),
            request.getDestination(),
            request.getWeight());
    Map<String, Object> response = new HashMap<>();
    response.put("shipment", new ShipmentResponse(shipment));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllShipments(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit) {
    List<ShipmentResponse> shipments =
        shipmentService.getAllShipments(offset, limit).stream()
            .map(ShipmentResponse::new)
            .collect(Collectors.toList());
    int count = shipmentService.getShipmentCount();
    Map<String, Object> response = new HashMap<>();
    response.put("shipments", shipments);
    response.put("shipmentsCount", count);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getShipmentById(@PathVariable String id) {
    Shipment shipment =
        shipmentService
            .getShipmentById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
    Map<String, Object> response = new HashMap<>();
    response.put("shipment", new ShipmentResponse(shipment));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/track/{trackingNumber}")
  public ResponseEntity<Map<String, Object>> trackShipment(@PathVariable String trackingNumber) {
    Shipment shipment =
        shipmentService
            .getShipmentByTrackingNumber(trackingNumber)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Shipment not found with tracking number: " + trackingNumber));
    List<TrackingEventResponse> events =
        shipmentService.getTrackingHistory(shipment.getId()).stream()
            .map(TrackingEventResponse::new)
            .collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("shipment", new ShipmentResponse(shipment));
    response.put("trackingEvents", events);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<Map<String, Object>> updateShipmentStatus(
      @PathVariable String id, @Valid @RequestBody UpdateStatusRequest request) {
    ShipmentStatus newStatus;
    try {
      newStatus = ShipmentStatus.valueOf(request.getStatus());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid status: " + request.getStatus());
    }
    Shipment shipment =
        shipmentService.updateShipmentStatus(
            id, newStatus, request.getLocation(), request.getDescription());
    Map<String, Object> response = new HashMap<>();
    response.put("shipment", new ShipmentResponse(shipment));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/events")
  public ResponseEntity<Map<String, Object>> getTrackingEvents(@PathVariable String id) {
    List<TrackingEventResponse> events =
        shipmentService.getTrackingHistory(id).stream()
            .map(TrackingEventResponse::new)
            .collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("trackingEvents", events);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteShipment(@PathVariable String id) {
    shipmentService.deleteShipment(id);
    return ResponseEntity.noContent().build();
  }
}
