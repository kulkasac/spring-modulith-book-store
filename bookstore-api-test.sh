#!/bin/bash
# ============================================================
#  Bookstore API - End-to-End Simulation Script
#  Tests: Inventory, Orders, Payments, Metrics
# ============================================================

BASE_URL="http://localhost:8080"
PASS=0
FAIL=0

# ── Colours ─────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

# ── Helpers ──────────────────────────────────────────────────
header() {
    echo ""
    echo -e "${CYAN}${BOLD}═══════════════════════════════════════${RESET}"
    echo -e "${CYAN}${BOLD}  $1${RESET}"
    echo -e "${CYAN}${BOLD}═══════════════════════════════════════${RESET}"
}

step() {
    echo -e "\n${YELLOW}▶ $1${RESET}"
}

pass() {
    echo -e "${GREEN}  ✅ $1${RESET}"
    ((PASS++))
}

fail() {
    echo -e "${RED}  ❌ $1${RESET}"
    ((FAIL++))
}

info() {
    echo -e "  ℹ️  $1"
}

assert_http() {
    local label=$1
    local expected=$2
    local actual=$3
    if [ "$actual" == "$expected" ]; then
        pass "$label → HTTP $actual"
    else
        fail "$label → Expected HTTP $expected, got HTTP $actual"
    fi
}

assert_json_value() {
    local label=$1
    local expected=$2
    local actual=$3
    if [ "$actual" == "$expected" ]; then
        pass "$label → $actual"
    else
        fail "$label → Expected '$expected', got '$actual'"
    fi
}

extract_location_id() {
    # Extracts UUID from Location header
    echo "$1" | grep -i "^location:" | grep -oE '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}'
}

# ── Unique Run ID (ensures ISBNs are unique across runs) ─────
RUN_ID=$(date +%s)
ISBN_1="978-${RUN_ID}-001"
ISBN_2="978-${RUN_ID}-002"

# ── Prerequisite Check ───────────────────────────────────────
header "Prerequisite Check"

step "Checking app is reachable at $BASE_URL"
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
if [ "$HEALTH" == "200" ]; then
    pass "App is running"
else
    fail "App not reachable at $BASE_URL (got HTTP $HEALTH). Is it started?"
    echo ""
    echo -e "${RED}  Aborting. Please start the application first.${RESET}"
    exit 1
fi

# ────────────────────────────────────────────────────────────
# SECTION 1 — INVENTORY
# ────────────────────────────────────────────────────────────
header "Section 1 — Inventory"

# 1.1 Add Book (Happy Path)
step "1.1  Adding book: Clean Code (stock: 10)"
RESPONSE=$(curl -s -D - -X POST "$BASE_URL/api/inventory/books" \
    -H "Content-Type: application/json" \
    -d "{\"isbn\":\"$ISBN_1\",\"title\":\"Clean Code\",\"initialStock\":10}")

HTTP_CODE=$(echo "$RESPONSE" | grep -m1 "^HTTP" | awk '{print $2}')
BOOK_ID=$(extract_location_id "$RESPONSE")

assert_http "Add book" "201" "$HTTP_CODE"
if [ -n "$BOOK_ID" ]; then
    pass "Book ID extracted → $BOOK_ID"
else
    fail "Could not extract Book ID from Location header"
    exit 1
fi

# 1.2 Get single book and verify stock
step "1.2  Verifying book details and stock"
BOOK_JSON=$(curl -s "$BASE_URL/api/inventory/books/$BOOK_ID")
STOCK=$(echo "$BOOK_JSON" | jq -r '.stock')
TITLE=$(echo "$BOOK_JSON" | jq -r '.title')

assert_json_value "Title" "Clean Code" "$TITLE"
assert_json_value "Initial stock" "10" "$STOCK"

# 1.3 Add a second book
step "1.3  Adding book: The Pragmatic Programmer (stock: 5)"
RESPONSE2=$(curl -s -D - -X POST "$BASE_URL/api/inventory/books" \
    -H "Content-Type: application/json" \
    -d "{\"isbn\":\"$ISBN_2\",\"title\":\"The Pragmatic Programmer\",\"initialStock\":5}")

HTTP_CODE2=$(echo "$RESPONSE2" | grep -m1 "^HTTP" | awk '{print $2}')
BOOK_ID2=$(extract_location_id "$RESPONSE2")
assert_http "Add second book" "201" "$HTTP_CODE2"

# 1.4 List all books
step "1.4  Listing all books"
ALL_BOOKS=$(curl -s "$BASE_URL/api/inventory/books")
BOOK_COUNT=$(echo "$ALL_BOOKS" | jq '. | length')
if [ "$BOOK_COUNT" -ge 2 ]; then
    pass "Book list returned $BOOK_COUNT books"
else
    fail "Expected at least 2 books, got $BOOK_COUNT"
fi

# 1.5 Increase stock
step "1.5  Increasing stock of Clean Code by 5"
HTTP_INCREASE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X PATCH "$BASE_URL/api/inventory/books/$BOOK_ID/increase-stock" \
    -H "Content-Type: application/json" \
    -d '{"quantity":5}')
assert_http "Increase stock" "204" "$HTTP_INCREASE"

STOCK_AFTER=$(curl -s "$BASE_URL/api/inventory/books/$BOOK_ID" | jq -r '.stock')
assert_json_value "Stock after increase" "15" "$STOCK_AFTER"

# 1.6 Validation — blank ISBN
step "1.6  Validation: blank ISBN should return 400"
HTTP_VALIDATION=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/inventory/books" \
    -H "Content-Type: application/json" \
    -d '{"isbn":"","title":"Bad Book","initialStock":5}')
assert_http "Blank ISBN rejected" "400" "$HTTP_VALIDATION"

# 1.7 Validation — negative stock
step "1.7  Validation: negative initial stock should return 400"
HTTP_NEG=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/inventory/books" \
    -H "Content-Type: application/json" \
    -d '{"isbn":"999-test","title":"Bad Book","initialStock":-1}')
assert_http "Negative stock rejected" "400" "$HTTP_NEG"

# ────────────────────────────────────────────────────────────
# SECTION 2 — ORDERS
# ────────────────────────────────────────────────────────────
header "Section 2 — Orders"

# 2.1 Place order (Happy Path)
step "2.1  Placing order: 3 x Clean Code (stock should go 15 → 12)"
ORDER_RESPONSE=$(curl -s -D - -X POST "$BASE_URL/api/orders" \
    -H "Content-Type: application/json" \
    -d "{\"bookId\":\"$BOOK_ID\",\"quantity\":3}")

ORDER_HTTP=$(echo "$ORDER_RESPONSE" | grep -m1 "^HTTP" | awk '{print $2}')
ORDER_ID=$(extract_location_id "$ORDER_RESPONSE")

assert_http "Place order" "201" "$ORDER_HTTP"
if [ -n "$ORDER_ID" ]; then
    pass "Order ID extracted → $ORDER_ID"
else
    fail "Could not extract Order ID from Location header"
fi

# 2.2 Verify order details
step "2.2  Verifying order details"
ORDER_JSON=$(curl -s "$BASE_URL/api/orders/$ORDER_ID")
ORDER_STATUS=$(echo "$ORDER_JSON" | jq -r '.status')
ORDER_QTY=$(echo "$ORDER_JSON" | jq -r '.quantity')

assert_json_value "Order status" "CREATED" "$ORDER_STATUS"
assert_json_value "Order quantity" "3" "$ORDER_QTY"

# 2.3 Verify stock decreased
step "2.3  Verifying stock decreased after order"
STOCK_AFTER_ORDER=$(curl -s "$BASE_URL/api/inventory/books/$BOOK_ID" | jq -r '.stock')
assert_json_value "Stock after order (15-3)" "12" "$STOCK_AFTER_ORDER"

# 2.4 Place second order (different book)
step "2.4  Placing order: 1 x The Pragmatic Programmer"
ORDER_RESPONSE2=$(curl -s -D - -X POST "$BASE_URL/api/orders" \
    -H "Content-Type: application/json" \
    -d "{\"bookId\":\"$BOOK_ID2\",\"quantity\":1}")
ORDER_HTTP2=$(echo "$ORDER_RESPONSE2" | grep -m1 "^HTTP" | awk '{print $2}')
assert_http "Place second order" "201" "$ORDER_HTTP2"

# 2.5 List all orders
step "2.5  Listing all orders"
ALL_ORDERS=$(curl -s "$BASE_URL/api/orders")
ORDER_COUNT=$(echo "$ALL_ORDERS" | jq '. | length')
if [ "$ORDER_COUNT" -ge 2 ]; then
    pass "Order list returned $ORDER_COUNT orders"
else
    fail "Expected at least 2 orders, got $ORDER_COUNT"
fi

# 2.6 Insufficient stock (Failure Path)
step "2.6  Failure: ordering 999 copies (insufficient stock)"
HTTP_INSUFF=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/orders" \
    -H "Content-Type: application/json" \
    -d "{\"bookId\":\"$BOOK_ID\",\"quantity\":999}")
assert_http "Insufficient stock rejected" "422" "$HTTP_INSUFF"

# 2.7 Validation — null bookId
step "2.7  Validation: null bookId should return 400"
HTTP_NULL=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/orders" \
    -H "Content-Type: application/json" \
    -d '{"bookId":null,"quantity":1}')
assert_http "Null bookId rejected" "400" "$HTTP_NULL"

# 2.8 Not found
step "2.8  Get non-existent order → 404"
HTTP_404=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/api/orders/00000000-0000-0000-0000-000000000000")
assert_http "Order not found" "404" "$HTTP_404"

# ────────────────────────────────────────────────────────────
# SECTION 3 — METRICS
# ────────────────────────────────────────────────────────────
header "Section 3 — Metrics & Observability"

# 3.1 Orders placed counter
step "3.1  Checking bookstore.orders.placed counter"
ORDERS_COUNT=$(curl -s "$BASE_URL/actuator/metrics/bookstore.orders.placed" \
    | jq -r '.measurements[0].value')
if (( $(echo "$ORDERS_COUNT >= 2" | bc -l) )); then
    pass "Orders placed counter → $ORDERS_COUNT"
else
    fail "Expected orders.placed >= 2, got $ORDERS_COUNT"
fi

# 3.2 Payments completed counter
step "3.2  Checking bookstore.payments.completed counter"
PAYMENTS_OK=$(curl -s "$BASE_URL/actuator/metrics/bookstore.payments.completed" \
    | jq -r '.measurements[0].value')
if (( $(echo "$PAYMENTS_OK >= 2" | bc -l) )); then
    pass "Payments completed counter → $PAYMENTS_OK"
else
    fail "Expected payments.completed >= 2, got $PAYMENTS_OK"
fi

# 3.3 Modulith endpoint
step "3.3  Checking /actuator/modulith"
MODULITH_HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/modulith")
assert_http "Modulith endpoint" "200" "$MODULITH_HTTP"

# 3.4 Prometheus endpoint
step "3.4  Checking /actuator/prometheus"
PROM_HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/prometheus")
assert_http "Prometheus endpoint" "200" "$PROM_HTTP"

step "3.5  Prometheus bookstore metrics"
PROM_LINES=$(curl -s "$BASE_URL/actuator/prometheus" | grep "^bookstore_")
if [ -n "$PROM_LINES" ]; then
    pass "Prometheus bookstore metrics found:"
    echo "$PROM_LINES" | while read -r line; do
        info "$line"
    done
else
    fail "No bookstore_ metrics found in Prometheus output"
fi

# ────────────────────────────────────────────────────────────
# SUMMARY
# ────────────────────────────────────────────────────────────
header "Test Summary"
TOTAL=$((PASS + FAIL))
echo -e "  Total  : ${BOLD}$TOTAL${RESET}"
echo -e "  ${GREEN}Passed : $PASS${RESET}"
echo -e "  ${RED}Failed : $FAIL${RESET}"
echo ""
if [ "$FAIL" -eq 0 ]; then
    echo -e "${GREEN}${BOLD}  🎉 All tests passed!${RESET}"
else
    echo -e "${RED}${BOLD}  ⚠️  $FAIL test(s) failed. Check output above.${RESET}"
fi
echo ""
