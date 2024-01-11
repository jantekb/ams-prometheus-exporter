# Prometheus Exporter Plugin

This plugin exposes Ant Media Server metrics in Prometheus format,
allowing standard scraping of the data.

# Building and Installation

  ```sh
  mvn install  -Dgpg.skip=true
  ```

Copy the jar file to under the `plugins` folder of your Ant Media Server installation:

   $ cp target/prometheus-exporter-plugin.jar /usr/local/antmedia/plugins

Restart Ant Media Server to pick up the new plugin:

   $ sudo systemctl restart antmedia.service

Test the metrics endpoint:

```sh
   $ curl http://localhost:5080/LiveApp/rest/prometheus-exporter/metrics
    antmediaserver_hls_viewer_count 0.000
    antmediaserver_dash_viewer_count 0.000
    antmediaserver_active_broadcast_count 0.000
    antmediaserver_total_broadcast_count 1.000
    antmediaserver_cpu_load 0.000
```

# See Data in Prometheus

A sample `prometheus.yml` file is included in this repository with minimal scraping config:

```sh
   $ docker run \
      -p 9090:9090 \
      -v prometheus.yml:/etc/prometheus/prometheus.yml \
      prom/prometheus
```
