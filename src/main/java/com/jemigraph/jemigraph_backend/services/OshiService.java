package com.jemigraph.jemigraph_backend.services;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

@Service
public class OshiService {

	private final SystemInfo systemInfo = new SystemInfo();
	private final Instant bootTime = Instant.now().minusMillis(ManagementFactory.getRuntimeMXBean().getUptime());

	public Map<String, Object> getSystemMetrics() {
		HardwareAbstractionLayer hardware = systemInfo.getHardware();
		OperatingSystem os = systemInfo.getOperatingSystem();

		Map<String, Object> metrics = new HashMap<>();

		// 1. CPU Metrics & Per-Core Loads
		CentralProcessor processor = hardware.getProcessor();
		long[] prevTicks = processor.getSystemCpuLoadTicks();

		try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

		// Per-core load calculations
		double[] cpuLoadPerProcessor = processor.getProcessorCpuLoadBetweenTicks(processor.getProcessorCpuLoadTicks());
		List<Double> perCoreUsage = new ArrayList<>();
		for (double load : cpuLoadPerProcessor) {
			perCoreUsage.add(Math.round(load * 10000.0) / 100.0);
		}

		Map<String, Object> cpuInfo = new HashMap<>();
		cpuInfo.put("name", processor.getProcessorIdentifier().getName());
		cpuInfo.put("cores", processor.getPhysicalProcessorCount());
		cpuInfo.put("threads", processor.getLogicalProcessorCount());
		cpuInfo.put("usagePercentage", Math.round(cpuLoad * 100.0) / 100.0);
		cpuInfo.put("perCoreUsage", perCoreUsage);
		cpuInfo.put("maxFreqMHz", processor.getMaxFreq() / 1_000_000);
		metrics.put("cpu", cpuInfo);

		// 2. RAM (Memory) Metrics
		GlobalMemory memory = hardware.getMemory();
		long totalMemory = memory.getTotal();
		long availableMemory = memory.getAvailable();
		long usedMemory = totalMemory - availableMemory;
		long totalSwap = memory.getVirtualMemory().getSwapTotal();
		long usedSwap = memory.getVirtualMemory().getSwapUsed();

		Map<String, Object> ramInfo = new HashMap<>();
		ramInfo.put("totalGB", totalMemory / (1024.0 * 1024 * 1024));
		ramInfo.put("usedGB", usedMemory / (1024.0 * 1024 * 1024));
		ramInfo.put("availableGB", availableMemory / (1024.0 * 1024 * 1024));
		ramInfo.put("swapTotalGB", totalSwap / (1024.0 * 1024 * 1024));
		ramInfo.put("swapUsedGB", usedSwap / (1024.0 * 1024 * 1024));
		metrics.put("ram", ramInfo);

		// 3. Storage (Disk File Stores) Metrics
		List<Map<String, Object>> storageList = new ArrayList<>();
		List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
		for (OSFileStore fs : fileStores) {
			long totalSpace = fs.getTotalSpace();
			long usableSpace = fs.getUsableSpace();
			long freeSpace = fs.getFreeSpace();
			long usedSpace = totalSpace - freeSpace;

			Map<String, Object> disk = new HashMap<>();
			disk.put("name", fs.getName());
			disk.put("mount", fs.getMount());
			disk.put("description", fs.getDescription());
			disk.put("type", fs.getType());
			disk.put("totalGB", totalSpace / (1024.0 * 1024 * 1024));
			disk.put("usedGB", usedSpace / (1024.0 * 1024 * 1024));
			disk.put("freeGB", usableSpace / (1024.0 * 1024 * 1024));

			if (totalSpace > 0) {
				double usagePercent = ((double) usedSpace / totalSpace) * 100;
				disk.put("usagePercentage", Math.round(usagePercent * 100.0) / 100.0);
			} else {
				disk.put("usagePercentage", 0.0);
			}

			storageList.add(disk);
		}
		metrics.put("storage", storageList);

		// 4. Network Interfaces Metrics
		List<Map<String, Object>> networkList = new ArrayList<>();
		List<NetworkIF> networkInterfaces = hardware.getNetworkIFs();
		for (NetworkIF net : networkInterfaces) {
			net.updateAttributes();
			Map<String, Object> netInfo = new HashMap<>();
			netInfo.put("name", net.getName());
			netInfo.put("displayName", net.getDisplayName());
			netInfo.put("ipv4", net.getIPv4addr());
			netInfo.put("bytesSent", net.getBytesSent());
			netInfo.put("bytesRecv", net.getBytesRecv());
			netInfo.put("speedBps", net.getSpeed());
			netInfo.put("isOperational", net.isConnectorPresent());
			networkList.add(netInfo);
		}
		metrics.put("network", networkList);

		// 5. Operating System & Host Metadata
		Map<String, Object> osInfo = new HashMap<>();
		osInfo.put("family", os.getFamily());
		osInfo.put("manufacturer", os.getManufacturer());
		osInfo.put("version", os.getVersionInfo().getVersion());
		osInfo.put("processCount", os.getProcessCount());
		osInfo.put("threadCount", os.getThreadCount());
		osInfo.put("uptimeSeconds", os.getSystemUptime());

		try {
			osInfo.put("hostName", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			osInfo.put("hostName", "Unknown");
		}
		metrics.put("os", osInfo);

		return metrics;
	}
}