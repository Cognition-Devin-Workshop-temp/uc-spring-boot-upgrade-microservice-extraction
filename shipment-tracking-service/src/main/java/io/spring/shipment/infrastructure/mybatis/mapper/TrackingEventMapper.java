package io.spring.shipment.infrastructure.mybatis.mapper;

import io.spring.shipment.core.TrackingEvent;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrackingEventMapper {

  void insert(@Param("event") TrackingEvent event);

  List<TrackingEvent> findByShipmentId(@Param("shipmentId") String shipmentId);
}
