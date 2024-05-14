#!/bin/sh

# Exit immediately if a command exits with a non-zero status.
set -e

# Run database migrations
echo "Running database migrations..."
diesel migration run

# Start the application
echo "Starting the application..."
exec "$@"

