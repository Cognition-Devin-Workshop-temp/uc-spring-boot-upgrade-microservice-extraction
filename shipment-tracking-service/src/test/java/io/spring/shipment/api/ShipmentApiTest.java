package io.spring.shipment.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.shipment.application.ShipmentService;
import io.spring.shipment.core.Shipment;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentApiTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ShipmentService shipmentService;

  @Test
  void createShipment_shouldReturnCreatedShipment() throws Exception {
    Map<String, Object> request = createShipmentRequest();

    mockMvc
        .perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shipment.id", notNullValue()))
        .andExpect(jsonPath("$.shipment.trackingNumber", notNullValue()))
        .andExpect(jsonPath("$.shipment.senderName", is("John Doe")))
        .andExpect(jsonPath("$.shipment.origin", is("New York")))
        .andExpect(jsonPath("$.shipment.destination", is("Los Angeles")))
        .andExpect(jsonPath("$.shipment.status", is("CREATED")));
  }

  @Test
  void createShipment_withMissingFields_shouldReturnValidationError() throws Exception {
    Map<String, Object> request = new HashMap<>();
    request.put("senderName", "");

    mockMvc
        .perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void getAllShipments_shouldReturnListOfShipments() throws Exception {
    shipmentService.createShipment("Sender1", "Addr1", "Recipient1", "Addr2", "NYC", "LA", 5.0);

    mockMvc
        .perform(get("/api/shipments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shipments").isArray())
        .andExpect(jsonPath("$.shipmentsCount").isNumber());
  }

  @Test
  void getShipmentById_shouldReturnShipment() throws Exception {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 3.5);

    mockMvc
        .perform(get("/api/shipments/" + shipment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shipment.id", is(shipment.getId())))
        .andExpect(jsonPath("$.shipment.trackingNumber", is(shipment.getTrackingNumber())));
  }

  @Test
  void getShipmentById_notFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/shipments/non-existent-id")).andExpect(status().isNotFound());
  }

  @Test
  void trackShipment_shouldReturnShipmentWithEvents() throws Exception {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 2.0);

    mockMvc
        .perform(get("/api/shipments/track/" + shipment.getTrackingNumber()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shipment.trackingNumber", is(shipment.getTrackingNumber())))
        .andExpect(jsonPath("$.trackingEvents", hasSize(1)))
        .andExpect(jsonPath("$.trackingEvents[0].status", is("CREATED")));
  }

  @Test
  void updateShipmentStatus_shouldUpdateStatusAndAddEvent() throws Exception {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 1.0);

    Map<String, Object> statusUpdate = new HashMap<>();
    statusUpdate.put("status", "PICKED_UP");
    statusUpdate.put("location", "New York Warehouse");
    statusUpdate.put("description", "Package picked up from sender");

    mockMvc
        .perform(
            put("/api/shipments/" + shipment.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shipment.status", is("PICKED_UP")));
  }

  @Test
  void getTrackingEvents_shouldReturnEventHistory() throws Exception {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 4.0);
    shipmentService.updateShipmentStatus(
        shipment.getId(), io.spring.shipment.core.ShipmentStatus.IN_TRANSIT, "Hub", "In transit");

    mockMvc
        .perform(get("/api/shipments/" + shipment.getId() + "/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.trackingEvents", hasSize(2)))
        .andExpect(jsonPath("$.trackingEvents[0].status", is("CREATED")))
        .andExpect(jsonPath("$.trackingEvents[1].status", is("IN_TRANSIT")));
  }

  @Test
  void deleteShipment_shouldReturn204() throws Exception {
    Shipment shipment =
        shipmentService.createShipment("Sender", "Addr1", "Recipient", "Addr2", "NYC", "LA", 1.5);

    mockMvc.perform(delete("/api/shipments/" + shipment.getId())).andExpect(status().isNoContent());
  }

  private Map<String, Object> createShipmentRequest() {
    Map<String, Object> request = new HashMap<>();
    request.put("senderName", "John Doe");
    request.put("senderAddress", "123 Main St, New York");
    request.put("recipientName", "Jane Smith");
    request.put("recipientAddress", "456 Oak Ave, Los Angeles");
    request.put("origin", "New York");
    request.put("destination", "Los Angeles");
    request.put("weight", 10.5);
    return request;
  }
}
