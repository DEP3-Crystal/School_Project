package infrastructure.postgres.sinker;

import model.FileUtils;
import model.Session;
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


public class SessionDataSinker {
    public static final String CSV_HEADER = "session_id,department_id,title,description,type,difficulty_level,keywords,start_time,end_time";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "session";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<Session> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/session_table.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseSessionDataFn()));

        data.apply(JdbcIO.<Session>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?, ?, ?, ?, ?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<Session>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getSessionId());
                    preparedStatement.setInt(2, element.getDepartmentId());
                    preparedStatement.setString(3, element.getTitle());
                    preparedStatement.setString(4, element.getDescription());
                    preparedStatement.setString(5, element.getDifficultyLevel());
                    preparedStatement.setString(6, element.getKeywords());
                    preparedStatement.setString(7, element.getType());
                    preparedStatement.setTimestamp(8, element.getStartTime());
                    preparedStatement.setTimestamp(9, element.getEndTime());
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

    public static class ParseSessionDataFn extends DoFn<String, Session> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<Session> out) {
            String[] data = line.split(",");

            Session session = new Session(Integer.parseInt(data[0]), Integer.parseInt(data[1]), data[2], data[3], data[4],
                    data[5], data[6], java.sql.Timestamp.valueOf(data[7]), java.sql.Timestamp.valueOf(data[8]));

            out.output(session);
        }
    }
}
