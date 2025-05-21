package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Measurement;
import com.ftn.uns.ac.rs.smarthome.models.dtos.*;
import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import okhttp3.OkHttpClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class InfluxService {
    private final InfluxDBClient influxDbClient;

    public InfluxService(InfluxDBClient influxDbClient, Environment env) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://" + env.getProperty("influxdb.host") + ":" + env.getProperty("influxdb.port"))
                .authenticateToken(env.getProperty("influxdb.token").toCharArray())
                .okHttpClient(builder)
                .bucket(env.getProperty("influxdb.bucket"))
                .org(env.getProperty("influxdb.organization"))
                .build();
        this.influxDbClient = InfluxDBClientFactory.create(options);
        this.bucket = env.getProperty("influxdb.bucket");
    }
    private final String bucket;

    public void save(String name, String value, Date timestamp, Map<String, String> tags) {
        WriteApiBlocking writeApi = this.influxDbClient.getWriteApiBlocking();
        Point point = Point.measurement(name)
                .addTags(tags)
                .addField("value", value)
                .time(timestamp.toInstant(), WritePrecision.MS);
        writeApi.writePoint(point);
    }

    public void save(String name, float value, Date timestamp, Map<String, String> tags) {
    WriteApiBlocking writeApi = this.influxDbClient.getWriteApiBlocking();
    Point point = Point.measurement(name)
            .addTags(tags)
            .addField("value", value)
            .time(timestamp.toInstant(), WritePrecision.MS);
        writeApi.writePoint(point);
    }

    public void save(String name, double value, Date timestamp, Map<String, String> tags) {
        WriteApiBlocking writeApi = this.influxDbClient.getWriteApiBlocking();
        Point point = Point.measurement(name)
                .addTags(tags)
                .addField("value", value)
                .time(timestamp.toInstant(), WritePrecision.MS);
        writeApi.writePoint(point);
    }


    private MeasurementsDTO query(String fluxQuery, int maxData) {
        List<Measurement> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Map<String,String> optTags = new HashMap<>();
                if(fluxRecord.getValueByKey("unit") != null)
                    optTags.put("unit", fluxRecord.getValueByKey("unit").toString());
                result.add(new Measurement(fluxRecord.getMeasurement(),
                        fluxRecord.getValue() == null ? 0 : ((double) fluxRecord.getValue()),
                        fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime()),
                        optTags));
            }
        }

        boolean hasMore = false;
        if(result.size() > maxData) {
            hasMore = true;
            result.remove(result.size() - 1);
        }
        return new MeasurementsDTO(result,hasMore);
    }

    private List<CommandSummaryInternal> queryCommands(String fluxQuery) {
        List<CommandSummaryInternal> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Map<String,String> optTags = new HashMap<>();
                optTags.put("userId",fluxRecord.getValueByKey("userId").toString());
                if(fluxRecord.getValueByKey("from") != null)
                    optTags.put("from", fluxRecord.getValueByKey("from").toString());
                if(fluxRecord.getValueByKey("mode") != null)
                    optTags.put("mode", fluxRecord.getValueByKey("mode").toString());
                if(fluxRecord.getValueByKey("temp") != null)
                    optTags.put("temp", fluxRecord.getValueByKey("temp").toString());
                if(fluxRecord.getValueByKey("centrifuge") != null)
                    optTags.put("centrifuge", fluxRecord.getValueByKey("centrifuge").toString());
                if(fluxRecord.getValueByKey("to") != null)
                    optTags.put("to", fluxRecord.getValueByKey("to").toString());
                if(fluxRecord.getValueByKey("everyDay") != null)
                    optTags.put("everyDay", fluxRecord.getValueByKey("everyDay").toString());
                if(fluxRecord.getValueByKey("isHealth") != null)
                    optTags.put("isHealth", fluxRecord.getValueByKey("isHealth").toString());
                if(fluxRecord.getValueByKey("isFungus") != null)
                    optTags.put("isFungus", fluxRecord.getValueByKey("isFungus").toString());
                if(fluxRecord.getValueByKey("target") != null)
                    optTags.put("target", fluxRecord.getValueByKey("target").toString());
                if(fluxRecord.getValueByKey("fanSpeed") != null)
                    optTags.put("fanSpeed", fluxRecord.getValueByKey("fanSpeed").toString());
                if(fluxRecord.getValueByKey("carCapacity") != null)
                    optTags.put("carCapacity", fluxRecord.getValueByKey("carCapacity").toString());
                if(fluxRecord.getValueByKey("carCharge") != null)
                    optTags.put("carCharge", fluxRecord.getValueByKey("carCharge").toString());
                if(fluxRecord.getValueByKey("portNum") != null)
                    optTags.put("portNum", fluxRecord.getValueByKey("portNum").toString());
                if(fluxRecord.getValueByKey("spentEnergy") != null)
                    optTags.put("spentEnergy", fluxRecord.getValueByKey("spentEnergy").toString());
                result.add(new CommandSummaryInternal(
                        fluxRecord.getValue() == null ? "" : fluxRecord.getValue().toString(),
                        fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime()),
                        optTags));
            }
        }
        result.sort(Comparator.comparing(CommandSummaryInternal::getTimestamp).reversed());
        return result;
    }

    private List<Integer> queryUniqueUsersPerDevice(String fluxQuery) {
        List<Integer> results = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                results.add(fluxRecord.getValue() == null ? 0 : Integer.parseInt(fluxRecord.getValue().toString()));
            }
        }
        return results;
    }

    public MeasurementsDTO findPaginatedByMeasurementNameAndDeviceIdInTimeRange(MeasurementsStreamRequestDTO requestDTO) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "  |> range(start: %d, stop: %d) " +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "  |> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "  |> filter(fn: (r) => r[\"deviceId\"] == \"%s\")" +
                        "  |> group() " +
                        "  |> limit(n: %d, offset: %d) ",
                this.bucket,requestDTO.getFrom(),
                requestDTO.getTo(),
                requestDTO.getMeasurementName(),
                requestDTO.getDeviceId().toString(),
                requestDTO.getLimit() + 1,
                requestDTO.getOffset());
        return this.query(fluxQuery,requestDTO.getLimit());
    }

    public MeasurementsDTO findPowerAggregation(PowerMeasurementsStreamRequestDTO requestDTO) {
        StringBuilder fluxQuery = new StringBuilder(String.format(
                "from(bucket: \"%s\") " +
                        "  |> range(start: %d, stop: %d) " +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \"%s\")",
                this.bucket,
                requestDTO.getFrom(),
                requestDTO.getTo(),
                requestDTO.getMeasurementName()));
        fluxQuery.append("  |> filter(fn: (r) => contains(value: r[\"deviceId\"] , set: [");
        for (int i = 0; i < requestDTO.getDeviceIds().size(); i++) {
            fluxQuery.append("\"").append(requestDTO.getDeviceIds().get(i)).append("\"");
            if(i != requestDTO.getDeviceIds().size() - 1)
                fluxQuery.append(",");
        }
        fluxQuery.append("]))");
        fluxQuery.append("  |> aggregateWindow(every: 15s, fn: sum, createEmpty: false)");
        fluxQuery.append("  |> group(columns: [\"_time\"])");
        fluxQuery.append("  |> sum()");
        return this.query(fluxQuery.toString(), requestDTO.getLimit());
    }

    public List<CommandSummaryInternal> findPaginatedByTimeSpanAndUserIdAndDeviceId(CommandsRequestDTO request) {
        String fluxQuery = String.format("from(bucket: \"%s\") " +
                "  |> range(start: %d, stop: %d) " +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"states\") " +
                "  |> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                "  |> filter(fn: (r) => r[\"deviceId\"] == \"%s\") ",
                this.bucket, request.getFrom(), request.getTo(), request.getDeviceId());
        if(request.getUserId() != -1) {
            fluxQuery += String.format("|> filter(fn: (r) => r[\"userId\"] == \"%s\")", request.getUserId());
        }
        fluxQuery += String.format("|> group() " +
                "|> sort(columns: [\"_time\"], desc: true) " +
                "|> limit(n: %d, offset: %d) "
                                         ,request.getSize(), request.getPage() * request.getSize());
        return this.queryCommands(fluxQuery);
    }

    public List<Integer> findAllDistinctUsersForAllRecords(Integer deviceId) {
        String fluxQuery = String.format("from(bucket: \"%s\") "  +
                                            " |> range(start: 0, stop: 9007199254740991) " +
                                            " |> filter(fn: (r) => r[\"_measurement\"] == \"states\") "  +
                                            " |> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                                            " |> filter(fn: (r) => r[\"deviceId\"] == \"%s\") " +
                                            " |> group(columns: [\"userId\"]) " +
                                            " |> distinct(column: \"userId\") " +
                                            " |> keep(columns: [\"_value\"]) ",
                            this.bucket,deviceId.toString());
        return this.queryUniqueUsersPerDevice(fluxQuery);
    }
}
