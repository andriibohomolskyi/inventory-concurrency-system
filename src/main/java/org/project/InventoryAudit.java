package org.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class InventoryAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;
    private int quantityChanged; // Wie viel wurde gekauft/hinzugefügt?
    private int previousStock;   // Bestand VOR der Änderung
    private int newStock;        // Bestand NACH der Änderung
    private String action;       // z.B. "CUSTOMER_PURCHASE" oder "RESTOCK"
    private LocalDateTime timestamp;

    public InventoryAudit(String sku, int quantityChanged, int previousStock, int newStock, String action) {
        this.sku = sku;
        this.quantityChanged = quantityChanged;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }
}