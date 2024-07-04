import subprocess
import json
import time

# A  script which continuously fires a search query whose execution time will exceed 3000ms and will
#  be blacklisted after executing for 5 iterations and will get stopped the moment a blacklist exception occurs
search_query = {
    "size": 786,
    "query": {
        "script": {
            "script": {
                "lang": "painless",
                "source": """
                    def result = 0;
                    for (int z = 0; z < 69; z++) {
                        for (int y = 0; y <40; y++) {
                            result -= z + y;
                        }
                    }
                """
            }
        }
    }
}

# Define function to execute Elasticsearch query using curl
def execute_query(port):
    # Define the curl command
    curl_command = f"curl -XGET http://localhost:{port}/my_index/_search -H 'Content-Type: application/json' -d '{json.dumps(search_query)}'"

    # Execute the curl command
    try:
        response = subprocess.check_output(curl_command, shell=True)
        print(f"Successfully executed query on port {port}. Response: {response.decode('utf-8')}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"Exception occurred on port {port}: {e}")
        return False

# Main loop
port = 9200
succeed_count = 0

while port <= 9200:  # Limit querying to ports 9200 and 9201
    if execute_query(port):
        succeed_count += 1
    else:
        print(f"Total successful counts on port {port}: {succeed_count}")
        succeed_count = 0
        port += 1
    time.sleep(1)  # Wait for 1 second before executing the next query

