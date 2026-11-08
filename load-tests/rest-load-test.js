import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "10m", target: 200 }
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(90)<2000"]
  }
};

const BASE_URL =
  (__ENV.BASE_URL || "https://mfz-rides-8e1df3a252f8.herokuapp.com")
    .trim()
    .replace(/\/$/, "");

export default function () {
  const response = http.get(
    `${BASE_URL}/api/rides/public/available`
  );

  check(response, {
    "Status ist 200": (r) => r.status === 200,
    "Antwort ist nicht leer": (r) => r.body && r.body.length > 0
  });

  sleep(1);
}
