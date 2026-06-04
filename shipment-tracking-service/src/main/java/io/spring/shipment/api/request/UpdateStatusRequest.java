package io.spring.shipment.api.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateStatusRequest {

  @NotNull private String status;
  @NotBlank private String location;
  @NotBlank private String description;
}
