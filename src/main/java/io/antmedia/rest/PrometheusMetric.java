package io.antmedia.rest;

import lombok.Data;

@Data
public class PrometheusMetric {

    public static final String PREFIX = "antmediaserver_";

    private String name;

    private double value;

    public PrometheusMetric(String name, long totalHLSViewer) {
        this.name = name;
        this.value = totalHLSViewer;
    }

    public String toString() {
        return String.format(PREFIX + "%s %.3f", name, value);
    }
}
