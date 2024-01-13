package io.antmedia.rest;

import junit.framework.TestCase;
import org.junit.Test;

public class PrometheusMetricTest extends TestCase {

    @Test
    public void testSimpleFormatting() {
        assertEquals(
                "HELP antmediaserver_foo Number of foos\n" +
                "TYPE antmediaserver_foo counter\n" +
                "antmediaserver_foo 1",

                PrometheusMetric.builder()
                .name("foo")
                .value(1).type(PrometheusMetric.MetricType.COUNTER)
                .help("Number of foos").build().toString());
    }

    @Test
    public void testDecimalsAndCounter() {
        assertEquals(
                "HELP antmediaserver_foo Number of foos\n" +
                        "TYPE antmediaserver_foo gauge\n" +
                        "antmediaserver_foo 3.1416",

                PrometheusMetric.builder()
                        .name("foo")
                        .value(3.141592)
                        .help("Number of foos").build().toString());
    }

}