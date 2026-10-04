# Local Smoke Test

End-to-end verification of the full stack.

**Assumes a clean stack.** Every run should start from the same state — DynamoDB Local is in-memory, so a `down`/`up` cycle wipes seeded stock and gives you a deterministic baseline.

Each step has an expected result. If a step doesn't produce what's expected, stop and check `docker compose logs <service>` before proceeding to the next one.

---

## 1. Bring up the stack

```zsh
docker compose down
docker compose up -d
sleep 30
docker compose ps
```

**Expected:** 13 containers `Up` (or `Up (healthy)`

---

## 2. Gateway health

```zsh
curl -s http://localhost:8080/actuator/health
```

**Expected:** `{"status":"UP"}`

---

## 3. Get a Keycloak token

```zsh
TOKEN=$(curl -s -X POST http://localhost:8081/realms/fulfillment/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=gateway-client&client_secret=local-dev-secret&username=dev-user&password=devpassword" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

echo "Token length: ${#TOKEN}"
```

**Expected:** `Token length:` followed by a number in the hundreds or thousands. If it's `0` or empty, Keycloak isn't ready yet — wait 15 seconds and retry.

**Note:** this token expires in 5 minutes. If later steps return `401`, re-run this block to get a fresh one.

---

## 4. Verify auth is enforced

```zsh
curl -s -o /dev/null -w "no-token:   %{http_code}\n" http://localhost:8080/api/orders/anything
curl -s -o /dev/null -w "with-token: %{http_code}\n" -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders/anything
```

**Expected:**
```
no-token:   401
with-token: 404
```

`401` proves the gateway rejected an unauthenticated request. `404` proves the token passed validation, the request reached `order-service`, and the order simply doesn't exist. Both outcomes are correct.

---

## 5. Rejection path (out-of-stock order)

```zsh
REJECTED_ID=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-x","items":[{"sku":"no-stock-sku","quantity":1}]}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['orderId'])")

echo "Rejected order ID: $REJECTED_ID"
sleep 3
```

**Expected:** A UUID printed to the terminal.

Now verify the rejection event reached the alert service:

```zsh
curl -s http://localhost:8025/api/v1/messages \
  | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(f'{len(d[\"messages\"])} messages in Mailpit')
print(f'newest subject: {d[\"messages\"][0][\"Subject\"]}')
"
```

**Expected:** At least one message, newest subject containing `Order rejected: <your-order-id>`. Or open `http://localhost:8025` in a browser for the visual version.

---

## 6. Happy path (sufficient stock)

First, seed stock directly into DynamoDB Local:

```zsh
AWS_ACCESS_KEY_ID=local AWS_SECRET_ACCESS_KEY=local \
aws dynamodb put-item \
  --endpoint-url http://localhost:8000 \
  --region us-east-1 \
  --table-name InventoryItems \
  --item '{"sku": {"S": "sku-happy-1"}, "quantityAvailable": {"N": "10"}}'
```

**Expected:** no output on success.

Now create an order for that SKU:

```zsh
ORDER_ID=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-y","items":[{"sku":"sku-happy-1","quantity":2}]}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['orderId'])")

echo "Happy order ID: $ORDER_ID"
sleep 3
```

**Expected:** A UUID printed to the terminal.

Now verify the shipment was created:

```zsh
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/shipments/$ORDER_ID" | python3 -m json.tool
```

**Expected:**
```json
{
    "shipmentId": "...",
    "orderId": "<your-order-id>",
    "status": "SCHEDULED",
    "updatedAt": "..."
}
```

Finally, confirm stock was decremented:

```zsh
AWS_ACCESS_KEY_ID=local AWS_SECRET_ACCESS_KEY=local \
aws dynamodb get-item \
  --endpoint-url http://localhost:8000 \
  --region us-east-1 \
  --table-name InventoryItems \
  --key '{"sku": {"S": "sku-happy-1"}}'
```

**Expected:** `"quantityAvailable": {"N": "8"}` (10 − 2).

---

## 7. Prometheus scrape check

```zsh
curl -s http://localhost:9090/api/v1/targets \
  | python3 -c "
import sys, json
data = json.load(sys.stdin)
up = {t['labels']['job'] for t in data['data']['activeTargets'] if t['health'] == 'up'}
expected = {'order-service', 'inventory-service', 'fulfillment-service',
            'alert-service', 'insight-service', 'api-gateway'}
missing = expected - up
print(f'up: {sorted(up)}')
if missing:
    print(f'MISSING: {sorted(missing)}')
    sys.exit(1)
print('all 6 services scraped OK')
"
```

**Expected:** `all 6 services scraped OK`.

---

## 8. End-to-end verification summary

| # | Check | Expected |
|---|---|---|
| 1 | Containers up | 13 `Up`/`healthy` |
| 2 | Gateway health | `{"status":"UP"}` |
| 3 | Token acquisition | Non-empty `$TOKEN` |
| 4 | Auth enforcement | `401` without token, `404` with |
| 5 | Rejection path | Rejection email in Mailpit |
| 6 | Happy path | Shipment `SCHEDULED`, stock `8` |
| 7 | Observability | All 6 services scraped |

If all seven pass, the stack is healthy end-to-end. If any fail, the failure is the fastest place to start debugging.

---

## Between runs

To reset to a clean state:

```zsh
docker compose down
docker compose up -d
sleep 30
```

DynamoDB Local runs `-inMemory`, so all seeded stock is wiped on `down`. Postgres and Kafka also lose their data at this phase (no named volumes are configured in `docker-compose.yml`). This is intentional — every smoke-test run starts from the same baseline.

To stop without wiping:

```zsh
docker compose stop
```

To restart after a `stop`:

```zsh
docker compose start
```