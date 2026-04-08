package com.newproject.customer.controller;

import com.newproject.customer.dto.AddressRequest;
import com.newproject.customer.dto.AddressResponse;
import com.newproject.customer.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/{customerId}/addresses")
    public List<AddressResponse> list(@PathVariable Long customerId) {
        return addressService.listForCustomer(customerId);
    }

    @PostMapping("/{customerId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@PathVariable Long customerId, @Valid @RequestBody AddressRequest request) {
        return addressService.create(customerId, request);
    }

    @GetMapping("/{customerId}/addresses/type/{addressType}")
    public AddressResponse getByType(@PathVariable Long customerId, @PathVariable String addressType) {
        return addressService.getForCustomerAndType(customerId, addressType);
    }

    @PutMapping("/{customerId}/addresses/type/{addressType}")
    public AddressResponse upsertByType(
        @PathVariable Long customerId,
        @PathVariable String addressType,
        @Valid @RequestBody AddressRequest request
    ) {
        request.setAddressType(addressType);
        return addressService.upsertForCustomerAndType(customerId, addressType, request);
    }

    @GetMapping("/addresses/{addressId}")
    public AddressResponse get(@PathVariable Long addressId) {
        return addressService.get(addressId);
    }

    @PutMapping("/addresses/{addressId}")
    public AddressResponse update(@PathVariable Long addressId, @Valid @RequestBody AddressRequest request) {
        return addressService.update(addressId, request);
    }

    @DeleteMapping("/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long addressId) {
        addressService.delete(addressId);
    }
}
