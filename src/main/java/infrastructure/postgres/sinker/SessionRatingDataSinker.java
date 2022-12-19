package infrastructure.postgres.sinker;

import model.FileUtils;
import model.SessionRating;
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

public class SessionRatingDataSinker {

    public static final String CSV_HEADER = "session_id,user_id,rating";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "session_rating";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<SessionRating> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/session_rating.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseSessionRatingDataFn()));

        data.apply(JdbcIO.<SessionRating>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<SessionRating>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getSessionId());
                    preparedStatement.setInt(2, element.getUserId());
                    preparedStatement.setInt(3, element.getRating());
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

    public static class ParseSessionRatingDataFn extends DoFn<String, SessionRating> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<SessionRating> out) {
            String[] data = line.split(",");

            SessionRating sessionRating = new SessionRating(Integer.parseInt(data[0]), Integer.parseInt(data[1])
                    , Integer.parseInt(data[2]));

            out.output(sessionRating);
        }
    }
}
