# Use Python 3.9 as base image
FROM python:3.9-slim

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install Python packages
RUN pip3 install --no-cache-dir \
    pyarrow \
    pyiceberg \
    "locknessie[microsoft]" \
    s3fs \
    boto3

# Copy the script
COPY el_script.py .

# Run the script
CMD ["python", "el_script.py"]
