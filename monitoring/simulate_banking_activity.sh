#!/bin/bash

set -e
BASE_URL="http://localhost:8080/ssr"
COOKIE_JAR_1="user1_cookies.txt"
COOKIE_JAR_2="user2_cookies.txt"

echo "==== Banking Application User Simulation ===="
echo "This script will simulate user interactions to generate traces"

# Clean up any existing cookie files
rm -f $COOKIE_JAR_1 $COOKIE_JAR_2

# Function to simulate delay between actions
simulate_delay() {
  sleep 1
}

echo "----- Registering User 1 -----"
curl -s -L -c $COOKIE_JAR_1 -X POST "$BASE_URL/register" \
  -d "username=alice&password=Password123!&email=alice@example.com" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 1 (alice) registered"

simulate_delay

echo "----- Registering User 2 -----"
curl -s -L -c $COOKIE_JAR_2 -X POST "$BASE_URL/register" \
  -d "username=bob&password=Password123!&email=bob@example.com" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 2 (bob) registered"

simulate_delay

# Login as User 1 (Alice)
echo "----- Logging in as User 1 (Alice) -----"
curl -s -L -c $COOKIE_JAR_1 -b $COOKIE_JAR_1 -X POST "$BASE_URL/login" \
  -d "username=alice&password=Password123!" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 1 logged in"

simulate_delay

# Check dashboard
echo "----- Checking User 1 Dashboard -----"
curl -s -L -b $COOKIE_JAR_1 "$BASE_URL/dashboard" > /dev/null
echo "User 1 viewed dashboard"

simulate_delay

# Login as User 2 (Bob)
echo "----- Logging in as User 2 (Bob) -----"
curl -s -L -c $COOKIE_JAR_2 -b $COOKIE_JAR_2 -X POST "$BASE_URL/login" \
  -d "username=bob&password=Password123!" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 2 logged in"

simulate_delay

# Check dashboard for User 2
echo "----- Checking User 2 Dashboard -----"
curl -s -L -b $COOKIE_JAR_2 "$BASE_URL/dashboard" > /dev/null
echo "User 2 viewed dashboard"

simulate_delay

# User 1 transfers money to User 2
echo "----- User 1 transferring money to User 2 -----"
curl -s -L -b $COOKIE_JAR_1 -c $COOKIE_JAR_1 -X POST "$BASE_URL/transfer" \
  -d "recipientUsername=bob&amount=50.00" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 1 transferred \$50 to User 2"

simulate_delay

# User 2 checks transactions
echo "----- User 2 checking transactions -----"
curl -s -L -b $COOKIE_JAR_2 "$BASE_URL/transactions" > /dev/null
echo "User 2 checked transaction history"

simulate_delay

# User 2 transfers money back to User 1
echo "----- User 2 transferring money to User 1 -----"
curl -s -L -b $COOKIE_JAR_2 -c $COOKIE_JAR_2 -X POST "$BASE_URL/transfer" \
  -d "recipientUsername=alice&amount=25.00" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 2 transferred \$25 to User 1"

simulate_delay

# User 1 checks transactions with pagination
echo "----- User 1 checking paginated transactions -----"
curl -s -L -b $COOKIE_JAR_1 "$BASE_URL/transactions?page=0&size=5" > /dev/null
echo "User 1 checked first page of transactions"

simulate_delay

# User 1 logs out
echo "----- User 1 logging out -----"
curl -s -L -b $COOKIE_JAR_1 -c $COOKIE_JAR_1 "$BASE_URL/logout" > /dev/null
echo "User 1 logged out"

simulate_delay

# User 1 logs back in
echo "----- User 1 logging back in -----"
curl -s -L -c $COOKIE_JAR_1 -b $COOKIE_JAR_1 -X POST "$BASE_URL/login" \
  -d "username=alice&password=Password123!" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 1 logged back in"

simulate_delay

# User 1 tries another transfer
echo "----- User 1 making another transfer -----"
curl -s -L -b $COOKIE_JAR_1 -c $COOKIE_JAR_1 -X POST "$BASE_URL/transfer" \
  -d "recipientUsername=bob&amount=15.00" \
  -H "Content-Type: application/x-www-form-urlencoded" > /dev/null
echo "User 1 transferred \$15 to User 2"

simulate_delay

# User 2 logs out
echo "----- User 2 logging out -----"
curl -s -L -b $COOKIE_JAR_2 -c $COOKIE_JAR_2 "$BASE_URL/logout" > /dev/null
echo "User 2 logged out"

simulate_delay

# Clean up cookie files
rm -f $COOKIE_JAR_1 $COOKIE_JAR_2

echo "==== Simulation completed ===="
