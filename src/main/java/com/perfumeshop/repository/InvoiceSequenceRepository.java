package com.perfumeshop.repository;

import com.perfumeshop.entity.InvoiceSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Long> {
}