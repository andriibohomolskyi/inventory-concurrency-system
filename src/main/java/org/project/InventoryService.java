package org.project;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryAuditRepository auditRepository;

    @Transactional
    public void buyProduct(String sku, int quantity) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Produkt existiert nicht: " + sku));

        if (product.getStock() < quantity) {
            throw new IllegalStateException("Nicht genug Bestand für SKU: " + sku);
        }

        int previousStock = product.getStock();
        int newStock = previousStock - quantity;

        // 1. Bestand aktualisieren
        product.setStock(newStock);
        productRepository.save(product);

        // 2. Audit-Log schreiben
        InventoryAudit audit = new InventoryAudit(
                sku,
                -quantity,
                previousStock,
                newStock,
                "CUSTOMER_PURCHASE"
        );
        auditRepository.save(audit);
    }
}


