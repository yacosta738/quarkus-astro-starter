package com.acosta.quarkusastro.web.rest

import io.micrometer.core.instrument.*
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.search.Search
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


private const val HTTP_SERVER_REQUESTS = "http.server.requests"

private const val JVM_GC_PAUSE = "jvm.gc.pause"

/**
 *
 * MetricsEndpoint class.
 */
@Path("/management")
@Produces(MediaType.APPLICATION_JSON)
class MetricsEndpoint
/**
 *
 * Constructor for MetricsEndpoint.
 *
 * @param meterRegistry a [io.micrometer.core.instrument.MeterRegistry] object.
 */ @Inject constructor(private val meterRegistry: MeterRegistry) {
    private val log = LoggerFactory.getLogger(
        MetricsEndpoint::class.java
    )

    /**
     * GET /management/jhi-metrics
     *
     *
     * Give metrics displayed on Metrics page
     *
     * @return a Map with a String defining a category of metrics as Key and
     * another Map containing metrics related to this category as Value
     */
    @GET
    @Path("/metrics")
    fun allMetrics(): Map<String, Map<*, *>> {
        log.debug("REST request to get all Metrics")
        val results: MutableMap<String, Map<*, *>> = HashMap()
        // JVM stats
        results["jvm"] = jvmMemoryMetrics()
        // HTTP requests stats
        results[HTTP_SERVER_REQUESTS] = httpRequestsMetrics()
        // Service stats
        results["services"] = serviceMetrics()
        // Garbage collector
        results["garbageCollector"] = garbageCollectorMetrics()
        // Process stats
        results["processMetrics"] = processMetrics()
        return results
    }

    @get:Path("/threaddump")
    @get:GET
    val threadDump: Map<String, List<ThreadInfo>>
        get() {
            log.debug("REST request to get Thread dump")
            val threads =
                listOf(*ManagementFactory.getThreadMXBean().dumpAllThreads(true, true))
            return java.util.Map.of("threads", threads)
        }

    private fun processMetrics(): Map<String, Number> {
        log.debug("REST request to get Process metrics")
        val resultsProcess: MutableMap<String, Number> = HashMap()
        val gauges = Search.`in`(meterRegistry).name { s: String ->
            s.contains("cpu") || s.contains(
                "system"
            ) || s.contains("process")
        }.gauges()
        gauges.forEach(Consumer { gauge: Gauge ->
            resultsProcess[gauge.id.name] = gauge.value()
        })
        val timeGauges = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                "process"
            )
        }.timeGauges()
        timeGauges.forEach(Consumer { gauge: TimeGauge ->
            resultsProcess[gauge.id.name] = gauge.value(TimeUnit.MILLISECONDS)
        })
        return resultsProcess
    }

    private fun garbageCollectorMetrics(): Map<String, Any> {
        log.debug("REST request to get Garbage Collector metrics")
        val resultsGarbageCollector: MutableMap<String, Any> = HashMap()
        val timers = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                JVM_GC_PAUSE
            )
        }.timers()
        timers.forEach(Consumer { timer: Timer ->
            val key = timer.id.name
            val gcPauseResults =
                HashMap<String, Number>()
            gcPauseResults["count"] = timer.count()
            gcPauseResults["max"] = timer.max(TimeUnit.MILLISECONDS)
            gcPauseResults["totalTime"] = timer.totalTime(TimeUnit.MILLISECONDS)
            gcPauseResults["mean"] = timer.mean(TimeUnit.MILLISECONDS)
            val percentiles =
                timer.takeSnapshot().percentileValues()
            for (percentile in percentiles) {
                gcPauseResults[percentile.percentile().toString()] =
                    percentile.value(TimeUnit.MILLISECONDS)
            }
            resultsGarbageCollector.putIfAbsent(key, gcPauseResults)
        })
        resultsGarbageCollector.putIfAbsent(JVM_GC_PAUSE, HashMap<Any, Any>())
        var gauges = Search.`in`(meterRegistry).name { s: String ->
            s.contains("jvm.gc") && !s.contains(
                JVM_GC_PAUSE
            )
        }.gauges()
        gauges.forEach(Consumer { gauge: Gauge ->
            resultsGarbageCollector[gauge.id.name] = gauge.value()
        })
        val counters = Search.`in`(meterRegistry).name { s: String ->
            s.contains("jvm.gc") && !s.contains(
                JVM_GC_PAUSE
            )
        }.counters()
        counters.forEach(Consumer { counter: Counter ->
            resultsGarbageCollector[counter.id.name] = counter.count()
        })
        gauges = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                "jvm.classes.loaded"
            )
        }.gauges()
        val classesLoaded = gauges.stream().map { obj: Gauge -> obj.value() }
            .reduce { x: Double, y: Double -> x + y }.orElse(0.0)
        resultsGarbageCollector["classesLoaded"] = classesLoaded
        val functionCounters = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                "jvm.classes.unloaded"
            )
        }.functionCounters()
        val classesUnloaded = functionCounters.stream().map { obj: FunctionCounter -> obj.count() }
            .reduce { x: Double, y: Double -> x + y }.orElse(0.0)
        resultsGarbageCollector["classesUnloaded"] = classesUnloaded
        return resultsGarbageCollector
    }

    private fun serviceMetrics(): Map<String, Map<*, *>> {
        log.debug("REST request to get Service metrics")
        val crudOperation: Collection<String> = listOf("GET", "POST", "PUT", "DELETE")
        val uris: MutableSet<String> = HashSet()
        val timers = meterRegistry.find(HTTP_SERVER_REQUESTS).timers()
        timers.forEach(Consumer { timer: Timer ->
            timer.id.getTag("uri")?.let {
                uris.add(
                    it
                )
            }
        })
        val resultsHttpPerUri: MutableMap<String, Map<*, *>> = HashMap()
        uris.forEach(Consumer { uri: String ->
            val resultsPerUri: MutableMap<String, Map<*, *>> =
                HashMap()
            crudOperation.forEach(Consumer { operation: String ->
                val resultsPerUriPerCrudOperation: MutableMap<String, Number> =
                    HashMap()
                val httpTimersStream =
                    meterRegistry.find(HTTP_SERVER_REQUESTS)
                        .tags("uri", uri, "method", operation).timers()
                val count = httpTimersStream.stream()
                    .map { obj: Timer -> obj.count() }
                    .reduce { x: Long, y: Long -> x + y }
                    .orElse(0L)
                if (count != 0L) {
                    val max = httpTimersMax(httpTimersStream)
                    val totalTime = httpTimersTotalTime(httpTimersStream)
                    resultsPerUriPerCrudOperation["count"] = count
                    resultsPerUriPerCrudOperation["max"] = max
                    resultsPerUriPerCrudOperation["mean"] = totalTime / count
                    resultsPerUri[operation] = resultsPerUriPerCrudOperation
                }
            })
            resultsHttpPerUri[uri] = resultsPerUri
        })
        return resultsHttpPerUri
    }

    private fun jvmMemoryMetrics(): Map<String, MutableMap<String, Number>> {
        log.debug("REST request to get JVM Memory metrics")
        val resultsJvm: MutableMap<String, MutableMap<String, Number>> = HashMap()
        val jvmUsedSearch = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                "jvm.memory.used"
            )
        }
        var gauges = jvmUsedSearch.gauges()
        gauges.forEach(Consumer { gauge: Gauge ->
            val key = gauge.id.getTag("id")
            if (key != null) {
                resultsJvm.putIfAbsent(key, HashMap())
                resultsJvm[key]!!["used"] = gauge.value()
            }
        })
        val jvmMaxSearch = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                "jvm.memory.max"
            )
        }
        gauges = jvmMaxSearch.gauges()
        gauges.forEach(Consumer { gauge: Gauge ->
            val key = gauge.id.getTag("id")
            if (key != null) {
                resultsJvm[key.toString()]!!["max"] = gauge.value()
            }
        })
        gauges = Search.`in`(meterRegistry).name { s: String ->
            s.contains(
                "jvm.memory.committed"
            )
        }.gauges()
        gauges.forEach(Consumer { gauge: Gauge ->
            val key = gauge.id.getTag("id")
            if (key != null) {
                resultsJvm[key.toString()]!!["committed"] = gauge.value()
            }
        })
        return resultsJvm
    }

    private fun httpRequestsMetrics(): Map<String, Map<*, *>> {
        log.debug("REST request to get HTTP Requests metrics")
        val statusCode: MutableSet<String> = HashSet()
        var timers = meterRegistry.find(HTTP_SERVER_REQUESTS).timers()
        timers.forEach(Consumer { timer: Timer ->
            timer.id.getTag("status")?.let {
                statusCode.add(
                    it
                )
            }
        })
        val resultsHttp: MutableMap<String, Map<*, *>> = HashMap()
        val resultsHttpPerCode: MutableMap<String, Map<String, Number>> = HashMap()
        statusCode.forEach(Consumer { code: String ->
            val resultsPerCode: MutableMap<String, Number> =
                HashMap()
            val httpTimersStream =
                meterRegistry.find(HTTP_SERVER_REQUESTS).tag("status", code).timers()
            val count = httpTimersStream.stream()
                .map { obj: Timer -> obj.count() }
                .reduce { x: Long, y: Long -> x + y }
                .orElse(0L)
            val max = httpTimersMax(httpTimersStream)
            val totalTime = httpTimersTotalTime(httpTimersStream)
            resultsPerCode["count"] = count
            resultsPerCode["max"] = max
            resultsPerCode["mean"] = if (count != 0L) totalTime / count else 0
            resultsHttpPerCode[code] = resultsPerCode
        })
        resultsHttp["percode"] = resultsHttpPerCode
        timers = meterRegistry.find(HTTP_SERVER_REQUESTS).timers()
        val countAllrequests = timers.stream().map { obj: Timer -> obj.count() }
            .reduce { x: Long, y: Long -> x + y }.orElse(0L)
        val resultsHTTPAll: MutableMap<String, Number> = HashMap()
        resultsHTTPAll["count"] = countAllrequests
        resultsHttp["all"] = resultsHTTPAll
        return resultsHttp
    }

    private fun httpTimersTotalTime(httpTimersStream: MutableCollection<Timer>): Double {
        val totalTime = httpTimersStream.stream()
            .map { x: Timer ->
                x.totalTime(
                    TimeUnit.MILLISECONDS
                )
            }.reduce { x: Double, y: Double -> x + y }
            .orElse(0.0)
        return totalTime
    }

    private fun httpTimersMax(httpTimersStream: MutableCollection<Timer>): Double {
        val max = httpTimersStream.stream()
            .map { x: Timer ->
                x.max(
                    TimeUnit.MILLISECONDS
                )
            }.reduce { x: Double, y: Double -> if (x > y) x else y }
            .orElse(0.0)
        return max
    }

    companion object {
        /** Constant `MISSING_NAME_TAG_MESSAGE="Missing name tag for metric {}"`  */
        const val MISSING_NAME_TAG_MESSAGE = "Missing name tag for metric {}"
    }
}
