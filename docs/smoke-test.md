# Full-Stack Smoke Test

Run this after `docker compose up -d` to verify the whole system end-to-end.

## 1. Bring up the entire stack

```bash
docker compose up -d
docker compose ps
```

Expect all 13 containers `Up` (or `healthy` where a healthcheck is defined). Allow up to ~60s for Kafka/Keycloak/Postgres to finish starting.

## 2. Get a Keycloak token and confirm gateway auth

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/realms/fulfillment/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=gateway-client&client_secret=local-dev-secret&username=dev-user&password=devpassword" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

curl -s -o /dev/null -w "no-token: %{http_code}\n" http://localhost:8080/api/orders/anything
curl -s -o /dev/null -w "with-token: %{http_code}\n" -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders/nonexistent-order
```

Expect `no-token: 401`, `with-token: 404`.

## 3. Drive the full event chain

Out-of-stock path (no stock seeded for this SKU):

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"customerId":"cust-smoke-1","items":[{"sku":"sku-smoke-1","quantity":1}]}'
```

Check Mailpit (`http://localhost:8025`) for an "Order rejected" email mentioning the `orderId`.

Happy path — seed stock directly in DynamoDB Local first:

```bash
AWS_ACCESS_KEY_ID=fakeMyKeyId AWS_SECRET_ACCESS_KEY=fakeSecretAccessKey aws dynamodb put-item \
  --endpoint-url http://localhost:8000 --region us-east-1 \
  --table-name InventoryItems \
  --item '{"sku": {"S": "sku-smoke-2"}, "quantityAvailable": {"N": "10"}}'
```
(DynamoDB Local validates the *format* of the access key, not its value — a hyphenated key like `local-fake-key` is rejected with "The Access Key ID or security token is invalid"; alphanumeric dummy values like these are the standard convention.)

Then repeat the order POST with `"sku-smoke-2"`, and confirm:

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/shipments/<orderId>
```

returns `"status":"SCHEDULED"`.

## 4. Verify Prometheus and Grafana

```bash
curl -s http://localhost:9090/api/v1/targets \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print([t['labels']['job'] for t in d['data']['activeTargets'] if t['health']=='up'])"
```

Expect all 6 job names. Open `http://localhost:3000` and confirm the "Fulfillment Overview" dashboard renders data after a few requests.
