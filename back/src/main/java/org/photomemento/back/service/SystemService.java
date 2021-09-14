package org.photomemento.back.service;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import org.photomemento.back.types.size.ByteUnit;
import org.photomemento.back.util.DateUtils;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

/**
 * Description:
 * Helper class to retrieve system related data
 */
@Service
public class SystemService {
    /**
     * Description:
     * Type of operating system
     */
    public enum OS_TYPE {
        WINDOWS,
        LINUX,
        MAC,
        OTHER
    }

    /**
     * Starting time
     */
    @Getter
    private final Instant startTime = Instant.now();

    /**
     * Running time
     */
    public String getUpTime() {
        return DateUtils.getHumanTime(Instant.now().getEpochSecond() - startTime.getEpochSecond());
    }

    /**
     * Max memory allocatable
     */
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Max memory allocatable
     */
    public String getMaxDisplayMemory() {
        return ByteUnit.toStringMostSuitableForBytes(getMaxMemory());
    }

    /**
     * Total allocated memory
     */
    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Total allocated memory
     */
    public String getTotalDisplayMemory() {
        return ByteUnit.toStringMostSuitableForBytes(getTotalMemory());
    }

    /**
     * Free memory
     */
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Free displayed memory
     */
    public String getFreeDisplayMemory() {
        return ByteUnit.toStringMostSuitableForBytes(getFreeMemory());
    }

    /**
     * Free used memory
     */
    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    /**
     * Free allocated memory
     */
    public String getUsedDisplayMemory() {
        return ByteUnit.toStringMostSuitableForBytes(getUsedMemory());
    }

    public float getProcessCpu() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return Math.round(osBean.getProcessCpuLoad() * 1000) / 10f;
    }
    public float getSystemCpu() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return Math.round(osBean.getSystemCpuLoad() * 1000) / 10f;
    }

    /**
     * Current java version
     */
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Current os architecture
     */
    public String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    /**
     * Current os name
     */
    public String getOSName() {
        return System.getProperty("os.name");
    }

    /**
     * Current os name
     */
    public String getOSVersion() {
        return System.getProperty("os.version");
    }

    /**
     * Current total active threads
     */
    public int getTotalActiveThreads() {
        return Thread.activeCount();
    }

    /**
     * All current active threads
     */
    public Map<Thread, StackTraceElement[]> getAllActiveThreads() {
        return Thread.getAllStackTraces();
    }

    /**
     * Returns true if current system is windows
     */
    public boolean isWindows() {
        return getOSName().toLowerCase().contains("win");
    }

    /**
     * Returns true if current system is linux
     */
    public boolean isLinux() {
        return getOSName().toLowerCase().contains("nux");
    }

    /**
     * Returns true if current system is Mac
     */
    public boolean isMac() {
        return getOSName().toLowerCase().contains("mac") || getOSName().toLowerCase().contains("darwin");
    }

    /**
     * Returns the type of current system
     */
    public OS_TYPE getOSType() {
        if (isWindows()) return OS_TYPE.WINDOWS;
        else if (isLinux()) return OS_TYPE.LINUX;
        else if (isMac()) return OS_TYPE.MAC;
        return OS_TYPE.OTHER;
    }
}
