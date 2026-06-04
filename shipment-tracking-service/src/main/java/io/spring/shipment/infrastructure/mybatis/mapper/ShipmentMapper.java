package io.spring.shipment.infrastructure.mybatis.mapper;

import io.spring.shipment.core.Shipment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShipmentMapper {

  void insert(@Param("shipment") Shipment shipment);

  Shipment findById(@Param("id") String id);

  Shipment findByTrackingNumber(@Param("trackingNumber") String trackingNumber);

  List<Shipment> findAll(@Param("offset") int offset, @Param("limit") int limit);

  int count();

  void update(@Param("shipment") Shipment shipment);

  void delete(@Param("id") String id);
}
