package com.perfumeshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;


@Service
public class InvoiceService {

    private final com.perfumeshop.repository.InvoiceSequenceRepository repo;

    public InvoiceService(com.perfumeshop.repository.InvoiceSequenceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public String nextInvoice() {
        // Always use row id=1 as single sequence
        com.perfumeshop.entity.InvoiceSequence seq = repo.findById(1L).orElseGet(() -> {
            com.perfumeshop.entity.InvoiceSequence s = new com.perfumeshop.entity.InvoiceSequence();
            s.setId(1L);
            s.setNextValue(1L);
            return repo.save(s);
        });

        long current = (seq.getNextValue() == null) ? 1L : seq.getNextValue();
        seq.setNextValue(current + 1);
        repo.save(seq);

        return "INV-" + new DecimalFormat("000000").format(current);
    }
}
