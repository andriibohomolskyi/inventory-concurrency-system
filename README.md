# Enterprise Inventory Management System

Ein nebenläufiger Spring Boot Microservice, der das Problem von Überverkäufen (Race Conditions) bei starkem Traffic löst. Das Projekt demonstriert den Umgang mit Datenbank-Transaktionen, Thread-Sicherheit und manipulationssicherer Datenhistorisierung.

## Das Business-Problem

Während eines Sales greifen viele Kunden exakt zeitgleich auf denselben, limitierten Lagerbestand zu. Ein naives Backend liest den Bestand in mehreren Threads gleichzeitig, zieht die Kaufmenge ab und überschreibt den Wert. Das Resultat ist ein klassischer Overselling-Bug: Es werden mehr Artikel verkauft, als physisch vorhanden sind. Dies führt zu Stornierungen und Reputationsverlust.

## Die Lösung

Optimistic Locking
Das System verhindert Überverkäufe auf Datenbankebene ohne Performance-Einbußen durch teure Tabellen-Locks. Jeder Datensatz erhält eine Versionierung. Wenn zwei Threads versuchen, gleichzeitig denselben Datensatz zu ändern, wird der erste erfolgreich gespeichert. Der zweite Thread bemerkt die veraltete Version, und die Datenbank blockiert die Transaktion. Der Überverkauf wird abgewehrt.

Immutable Audit Logging
Jede Bestandsänderung wird durch Spring-Transaktionen atomar mit einem unveränderlichen Log-Eintrag verknüpft. Fällt eine Transaktion zurück, wird sichergestellt, dass auch kein fehlerhaftes Log geschrieben wird. Die Datenhistorie bleibt absolut konsistent.

## Tech Stack

- Java 21
- Spring Boot 3, Spring Web
- Spring Data JPA, Hibernate, H2 In-Memory Datenbank
- OpenAPI 3.0
- Thymeleaf, Tailwind CSS
- JUnit 5, ExecutorService

## Systemarchitektur

```mermaid
graph LR
    A[Client UI] -->|POST /buy| B(REST Controller)
    B --> C{Inventory Service}
    C -->|1. Check Stock and Version| D[(Product Table)]
    C -->|2. Save Transaction| E[(Audit Log Table)]


## Lokaler Start

Repository klonen:
```bash
git clone [https://github.com/andriibohomolskyi/inventory-concurrency-system.git](https://github.com/andriibohomolskyi/inventory-concurrency-system.git)
cd inventory-concurrency-system