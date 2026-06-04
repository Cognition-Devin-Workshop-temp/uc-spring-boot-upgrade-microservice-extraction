package io.spring.shipment.api.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateShipmentRequest {

  @NotBlank private String senderName;
  @NotBlank private String senderAddress;
  @NotBlank private String recipientName;
  @NotBlank private String recipientAddress;
  @NotBlank private String origin;
  @NotBlank private String destination;
  @Positive private double weight;
}
