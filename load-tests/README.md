# Backend-Load-Tests

Die Tests verwenden [k6](https://grafana.com/docs/k6/latest/) und prüfen den
öffentlichen Ride-Offer-Service über REST und GraphQL.

## Voraussetzungen

```powershell
k6 version
```

Die Skripte führen standardmäßig keine Schreiboperationen aus. Sie lesen nur
verfügbare Fahrten und verändern keine Benutzer-, Fahrt- oder Buchungsdaten.

## Skripte

| Datei | Zweck |
|---|---|
| `smoke-test.js` | Kleiner Funktionstest mit 1 VU und 3 Iterationen |
| `rest-load-test.js` | REST-Lasttest bis 200 virtuelle Benutzer |
| `graphql-load-test.js` | GraphQL-Lasttest bis 200 virtuelle Benutzer |

## Ausführung lokal

Der Ride-Offer-Service muss lokal auf Port `8081` laufen:

```powershell
k6 run .\load-tests\smoke-test.js
k6 run .\load-tests\rest-load-test.js
k6 run .\load-tests\graphql-load-test.js
```

Alternativ kann die URL explizit gesetzt werden:

```powershell
$env:BASE_URL="http://localhost:8081"
k6 run .\load-tests\rest-load-test.js
```

## Ausführung gegen Produktion

Zuerst nur den Smoke-Test ausführen:

```powershell
$env:BASE_URL="https://mfz-rides-8e1df3a252f8.herokuapp.com"
k6 run .\load-tests\smoke-test.js
```

Die beiden Lasttests erzeugen bis zu 200 parallele virtuelle Benutzer. Sie
dürfen gegen Produktion nur nach Freigabe ausgeführt werden:

```powershell
$env:BASE_URL="https://mfz-rides-8e1df3a252f8.herokuapp.com"
k6 run .\load-tests\rest-load-test.js
k6 run .\load-tests\graphql-load-test.js
```

Während eines Heroku-Tests können die Logs beobachtet werden:

```powershell
heroku logs --tail -a mfz-rides
```

## Erfolgskriterien

Die Lasttests verwenden folgende k6-Thresholds:

- Fehlerrate kleiner als 5 Prozent
- 95. Perzentil der Antwortzeit unter 2 Sekunden
- REST-Checks: HTTP 200 und nichtleere Antwort
- GraphQL-Checks: HTTP 200, keine `errors` und vorhandene `data.availableRides`

Die k6-Ausgabe enthält unter anderem Requests, Fehlerrate, durchschnittliche
Antwortzeit sowie p90, p95 und p99.

## Sicherheit und Grenzen

- Keine Registrierung, Fahrtenerstellung, Buchung, Stornierung oder Löschung.
- Keine Zugangsdaten oder Tokens in den Skripten speichern.
- Produktions-Lasttests vorher mit dem Team beziehungsweise Product Owner
  abstimmen.
- Bei unerwarteten 5xx-Fehlern, Timeouts oder hoher Systemlast den Test
  abbrechen.
