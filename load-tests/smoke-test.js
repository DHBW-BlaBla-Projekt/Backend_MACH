import http from "k6/http";
import { check } from "k6";

export const options = {
  vus: 1,
  iterations: 3,
  thresholds: {
    http_req_failed: ["rate==0"],
    http_req_duration: ["p(95)<1000"]
  }
};

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8081")
  .trim()
  .replace(/\/$/, "");

export default function () {
  const response = http.get(`${BASE_URL}/api/rides/public/available`);

  check(response, {
    "Status ist 200": (r) => r.status === 200,
    "Antwort ist nicht leer": (r) => r.body && r.body.length > 0
  });
}
