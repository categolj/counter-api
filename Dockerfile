# Build stage
FROM rust:1.78 as builder

# Set the working directory
WORKDIR /usr/src/app

# Install diesel_cli
RUN cargo install diesel_cli --no-default-features --features postgres

# Copy the source code
COPY . .

# Build the project
RUN cargo build --release

# Runtime stage
FROM ubuntu:jammy

# Install necessary packages
RUN apt-get update && apt-get install -y \
    libpq-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /usr/src/app

# Copy the binary from the build stage
COPY --from=builder /usr/src/app/target/release/counter_api .

# Copy the migrations and other necessary files
COPY --from=builder /usr/src/app/migrations ./migrations
COPY --from=builder /usr/local/cargo/bin/diesel /usr/local/bin/diesel
COPY entrypoint.sh .

# Expose the port the app runs on
EXPOSE 7777

# Set the entrypoint
ENTRYPOINT ["./entrypoint.sh"]

# Run the binary
CMD ["./counter_api"]

