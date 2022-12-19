package infrastructure.postgres.sinker;

import model.FileUtils;
import model.SessionRegistration;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import java.io.IOException;
import java.util.Properties;

public class SessionRegistrationDataSinker {

    public static final String CSV_HEADER = "room_id,session_id,start_time,end_time";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "session_registration";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<SessionRegistration> data = pipeline.apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/session_registration.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseSessionRegistrationDataFn()));

        data.apply(JdbcIO.<SessionRegistration>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<SessionRegistration>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getSessionId());
                    preparedStatement.setInt(2, element.getRoomId());
                    preparedStatement.setTimestamp(3, element.getStartTime());
                    preparedStatement.setTimestamp(4, element.getEndTime());
                }));

        pipeline.run().waitUntilFinish();
    }

    public static class FilterHeaderFn extends DoFn<String, String> {

        private final String header;

        public FilterHeaderFn(String header) {
            this.header = header;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            String row = c.element();

            if (!row.isEmpty() && !row.equals(this.header)) {
                c.output(row);
            }
        }
    }

    public static class ParseSessionRegistrationDataFn extends DoFn<String, SessionRegistration> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<SessionRegistration> out) {
            String[] data = line.split(",");

            SessionRegistration sessionRegistration = new SessionRegistration(Integer.parseInt(data[0]),
                    Integer.parseInt(data[1]), java.sql.Timestamp.valueOf(data[2]), java.sql.Timestamp.valueOf(data[3]));

            out.output(sessionRegistration);
        }
    }
}
