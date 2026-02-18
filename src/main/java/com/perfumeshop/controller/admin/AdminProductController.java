package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Product;
import com.perfumeshop.repository.CategoryRepository;
import com.perfumeshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/products")
public class AdminProductController {

    @Autowired
    private ProductService service;

    @Autowired
    private CategoryRepository categoryRepo;

    // LIST
    @GetMapping
    public Object list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size){

        return service.list(page, size);
    }

    // SAVE
    @PostMapping
    public Product save(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam Long categoryId,
            @RequestParam Double price,
            @RequestParam Double discount,
            @RequestParam Integer stock,
            @RequestParam(required=false) MultipartFile image
    ) throws Exception {

        Product p = (id!=0)
                ? service.find(id)
                : new Product();

        p.setName(name);
        p.setBrand(brand);
        p.setCategory(
                categoryRepo.findById(categoryId).orElse(null)
        );
        p.setPrice(price);
        p.setDiscount(discount);
        p.setStock(stock);

        if(image!=null && !image.isEmpty()){

            String fileName =
                    UUID.randomUUID()+"_"+image.getOriginalFilename();

            Path path =
                    Paths.get("src/main/resources/static/upload/"+fileName);

            Files.copy(image.getInputStream(), path,
                    StandardCopyOption.REPLACE_EXISTING);

            p.setImage(fileName);
        }

        return service.save(p);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        service.delete(id);
    }
}