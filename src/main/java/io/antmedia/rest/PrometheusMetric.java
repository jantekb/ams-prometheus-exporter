package io.antmedia.rest;

import lombok.Builder;
import lombok.Data;

import java.text.DecimalFormat;

@Data
@Builder
public class PrometheusMetric {

    public static final String PREFIX = "antmediaserver_";

    private final String name;

    private final double value;

    private final String help;

    @Builder.Default
    private MetricType type = MetricType.GAUGE;

    private final DecimalFormat df = new DecimalFormat("#.####");

    public String toString() {
        return String.format(
                "HELP " + PREFIX + "%1$s %2$s\n" +
                "TYPE " + PREFIX + "%1$s %3$s\n" +
                PREFIX + "%1$s %4$s",
                name, // 1
                help, // 2
                type.name().toLowerCase(), // 3
                df.format(value).replace(',', '.')); // 4

    }

    public enum MetricType { GAUGE, COUNTER }
}
