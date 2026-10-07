package org.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryServiceConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        productRepository.save(new Product("PS5-PRO", "PlayStation 5 Pro", 100)); // Wir haben genau 100 Stück
    }

    @Test
    void testConcurrentOrders() throws InterruptedException {
        int numberOfThreads = 200; // 200 Kunden wollen gleichzeitig kaufen
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // Jeder Kunde will 1 Stück kaufen
                    inventoryService.buyProduct("PS5-PRO", 1);
                    successfulOrders.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException | IllegalStateException e) {
                    // Exception gefangen: Jemand anderes war im gleichen Millisekunden-Bruchteil schneller
                    failedOrders.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Warte, bis alle 200 Threads fertig sind

        Product productInDb = productRepository.findBySku("PS5-PRO").get();

        System.out.println("Erfolgreiche Käufe: " + successfulOrders.get());
        System.out.println("Fehlgeschlagene Käufe: " + failedOrders.get());
        System.out.println("Verbleibender Bestand: " + productInDb.getStock());

        // Assertions: Die Mathematik muss immer exakt aufgehen (Keine Ware geht verloren, kein Überverkauf)
        assertEquals(100, successfulOrders.get() + productInDb.getStock(),
                "Verkaufte Menge + Restbestand muss exakt 100 ergeben!");

        // Es dürfen niemals mehr als 100 Stück verkauft werden
        assertTrue(successfulOrders.get() <= 100,
                "Es dürfen nicht mehr Artikel verkauft werden, als auf Lager sind!");
    }

    }