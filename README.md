# Prometheus Exporter Plugin for Ant Media Server

This plugin exposes Ant Media Server metrics in Prometheus format,
allowing standard scraping of the data.

This plugin requires Ant Media Server version 2.8.0.

# Building and Installation

  ```sh
  mvn install
  ```

Copy the jar file to under the `plugins` folder of your Ant Media Server installation:

    $ cp target/prometheus-exporter-plugin.jar /usr/local/antmedia/plugins

Restart Ant Media Server to pick up the new plugin:

    $ sudo systemctl restart antmedia.service

Test the metrics endpoint:

```sh
   $ curl http://localhost:5080/LiveApp/rest/prometheus-exporter/metrics
HELP antmediaserver_hls_viewer_total The number of HLS players
TYPE antmediaserver_hls_viewer_total gauge
antmediaserver_hls_viewer_total 0
HELP antmediaserver_dash_viewer_total The number for DASH players
TYPE antmediaserver_dash_viewer_total gauge
antmediaserver_dash_viewer_total 0
[...]
```

# See Data in Prometheus

A sample `prometheus.yml` file is included in this repository with minimal scraping config:

```sh
   $ docker run \
      -p 9090:9090 \
      -v prometheus.yml:/etc/prometheus/prometheus.yml \
      prom/prometheus
```

# List of supported metrics

| Metric name                                   | Type    | Help                                                             |
|-----------------------------------------------|---------|------------------------------------------------------------------|
| antmediaserver_hls_viewer_total               | gauge   | The number of HLS players                                        |
| antmediaserver_dash_viewer_total              | gauge   | The number for DASH players                                      |
| antmediaserver_active_broadcast_total         | gauge   | The number of ongoing live broadcasts                            |
| antmediaserver_total_broadcast_total          | gauge   | The number of broadcasts, including the ones that are not active |
| antmediaserver_cpu_usage_ratio                | gauge   | CPU usage                                                        |
| antmediaserver_total_memory_byte              | gauge   | Total amount of memory in the JVM                                |
| antmediaserver_free_memory_bytes              | gauge   | Free memory in the JVM                                           |
| antmediaserver_used_memory_bytes              | gauge   | Used memory in the JVM                                           |
| antmediaserver_available_processors_total     | counter | The number of available processors to the JVM                    |
| antmediaserver_usable_disk_space_bytes        | gauge   | Usable disk space in bytes                                       |
| antmediaserver_free_disk_space_bytes          | gauge   | Free disk space in bytes                                         |
| antmediaserver_total_disk_space_bytes         | gauge   | Total disk space in bytes                                        |
| antmediaserver_gpu_1_utilization_ratio        | gauge   | The 1st GPU utilization ratio                                    |
| antmediaserver_gpu_1_memory_utilization_ratio | gauge   | The 1st GPU total memory utilization ratio                       |
| antmediaserver_gpu_1_memory_total_bytes       | gauge   | The 1st GPU total memory in bytes                                |
| antmediaserver_gpu_1_memory_free_bytes        | gauge   | The 1st GPU free memory in bytes                                 |
| antmediaserver_gpu_1_memory_used_bytes        | gauge   | The 1st GPU used memory in bytes                                 |
