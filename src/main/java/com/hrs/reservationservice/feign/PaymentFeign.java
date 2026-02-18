package com.hrs.reservationservice.feign;

import com.hrs.reservationservice.dto.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="payment-service",url ="localhost:8081/payment")
public interface PaymentFeign
{
    @PostMapping("/initiate")
    ResponseEntity<Payment> pay(@RequestBody Payment payment);
}
