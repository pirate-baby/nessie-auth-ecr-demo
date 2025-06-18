FROM python:3.12-slim
WORKDIR /app
RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*
RUN pip3 install --no-cache-dir \
    pyarrow \
    pyiceberg \
    "locknessie[microsoft]" \
    s3fs \
    boto3

COPY el_script.py .
CMD ["python", "el_script.py"]
