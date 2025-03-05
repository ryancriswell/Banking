# Banking Frontend

## How to Run
1. Start Backend/DB (see main README for detailed instructions)
2. `cd frontend`
3. `npm install`
4. `npm start`
5. Navigate to http://localhost:3000/register if browser didn't automatically open the page. 
6. Register a new account and login. (not mocked, using real auth via JWT provided by backend).

## Libraries
- React
- Material UI

## Limitations
- I've yet to define a contract between frontend and backend request/response objects beyond the openapi.yml documentation. It'd be better if it was type enforced on the frontend via Zod or something.
- It could be a lot prettier, even for banking standards. I spent a lot of time today cleaning up and refactoring the backend for functionality and stability. 