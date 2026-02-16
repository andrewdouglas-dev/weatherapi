# Weather API

A simple, containerized REST API built in Java that provides current weather information for a given US zip code. It uses Redis for caching to reduce external API calls and improve response times.

## Features

- Fetches weather data by US zip code
- Caches responses in Redis (with TTL) to minimize external API requests
- Built with Java + Maven
- Fully Dockerized (single Dockerfile for the app + Docker Compose for app + Redis)
- Environment variable support (via `.env` for API keys, etc.)

## Tech Stack

- **Language & Build Tool**: Java 17+ (Maven)
- **Web Server**: Built-in `com.sun.net.httpserver` (lightweight HTTP server)
- **Redis Client**: Jedis (for caching)
- **Environment Management**: dotenv-java
- **Containerization**: Docker + Docker Compose
- **External Weather Data**: Visual Crossing (free!) (configure API Key via `.env`)

## Prerequisites

- Docker & Docker Compose (recommended way to run)
- Or: Java 17+, Maven (for local development)
