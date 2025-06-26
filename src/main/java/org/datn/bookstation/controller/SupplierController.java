package org.datn.bookstation.controller;

import org.datn.bookstation.dto.request.SupplierRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public PaginationResponse<SupplierRepuest> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status
    ) {
        return supplierService.getAllWithPagination(page, size, supplierName, contactName, email, status);
    }

    @PostMapping
    public void addSupplier(@RequestBody SupplierRepuest request) {
        supplierService.addSupplier(request);
    }

    @PutMapping
    public void editSupplier(@RequestBody SupplierRepuest request) {
        supplierService.editSupplier(request);
    }

    @DeleteMapping("/{id}")
    public void deleteSupplier(@PathVariable Integer id) {
        supplierService.deleteSupplier(id);
    }

    @PatchMapping("/status")
    public void upStatus(
            @RequestParam Integer id,
            @RequestParam byte status,
            @RequestParam String updatedBy
    ) {
        supplierService.upStatus(id, status, updatedBy);
    }
}
