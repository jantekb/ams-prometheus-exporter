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

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedList;
import java.util.List;

@Component
@Path("/prometheus-exporter")
public class PrometheusExporterRestService {

	@Context
	protected ServletContext servletContext;

	@GET
	@Path("/version")
	@Produces("text/plain")
	public String getVersion() {
		return "1.0.0";
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
			metrics.add(new PrometheusMetric("hls_viewer_count", totalHLSViewer));
		}

		if (appCtx.containsBean(DashViewerStats.BEAN_NAME)) {
			DashViewerStats dashViewerStats = (DashViewerStats) appCtx.getBean(DashViewerStats.BEAN_NAME);
			totalDASHViewer = dashViewerStats.getTotalViewerCount();
			metrics.add(new PrometheusMetric("dash_viewer_count", totalDASHViewer));
		}

		metrics.add(new PrometheusMetric("active_broadcast_count", activeBroadcastCount));
		metrics.add(new PrometheusMetric("total_broadcast_count", totalNumberOfBroadcasts));

		IStatsCollector monitor = (IStatsCollector) appCtx.getBean(IStatsCollector.BEAN_NAME);
		metrics.add(new PrometheusMetric("cpu_load", monitor.getCpuLoad()));

		
		metrics.add(new PrometheusMetric("total_memory_bytes", Runtime.getRuntime().totalMemory()));
		metrics.add(new PrometheusMetric("free_memory_bytes", Runtime.getRuntime().freeMemory()));
		metrics.add(new PrometheusMetric("used_memory_bytes", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

		metrics.add(new PrometheusMetric("available_processors", Runtime.getRuntime().availableProcessors()));

		metrics.add(new PrometheusMetric("usable_disk_space", SystemUtils.osHDUsableSpace(null)));
		metrics.add(new PrometheusMetric("total_disk_space", SystemUtils.osHDTotalSpace(null)));
		metrics.add(new PrometheusMetric("free_disk_space", SystemUtils.osHDFreeSpace(null)));
		metrics.add(new PrometheusMetric("used_disk_space", SystemUtils.osHDInUseSpace(null)));

		final GPUUtils gpuUtils = GPUUtils.getInstance();
		int deviceCount = gpuUtils.getDeviceCount();

		if (deviceCount > 0) {
			for (int i=0; i < deviceCount; i++) {
				metrics.add(new PrometheusMetric("gpu_" + i + "_utilization", gpuUtils.getGPUUtilization(i)));
				metrics.add(new PrometheusMetric("gpu_" + i + "_memory_utilization", gpuUtils.getMemoryUtilization(i)));
				GPUUtils.MemoryStatus memoryStatus = gpuUtils.getMemoryStatus(i);
				metrics.add(new PrometheusMetric("gpu_" + i + "_memory_total_bytes", memoryStatus.getMemoryTotal()));
				metrics.add(new PrometheusMetric("gpu_" + i + "_memory_free_bytes", memoryStatus.getMemoryFree()));
				metrics.add(new PrometheusMetric("gpu_" + i + "_memory_used_bytes", memoryStatus.getMemoryUsed()));

			}
		}

		return Joiner.on('\n').join(metrics) + '\n';
	}

}
