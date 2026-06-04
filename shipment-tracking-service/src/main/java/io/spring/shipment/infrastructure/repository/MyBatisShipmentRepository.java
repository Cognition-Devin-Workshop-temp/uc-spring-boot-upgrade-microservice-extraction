package io.spring.shipment.infrastructure.repository;

import io.spring.shipment.core.Shipment;
import io.spring.shipment.core.ShipmentRepository;
import io.spring.shipment.core.TrackingEvent;
import io.spring.shipment.infrastructure.mybatis.mapper.ShipmentMapper;
import io.spring.shipment.infrastructure.mybatis.mapper.TrackingEventMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisShipmentRepository implements ShipmentRepository {

  private final ShipmentMapper shipmentMapper;
  private final TrackingEventMapper trackingEventMapper;

  public MyBatisShipmentRepository(
      ShipmentMapper shipmentMapper, TrackingEventMapper trackingEventMapper) {
    this.shipmentMapper = shipmentMapper;
    this.trackingEventMapper = trackingEventMapper;
  }

  @Override
  public void save(Shipment shipment) {
    shipmentMapper.insert(shipment);
  }

  @Override
  public Optional<Shipment> findById(String id) {
    return Optional.ofNullable(shipmentMapper.findById(id));
  }

  @Override
  public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
    return Optional.ofNullable(shipmentMapper.findByTrackingNumber(trackingNumber));
  }

  @Override
  public List<Shipment> findAll(int offset, int limit) {
    return shipmentMapper.findAll(offset, limit);
  }

  @Override
  public int count() {
    return shipmentMapper.count();
  }

  @Override
  public void update(Shipment shipment) {
    shipmentMapper.update(shipment);
  }

  @Override
  public void delete(String id) {
    shipmentMapper.delete(id);
  }

  @Override
  public void saveTrackingEvent(TrackingEvent event) {
    trackingEventMapper.insert(event);
  }

  @Override
  public List<TrackingEvent> findTrackingEvents(String shipmentId) {
    return trackingEventMapper.findByShipmentId(shipmentId);
  }
}
