import random
import time
from elasticsearch import Elasticsearch

# Connect to Elasticsearch
es = Elasticsearch(["http://localhost:9200"])  # Change the URL as per your Elasticsearch configuration

## A script which will fire Non Aggregation based queries of diffrent types and will STOP the moment it recieves a
# CIRCUIT BREAKING EXCEPTION

# 1. Search Query
search_query = {
    "query": {
        "match_all": {}
    }
}

# 2. Index Document
index_doc = {
    "title": "Sample Document",
    "content": "This is a sample document for testing."
}

# 3. Get Document by ID
get_doc_id = "1"

# 4. Delete Document by ID
delete_doc_id = "1"

# 5. Update Document by ID
update_doc = {
    "doc": {
        "title": "Updated Document",
        "content": "This document has been updated."
    }
}

# 6. Bulk API
bulk_data = [
    {"index": {"_index": "my_index", "_id": "1"}},
    {"title": "Bulk Document 1", "content": "Bulk content 1"},
    {"index": {"_index": "my_index", "_id": "2"}},
    {"title": "Bulk Document 2", "content": "Bulk content 2"}
]

# 7. Term Query
term_query = {
    "query": {
        "term": {
            "title": "Sample"
        }
    }
}

# 8. Match Query
match_query = {
    "query": {
        "match": {
            "content": "test"
        }
    }
}

# 9. Multi Match Query
multi_match_query = {
    "query": {
        "multi_match": {
            "query": "Sample",
            "fields": ["title", "content"]
        }
    }
}

# 10. Range Query
range_query = {
    "query": {
        "range": {
            "timestamp": {
                "gte": "now-1d/d",
                "lt": "now/d"
            }
        }
    }
}

# 11. Transport Info Query
transport_info_query = {}

# List of all queries to randomize
queries = [
    ("search", search_query),
    ("index", index_doc),
    ("get", get_doc_id),
    ("delete", delete_doc_id),
    ("update", update_doc),
    ("bulk", bulk_data),
    ("term", term_query),
    ("match", match_query),
    ("multi_match", multi_match_query),
    ("range", range_query),
]

def execute_random_query():
    query_type, query = random.choice(queries)
    try:
        if query_type == "index":
            es.index(index="my_index", body=query)
            print("Index Document success")
        elif query_type == "get":
            es.get(index="my_index", id=query)
            print("Get Document success")
        elif query_type == "bulk":
            es.bulk(body=query)
            print("Bulk API success")
        elif query_type == "term":
            es.search(index="my_index", body=query)
            print("Term Query success")
        elif query_type == "match":
            es.search(index="my_index", body=query)
            print("Match Query success")
        elif query_type == "multi_match":
            es.search(index="my_index", body=query)
            print("Multi Match Query success")
        elif query_type == "range":
            es.search(index="my_index", body=query)
            print("Range Query success")
    except Exception as e:
        error_message = str(e)
        print(f"{query_type.capitalize()} Query error: {error_message}")
        if "Data too large" in error_message:
            print("Stopping script due to 'Circuit Breaking Exception' error.")
            return False
    return True

# Execute Queries in an Infinite Loop
print("\nExecuting Search-based and Other Queries...")
while True:
    if not execute_random_query():
        break
    #time.sleep(1)  # Adding a delay to avoid overwhelming the server
