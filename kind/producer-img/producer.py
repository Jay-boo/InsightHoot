from confluent_kafka import Producer
import logging
import socket
import feedparser
import time

 
def fetch_rss_and_publish(conf:dict,rss_url:str,kafka_topic:str):
    producer = Producer(conf)
    while True:
        feed = feedparser.parse(rss_url)
        print(f'Length of entries {feed.keys()}')
        for entry in feed.entries:
            entry_title = entry.title
            entry_link = entry.link
            entry_published = entry.published
            message_payload = {
                "title": entry_title,
                "link": entry_link,
                "published": entry_published
            }
            
            try: 
                producer.produce(kafka_topic, key="message", value=str(message_payload))
                producer.flush()
                logging.info(f"Published RSS entry to Kafka: {entry_title}" )
            except Exception as e:
                logging.error(f"Failed to publish RSS entry to Kafka: {str(e)}" )

        # time.sleep()

            

if __name__ == "__main__":
    rss_url = 'https://cloud-computing.developpez.com/rss.php'
    logging.basicConfig(format='%(asctime)s -  %(levelname)s : %(message)s', level=logging.DEBUG)
    logging.info('Starting RSS to Kafka script...')
    conf = {
        "bootstrap.servers": "kafka-service:9092",
        "client.id": socket.gethostname()
    }
    kafka_topic="topic_from_producer"
    fetch_rss_and_publish(conf,rss_url,kafka_topic)
    
