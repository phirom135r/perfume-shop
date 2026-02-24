package com.perfumeshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoice_sequence")
public class InvoiceSequence {

    @Id
    private Long id; // always 1

    @Column(name = "next_value", nullable = false)
    private Long nextValue = 1L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNextValue() { return nextValue; }
    public void setNextValue(Long nextValue) { this.nextValue = nextValue; }
}