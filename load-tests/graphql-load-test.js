import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "2m", target: 200 }
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<2000"]
  }
};

const BASE_URL =
  (__ENV.BASE_URL || "https://mfz-rides-8e1df3a252f8.herokuapp.com")
    .trim()
    .replace(/\/$/, "");

const query = `
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
`;

export default function () {
  const response = http.post(
    `${BASE_URL}/graphql`,
    JSON.stringify({ query }),
    {
      headers: {
        "Content-Type": "application/json"
      }
    }
  );

  const body = response.json();

  check(response, {
    "Status ist 200": (r) => r.status === 200,
    "GraphQL-Antwort enthält keine Fehler": () => body && !body.errors,
    "GraphQL-Daten sind vorhanden": () =>
      body && body.data && body.data.availableRides !== undefined
  });

  sleep(1);
}
