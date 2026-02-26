# RESTful Weather Data Proxy API with Redis Caching and Rate Limiting

A lightweight, production-inspired REST API proxy built **entirely in pure Java** that fetches and serves real-time and historical weather data from Visual Crossing.

Key capabilities include intelligent Redis caching, fixed-window rate limiting per IP, robust path parsing, graceful degradation (Redis unavailable → direct API fallback), and complete Dockerized deployment.

Developed as a deliberate step to strengthen backend engineering skills by building a resilient, realistic microservice from first principles.

## Features

- **RESTful endpoint** for weather data by US zip code (supports current + historical timelines)
- **Redis caching** with TTL (5 minutes) to reduce external API calls and improve latency
- **Fixed-window rate limiting** per client IP (configurable via env vars) stored in Redis
- **Custom HTTP server** using Java's built-in `com.sun.net.httpserver` (no frameworks)
- **Robust path parsing** with validation, percent-decoding, and edge-case handling
- **Graceful fallback** — seamlessly serves fresh data if Redis is unavailable
- **Environment-driven configuration** (API key, rate limits, Redis URL via `.env`)
- **Structured logging** and error handling for observability
- **Full containerization** — single Dockerfile + Docker Compose (app + Redis)

## Tech Stack

- **Language & Build Tool** — Java 17+ (Maven)
- **Web Server** — `com.sun.net.httpserver` (lightweight, zero-dependency)
- **Redis Client** — Jedis (connection pooling, fixed-window rate limiting)
- **Environment Management** — dotenv-java
- **Containerization** — Docker + Docker Compose
- **External Data** — Visual Crossing Weather API (free tier supported)

## Quick Start (Recommended: Docker)

1. **Clone the repository**
   ```bash
   git clone https://github.com/andrewdouglas-dev/weatherapi.git
   cd weatherapi

2. **Set up environment variables**
   
   Copy .env_sample to .env and add your Visual Crossing API key:
   ```bash
   WEATHER_API_KEY=your_visual_crossing_key_here
   RATE_LIMIT_MAX=100
   RATE_LIMIT_WINDOW=60   # seconds

4. **Run with Docker Compose**
   ```bash
   docker compose up --build
   ```
   
   App available at: http://localhost:8080 (or your configured port)
   
   Redis runs in a separate container (exposed on 6379 for debugging)

5. **Stop the containers**
   ```bash
   docker compose down

## Project Structure
    .
    ├── src/main/java/com/github/          # Application source
    │   ├── handlers/                      # HTTP handlers
    │   ├── utilities/                     # Helpers (path parsing, rate limiting, etc.)
    │   └── ...                            # Main entry point
    ├── src/test/java/                     # (Add tests here in future)
    ├── .env_sample                        # Example environment config
    ├── Dockerfile                         # Multi-stage build
    ├── docker-compose.yml                 # App + Redis orchestration
    ├── pom.xml                            # Maven dependencies
    └── README.md

## Future Improvements
- Add unit/integration tests (JUnit 5)
- Implement sliding-window rate limiting
- Add basic auth for admin endpoints
- Support more query params (units, language)
- Add OpenAPI/Swagger docs

## License
MIT License — feel free to use, modify, and learn from this project.
Contributions, feedback, and suggestions are welcome — open an issue or PR!
