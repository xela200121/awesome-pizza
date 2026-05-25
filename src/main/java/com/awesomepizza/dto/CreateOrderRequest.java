package com.awesomepizza.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String customerName;

    @NotNull
    @NotEmpty
    private List<@Valid CreateOrderItemRequest> items;
}
