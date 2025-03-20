# Observability Stack
- Grafana: Visualization
- Prometheus: Metrics
- Loki: Logs
- OpenTelemetry Collector: Data collection
- Tempo: Traces (not setup on the dashboard, but the Tempo datasource is queryable via Grafana - Explore) 

# Deployment Steps
Folder structure should look like this when unzipped:
```
/
banking/
├── src/
│   └── ...
├── docker-compose.yml <-- 
└── ...
monitoring/
├── grafana-provisioning/
│   ├── dashboards/
│   │   └── Spring Boot Observability.json <-- Important!
│   └── datasources/
│       └── datasources.yml
└── simulate_banking_activity.sh
```

1. `cd banking`
2. `docker compose up -d`
3. Navigate to `http://localhost:3000/dashboards`
Note: I wasn't able to get the dashboard to automatically detect the Prometheus and Loki data sources so you'll need to import and select where as they appear as defaults.
  - New
  - Import
  - Upload dashboard JSON file
  - Select Spring Boot Observability.json
  - Update Name and UID
  - Select data sources (both should appear as default and the only option)
4. Generate some user activity to populate logs/traces 
- `cd ../monitoring`
- `bash simulate_banking_activity.sh`
- Alternatively, manually perform actions on `http://localhost:8080/ssr/login`