package infrastructure.postgres.sinker;

import model.FileUtils;
import model.TeacherRating;
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

public class TeacherRatingDataSinker {

    public static final String CSV_HEADER = "user_id,teacher_id,rating";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "teacher_rating";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<TeacherRating> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/teacher_rating.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseTeacherRatingDataFn()));

        data.apply(JdbcIO.<TeacherRating>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<TeacherRating>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getUserId());
                    preparedStatement.setInt(2, element.getTeacherId());
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

    public static class ParseTeacherRatingDataFn extends DoFn<String, TeacherRating> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<TeacherRating> out) {
            String[] data = line.split(",");

            TeacherRating rating = new TeacherRating(Integer.parseInt(data[0]), Integer.parseInt(data[1])
                    , Integer.parseInt(data[2]));

            out.output(rating);
        }
    }
}
