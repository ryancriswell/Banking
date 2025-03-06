# How To Run
- `docker compose up`
- `cd banking`
- `./mvnw spring-boot:run`
- http://localhost:8080/ssr/login

## Technologies
- **Thymeleaf**: Java Spring server-side template engine

## SSR vs SPA frontend versions
Both versions have the same functionality, but different UIs. The SPA uses Material UI and the SSR version uses pure CSS. Given more time I'd look into including Tailwind and DaisyUI to make it more visually appealing and clean up the CSS.

If both versions are running, you can switch between the two by changing the path from `ssr` to `spa` or vice versa and you'll be forwarded to the matching port automatically. 

### SSR
- SSR urls start with http://localhost:8080/ssr.
- Started from /banking (detailed start up instructions are in `/frontend/README.md`)
- SSR templates can be found in `/banking/src/main/resources/templates/ssr`

### SPA:
- SPA urls start with http://localhost:3000/spa.
- Started from /frontend (detailed start up instructions are in `/frontend/README.md`)

### Viable Paths (Shared):
- /login
- /register
- /dashboard
- /transactions
- /transfer

## Troubleshooting
#### ERR_CONNECTION_REFUSED
There is some logic hard-coded with these port numbers, so make sure the app is actually running on the expected ports. Sometimes it'll automatically choose a new port if you have something running on it already when starting the app.



