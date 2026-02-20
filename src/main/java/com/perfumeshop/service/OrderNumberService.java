package com.perfumeshop.service;

import com.perfumeshop.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderNumberService {

    private final OrderRepository orderRepo;

    public OrderNumberService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    public String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        for (int i = 0; i < 10; i++) {
            int rand = ThreadLocalRandom.current().nextInt(0, 10000);
            String orderNo = "ORD" + date + String.format("%04d", rand);
            if (!orderRepo.existsByOrderNo(orderNo)) return orderNo;
        }
        // fallback (rare)
        return "ORD" + date + System.currentTimeMillis();
    }
}