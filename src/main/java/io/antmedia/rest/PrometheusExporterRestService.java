package io.antmedia.rest;

import com.google.common.base.Joiner;
import io.antmedia.SystemUtils;
import io.antmedia.datastore.db.DataStore;
import io.antmedia.datastore.db.DataStoreFactory;
import io.antmedia.statistic.DashViewerStats;
import io.antmedia.statistic.GPUUtils;
import io.antmedia.statistic.HlsViewerStats;
import io.antmedia.statistic.IStatsCollector;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;

import jakarta.ws.rs.core.UriInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedList;
import java.util.List;

@Component
@Path("/prometheus-exporter")
public class PrometheusExporterRestService {

	@Context
	protected ServletContext servletContext;

	@GET
	@Path("/")
	@Produces("text/html")
	public String welcomePage(@Context UriInfo uriInfo) {
		return "<html>\n" +
               "  <head><title>Ant Media Server Prometheus Metrics Exporter</title></head>\n" +
               "  <body>\n" +
               "    <h1>Ant Media Server Prometheus Metrics Exporter</h1>\n" +
               "      <p>You can find the actual metrics under <a href=\"" + uriInfo.getPath() + "/metrics\">/metrics</a><p>\n" +
               "	  <p>For more information about this exporter, please visit\n" +
               "	  <a href=\"https://github.com/jantekb/ams-prometheus-exporter\">https://github.com/jantekb/ams-prometheus-exporter</a>\n" +
               "  </body>\n" +
               "</html>\n";
	}

	@GET
	@Path("/version")
	@Produces("text/plain")
	public String getVersion() {
		return "1.0.1";
	}

	@GET
	@Path("/metrics")
	@Produces("text/plain; version=0.0.4")
	public String getData() {
		ApplicationContext appCtx = (ApplicationContext) servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		DataStoreFactory dataStoreFactory = (DataStoreFactory) appCtx.getBean("dataStoreFactory");
		final DataStore dataStore = dataStoreFactory.getDataStore();

		long activeBroadcastCount = dataStore.getActiveBroadcastCount();
		long totalNumberOfBroadcasts = dataStore.getTotalBroadcastNumber();
		long totalHLSViewer, totalDASHViewer;

		List<PrometheusMetric> metrics = new LinkedList<>();

		if (appCtx.containsBean(HlsViewerStats.BEAN_NAME)) {
			HlsViewerStats hlsViewerStats = (HlsViewerStats) appCtx.getBean(HlsViewerStats.BEAN_NAME);
			totalHLSViewer = hlsViewerStats.getTotalViewerCount();
			metrics.add(
					PrometheusMetric.builder().name("hls_viewer_total").
							help("The number of HLS players").value(totalHLSViewer).build());
		}

		if (appCtx.containsBean(DashViewerStats.BEAN_NAME)) {
			DashViewerStats dashViewerStats = (DashViewerStats) appCtx.getBean(DashViewerStats.BEAN_NAME);
			totalDASHViewer = dashViewerStats.getTotalViewerCount();
			metrics.add(PrometheusMetric.builder().name("dash_viewer_total")
					.help("The number for DASH players").value(totalDASHViewer).build());
		}

		metrics.add(PrometheusMetric.builder().name("active_broadcast_total")
				.help("The number of ongoing live broadcasts")
				.value(activeBroadcastCount).build());

		metrics.add(PrometheusMetric.builder().name("total_broadcast_total")
				.help("The number of broadcasts, including the ones that are not active")
				.value(totalNumberOfBroadcasts).build());

		IStatsCollector monitor = (IStatsCollector) appCtx.getBean(IStatsCollector.BEAN_NAME);
		metrics.add(PrometheusMetric.builder().name("cpu_usage_ratio")
				.help("CPU usage").value(monitor.getCpuLoad()).build());

		metrics.add(PrometheusMetric.builder().name("total_memory_bytes")
				.help("Total amount of memory in the JVM").value(Runtime.getRuntime().totalMemory()).build());
		metrics.add(PrometheusMetric.builder().name("free_memory_bytes")
				.help("Free memory in the JVM").value(Runtime.getRuntime().freeMemory()).build());
		metrics.add(PrometheusMetric.builder().name("used_memory_bytes")
				.help("Used memory in the JVM").value(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).build());

		metrics.add(PrometheusMetric.builder().name("available_processors_total")
						.type(PrometheusMetric.MetricType.COUNTER)
						.help("The number of available processors to the JVM")
				.value(Runtime.getRuntime().availableProcessors()).build());

		metrics.add(PrometheusMetric.builder().name("usable_disk_space_bytes")
				.help("Usable disk space in bytes").value(SystemUtils.osHDUsableSpace(null)).build());
		metrics.add(PrometheusMetric.builder().name("total_disk_space_bytes")
				.help("Total disk space in bytes").value(SystemUtils.osHDTotalSpace(null)).build());
		metrics.add(PrometheusMetric.builder().name("free_disk_space_bytes")
				.help("Free disk space in bytes").value(SystemUtils.osHDFreeSpace(null)).build());
		metrics.add(PrometheusMetric.builder().name("used_disk_space_bytes")
				.help("Used disk space in bytes").value(SystemUtils.osHDInUseSpace(null)).build());

		final GPUUtils gpuUtils = GPUUtils.getInstance();
		int deviceCount = gpuUtils.getDeviceCount();

		if (deviceCount > 0) {
			for (int i=0; i < deviceCount; i++) {
				metrics.add(PrometheusMetric.builder().name("gpu_" + i + "_utilization_ratio")
						.help("The GPU utilization ratio").value(gpuUtils.getGPUUtilization(i)).build());
				metrics.add(PrometheusMetric.builder().name("gpu_" + i + "_memory_utilization_ratio")
						.help("The GPU memory utilization ratio").value(gpuUtils.getMemoryUtilization(i)).build());
				GPUUtils.MemoryStatus memoryStatus = gpuUtils.getMemoryStatus(i);
				metrics.add(PrometheusMetric.builder().name("gpu_" + i + "_memory_total_bytes")
						.help("The total memory of the GPU in bytes").value(memoryStatus.getMemoryTotal()).build());
				metrics.add(PrometheusMetric.builder().name("gpu_" + i + "_memory_free_bytes")
						.help("The free memory of the GPU in bytes").value(memoryStatus.getMemoryFree()).build());
				metrics.add(PrometheusMetric.builder().name("gpu_" + i + "_memory_used_bytes")
						.help("The used memory by the GPU in bytes").value(memoryStatus.getMemoryUsed()).build());
			}
		}

		return Joiner.on('\n').join(metrics) + '\n';
	}

}
