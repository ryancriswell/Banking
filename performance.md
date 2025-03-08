# Optimization Report
- Refactored the backend to use the transactions table as the source of truth for user balance
- Use BIGINT in the database for storing transaction amounts
- Database connection pooling with Hikari
- Background job processing for transaction writes. They now begin in the pending status and are asyncronously updated to completed / failed.
- Added index to the database for optimizing the user balance calculation (benchmark estimated from custom prometheus metric, not a specific benchmark library integration)
- Script created to simulate user many concurrent api calls (monitoring/banking_load_test.sh)
- Unit/Integration tests rewritten for the refactor and expanded
- Exisiting rate limiting still in place

# Benchmarks
After the refactor to sum the transactions to calculate user balance this was the KPI I wanted to keep in check. 

I created a prometheus metric called banking.balance.get_cents and looked at the average time for that call.

## Results
A decent improvement on the order of 1/3 to 1/2 the processing time saved after indexing as seen in the test-results images.

The dashboard has exposed the ssr/register endpoint as the next thing that should be addressed. The load testing script has 100 users registering concurrently so there's multiple avenues we take to improve that given more time.