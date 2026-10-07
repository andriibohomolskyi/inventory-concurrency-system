package org.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "Endpoints für das Bestandsmanagement")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAuditRepository auditRepository;

    @Operation(summary = "Produkt kaufen", description = "Reduziert den Bestand eines Artikels sicher via Optimistic Locking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kauf erfolgreich"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage (z.B. nicht genug Bestand)",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Konflikt (Race Condition erkannt - jemand war schneller)",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden")
    })
    @PostMapping("/buy/{sku}")
    public ResponseEntity<String> buyProduct(@PathVariable String sku, @RequestParam int quantity) {
        try {
            inventoryService.buyProduct(sku, quantity);
            return ResponseEntity.ok("Erfolgreich " + quantity + " Stück von " + sku + " gekauft.");
        } catch (ObjectOptimisticLockingFailureException e) {
            // Das ist unser Concurrency-Schutz, der hier an das Frontend gemeldet wird!
            return ResponseEntity.status(409).body("Kauf fehlgeschlagen. Der Bestand hat sich in der Zwischenzeit geändert. Bitte neu laden.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Audit-Log abrufen", description = "Liefert die komplette Änderungshistorie eines Produkts.")
    @GetMapping("/{sku}/audit")
    public ResponseEntity<?> getAuditLog(@PathVariable String sku) {
        var auditLog = auditRepository.findBySkuOrderByTimestampDesc(sku);
        if (auditLog.isEmpty()) {
            return ResponseEntity.ok("Noch keine Historie für " + sku + " vorhanden.");
        }
        return ResponseEntity.ok(auditLog);
    }
}