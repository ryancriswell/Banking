#!/bin/bash

set -e
BASE_URL="http://localhost:8080/ssr"
CONCURRENCY=100
NUM_ITERATIONS=100
VERBOSE=false

# Parse command line arguments
while getopts "c:n:u:v" opt; do
  case ${opt} in
    c )
      CONCURRENCY=$OPTARG
      ;;
    n )
      NUM_ITERATIONS=$OPTARG
      ;;
    u )
      BASE_URL=$OPTARG
      ;;
    v )
      VERBOSE=true
      ;;
    \? )
      echo "Usage: $0 [-c concurrency] [-n iterations] [-u base_url] [-v]"
      exit 1
      ;;
  esac
done

log() {
  if [[ "$VERBOSE" = true ]]; then
    echo "$@"
  fi
}

extract_cookies() {
  local response=$1
  # Extract cookies from Set-Cookie headers in response
  grep -i "Set-Cookie:" <<< "$response" | sed 's/Set-Cookie: //i' | tr -d '\r'
}

echo "==== Banking Application Load Test ===="
echo "Concurrency: $CONCURRENCY"
echo "Iterations per user: $NUM_ITERATIONS"
echo "Base URL: $BASE_URL"

# Initialize a temporary directory for storing cookies in memory
COOKIE_MEM_DIR=$(mktemp -d)
trap 'rm -rf "$COOKIE_MEM_DIR"' EXIT

# Register users in parallel
register_users() {
  local num_users=$1
  echo "Registering $num_users users..."
  
  for i in $(seq 1 $num_users); do
    local username="user$i"
    
    log "Registering $username"
    local response=$(curl -s -L -i -X POST "$BASE_URL/register" \
      -d "username=$username&password=Password123!&email=$username@example.com" \
      -H "Content-Type: application/x-www-form-urlencoded")
    
    # Store cookies in memory (using files in RAM)
    echo "$(extract_cookies "$response")" > "$COOKIE_MEM_DIR/$i.cookie"
  done
  
  echo "All users registered successfully"
}

# Function to run a user session
run_user_session() {
  local user_id=$1
  local username="user$user_id"
  local recipient="user$(( (user_id % CONCURRENCY) + 1 ))"
  local cookie_path="$COOKIE_MEM_DIR/$user_id.cookie"
  
  # Login
  log "[$username] Logging in"
  local cookie_header="$(cat "$cookie_path")"
  local response=$(curl -s -L -i -X POST "$BASE_URL/login" \
    -H "Cookie: $cookie_header" \
    -d "username=$username&password=Password123!" \
    -H "Content-Type: application/x-www-form-urlencoded")
  echo "$(extract_cookies "$response")" > "$cookie_path"
    
  # View dashboard
  log "[$username] Viewing dashboard"
  cookie_header="$(cat "$cookie_path")"
  response=$(curl -s -L -i -H "Cookie: $cookie_header" "$BASE_URL/dashboard")
  echo "$(extract_cookies "$response")" > "$cookie_path"
  
  # Transfer money
  log "[$username] Transferring money to $recipient"
  cookie_header="$(cat "$cookie_path")"
  response=$(curl -s -L -i -X POST "$BASE_URL/transfer" \
    -H "Cookie: $cookie_header" \
    -d "recipientUsername=$recipient&amount=50.00" \
    -H "Content-Type: application/x-www-form-urlencoded")
  echo "$(extract_cookies "$response")" > "$cookie_path"
  
  # Check transactions
  log "[$username] Checking transactions"
  cookie_header="$(cat "$cookie_path")"
  response=$(curl -s -L -i -H "Cookie: $cookie_header" "$BASE_URL/transactions")
  echo "$(extract_cookies "$response")" > "$cookie_path"
  
  # Check paginated transactions
  log "[$username] Checking paginated transactions"
  cookie_header="$(cat "$cookie_path")"
  response=$(curl -s -L -i -H "Cookie: $cookie_header" "$BASE_URL/transactions?page=0&size=5")
  echo "$(extract_cookies "$response")" > "$cookie_path"
  
  # Another transfer
  log "[$username] Making another transfer"
  cookie_header="$(cat "$cookie_path")"
  response=$(curl -s -L -i -X POST "$BASE_URL/transfer" \
    -H "Cookie: $cookie_header" \
    -d "recipientUsername=$recipient&amount=25.00" \
    -H "Content-Type: application/x-www-form-urlencoded")
  echo "$(extract_cookies "$response")" > "$cookie_path"
  
  # Logout
  log "[$username] Logging out"
  cookie_header="$(cat "$cookie_path")"
  response=$(curl -s -L -i -H "Cookie: $cookie_header" "$BASE_URL/logout")
  echo "$(extract_cookies "$response")" > "$cookie_path"
}

# Register users first
register_users $CONCURRENCY

# Run load test with progress indicator
echo "Starting load test with $CONCURRENCY concurrent users..."
start_time=$(date +%s)

export -f run_user_session log extract_cookies
export BASE_URL VERBOSE CONCURRENCY COOKIE_MEM_DIR

# Use GNU Parallel to run concurrent sessions if available
if command -v parallel &>/dev/null; then
  seq 1 $CONCURRENCY | parallel -j $CONCURRENCY --progress \
    'for i in $(seq 1 '"$NUM_ITERATIONS"'); do run_user_session {}; done'
else
  # Fallback if GNU Parallel is not available
  echo "GNU Parallel not found. Running with reduced concurrency using background processes..."
  for user_id in $(seq 1 $CONCURRENCY); do
    (
      for i in $(seq 1 $NUM_ITERATIONS); do 
        run_user_session $user_id
        echo -n "."
      done
    ) &
    
    # Limit number of background processes
    if (( user_id % 5 == 0 )); then
      wait
    fi
  done
  wait
  echo ""
fi

end_time=$(date +%s)
duration=$((end_time - start_time))
total_requests=$((CONCURRENCY * NUM_ITERATIONS * 7))  # 7 API calls per iteration

echo "==== Load Test Completed ===="
echo "Duration: $duration seconds"
echo "Total requests: $total_requests"
echo "Requests per second: $(bc <<< "scale=2; $total_requests / $duration")"
