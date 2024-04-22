FROM python:3.9

WORKDIR /
COPY producer.py /
COPY requirements.txt /

RUN pip install --no-cache-dir -r requirements.txt
CMD ["python","producer.py"]
