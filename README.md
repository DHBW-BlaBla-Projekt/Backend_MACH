# Backend MACH

Backend des MACH-Projekts, einer webbasierten Mitfahrzentrale. Das Backend
besteht aus mehreren Spring-Boot-Microservices, die über REST, JWT und
RabbitMQ miteinander kommunizieren.

## Architektur

| Service | Aufgabe | Lokaler Port |
|---|---|---:|
| Auth-Service | Registrierung, Login, JWT- und Refresh-Tokens | 8080 |
| Ride-Offer-Service | Fahrten erstellen, suchen, buchen und verwalten | 8081 |
| Booking-Service | Buchungen, Stornierungen und Buchungshistorie | 8082 |
| Profile-Service | Profile, Fahrzeuge, Bewertungen und Fahrzeugdaten | 8084 |

Die Services verwenden jeweils eine PostgreSQL-Datenbank. Ereignisse zwischen
Auth-, Ride-Offer- und Booking-Service werden über RabbitMQ übertragen.

## Voraussetzungen

- Java 17 oder höher
- PostgreSQL
- RabbitMQ
- optional: [k6](https://grafana.com/docs/k6/latest/) für Lasttests
- optional: API-Schlüssel für OpenRouteService

## Repository-Struktur

```text
Auth-Service/          Authentifizierung und Benutzerkonten
ride-offer-service/    Fahrten und Routen
booking-service/       Buchungen
profile-service/       Profile, Fahrzeuge und Bewertungen
load-tests/            Smoke-, REST- und GraphQL-Lasttests
docs/                  Projektdokumente
```

## Lokale Einrichtung

1. PostgreSQL und RabbitMQ starten.
2. Für jeden Service eine eigene Datenbank anlegen.
3. Die benötigten Umgebungsvariablen setzen.
4. Die Services aus ihren jeweiligen Verzeichnissen starten.

Beispiel für die wichtigsten Variablen in PowerShell:

```powershell
$env:JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/mach"
$env:JWT_SECRET="lokales-test-secret"
$env:RABBITMQ_ENABLED="true"
$env:CLOUDAMQP_URL="amqp://guest:guest@localhost:5672"
$env:OPENROUTESERVICE_API_KEY="..."
$env:OPENROUTESERVICE_BASE_URL="https://api.openrouteservice.org"
```

Die Datenbank-URL, JWT-Konfiguration und RabbitMQ-Einstellungen müssen in der
jeweiligen Service-Umgebung passend gesetzt werden. Für lokale
Service-zu-Service-Aufrufe gelten standardmäßig:

```powershell
$env:PROFILE_SERVICE_URL="http://localhost:8084"
$env:BOOKING_SERVICE_URL="http://localhost:8082"
$env:RIDE_OFFER_SERVICE_URL="http://localhost:8081"
```

### Services starten

In jeweils einem Terminal:

```powershell
cd .\Auth-Service
.\gradlew.bat bootRun
```

```powershell
cd .\ride-offer-service
.\gradlew.bat bootRun
```

```powershell
cd .\booking-service
.\gradlew.bat bootRun
```

```powershell
cd .\profile-service
.\gradlew.bat bootRun
```

Der `PORT`-Wert kann in jeder Umgebung überschrieben werden. In der
Produktionsumgebung wird dadurch der von der Plattform vorgegebene Port
verwendet.

## REST-API

Die wichtigsten öffentlichen Endpunkte sind:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token

GET  /api/rides/public/available
GET  /api/rides/public/search
POST /api/rides
PATCH /api/rides/{id}/book

POST /api/bookings
GET  /api/bookings/my-bookings
GET  /api/bookings/my-history

GET  /api/profile/users/{username}/ratings
GET  /api/profile/users/{username}/average-rating
GET  /api/profile/cars
```

Geschützte Endpunkte erwarten ein JWT im Header:

```text
Authorization: Bearer <token>
```

## GraphQL-API

Der Ride-Offer-Service stellt zusätzlich eine öffentliche GraphQL-Query unter
`POST /graphql` bereit. Damit können verfügbare Fahrten abgefragt werden:

```graphql
query {
  availableRides {
    id
    driverUsername
    startLocation
    endLocation
    departureTime
    availableSeats
    status
    carId
    averageConsumptionLPer100km
    fuelType
    estimatedCost
  }
}
```

Beispiel mit `curl`:

```powershell
curl.exe -X POST http://localhost:8081/graphql `
  -H "Content-Type: application/json" `
  -d '{"query":"{ availableRides { id startLocation endLocation } }"}'
```

Die GraphQL-Schnittstelle verwendet dieselben Fahrtdaten wie der
Ride-Offer-Service. ORS wird für Routen- und Entfernungsberechnungen
verwendet. Die Fuel Economy API liefert Verbrauchsdaten; diese werden zur
Reduzierung externer Anfragen gecacht.

## Tests

Die Unit-Tests eines Services können aus dessen Verzeichnis gestartet werden:

```powershell
.\gradlew.bat test
```

Die Lasttests liegen unter [load-tests/](./load-tests/). Sie verwenden k6 und
prüfen den öffentlichen REST- und GraphQL-Endpunkt:

```powershell
k6 run .\load-tests\smoke-test.js
k6 run .\load-tests\rest-load-test.js
k6 run .\load-tests\graphql-load-test.js
```

Lasttests gegen die Produktion dürfen nur nach vorheriger Abstimmung
ausgeführt werden. Weitere Hinweise stehen in
[load-tests/README.md](./load-tests/README.md).

## Deployment

Jeder Service besitzt ein eigenes `Procfile` und kann separat deployed
werden. Für ein produktives Deployment müssen insbesondere folgende Werte
konfiguriert werden:

- `JDBC_DATABASE_URL`
- `JWT_SECRET`
- `CLOUDAMQP_URL`
- `RABBITMQ_ENABLED`
- `PROFILE_SERVICE_URL`
- `BOOKING_SERVICE_URL`
- `RIDE_OFFER_SERVICE_URL`
- `OPENROUTESERVICE_API_KEY`
- `OPENROUTESERVICE_BASE_URL`
- `FUEL_ECONOMY_BASE_URL`
- `FUEL_ECONOMY_CACHE_TTL_HOURS`

Secrets und Zugangsdaten dürfen nicht in das Repository eingecheckt werden.
Die Produktionsdatenbanken und RabbitMQ müssen vor dem Start der Services
erreichbar sein.
