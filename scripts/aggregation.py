from elasticsearch import Elasticsearch
import random
import time

# Connect to Elasticsearch
es = Elasticsearch(["http://localhost:9200"])  # Node-1 is running at :9200
                                               # Node-2 is running at :9201

# This is a script which basically fires only aggregation queries and will cause circuit breaking exceptions
# leading to blacklisting of queries occupying maximum space

heavy_agg_queries = [
    {
        "size": 0,
        "aggs": {
            "date_histogram_agg_with_big_arrays": {
                "date_histogram": {
                    "field": "timestamp",
                    "calendar_interval": "1d"
                },
                "aggs": {
                    "big_array_agg": {
                        "scripted_metric": {
                            "init_script": "state.big_array = new ArrayList();",
                            "map_script": "state.big_array.add(doc['price'].value * doc['quantity'].value);",
                            "combine_script": "double total = 0; for (t in state.big_array) { total += t } ",
                            "reduce_script": "double total = 0; for (a in states) { if (a != null) { total += a } }"
                        }
                    }
                }
            }
        }
    }

]

# Function to execute random heavy aggregation queries infinitely
def execute_random_heavy_agg_queries():
    iteration = 1
    while True:
        try:
            print(f"Iteration: {iteration}")
            # Select a random query from heavy_agg_queries
            random_query = random.choice(heavy_agg_queries)
            result = es.search(index="my_index", body=random_query)
            print(f"Query executed successfully")
        except Exception as e:
            error_message = str(e)
            if "Data too large" in error_message:
                print("A circuit Breaking Exception has trigerred")
            if "blacklist" in error_message:
                print("Query has been blacklisted")
                break


        iteration += 1

# Run the function to execute random heavy aggregation queries infinitely
execute_random_heavy_agg_queries()
