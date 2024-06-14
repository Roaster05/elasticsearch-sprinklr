import time
from datetime import datetime, timedelta
from elasticsearch import Elasticsearch
import re

# Elasticsearch cluster connection
es = Elasticsearch(["http://localhost:9200"])

def get_indices():
    indices = es.cat.indices(format="json", h="index")
    return indices

def update_indices_settings(index_names):
    settings = {
        "index": {
            "partial_search_allowed": True
        }
    }
    es.indices.put_settings(index=','.join(index_names), body=settings)
    print(f"Updated settings for indices: {', '.join(index_names)}")

def extract_date_from_index_name(index_name):
    # Adjust regex to find the date and time pattern anywhere in the index name
    match = re.search(r'(\d{8}_\d{4})', index_name)
    if match:
        date_str = match.group(1)
        return datetime.strptime(date_str, "%Y%m%d_%H%M")
    return None

def check_and_update_indices():
    indices = get_indices()
    current_time = datetime.utcnow()

    indices_to_update = []

    for index in indices:
        index_name = index['index']

        # Ignore indices that start with "."
        if index_name.startswith('.'):
            continue

        creation_date = extract_date_from_index_name(index_name)

        if creation_date and (current_time - creation_date) > timedelta(minutes=5):
            indices_to_update.append(index_name)

    if indices_to_update:
        update_indices_settings(indices_to_update)

if __name__ == "__main__":
    while True:
        check_and_update_indices()
        time.sleep(60)  # Sleep for 1 minute
