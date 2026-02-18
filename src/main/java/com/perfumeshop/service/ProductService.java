package com.perfumeshop.service;

import com.perfumeshop.entity.Product;
import com.perfumeshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    public Page<Product> list(int page, int size){
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("id").descending());

        return repo.findAll(pageable);
    }

    public Product save(Product p){
        return repo.save(p);
    }

    public Product find(Long id){
        return repo.findById(id).orElse(null);
    }

    public void delete(Long id){
        repo.deleteById(id);
    }
}