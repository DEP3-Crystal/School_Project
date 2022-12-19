package infrastructure.postgres.sinker;

import model.FileUtils;
import model.Teacher;
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

public class TeacherDataSinker {

    public static final String CSV_HEADER = "user_id,credentials";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "teacher";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<Teacher> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/teacher.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseTeacherDataFn()));

        data.apply(JdbcIO.<Teacher>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<Teacher>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getUserId());
                    preparedStatement.setString(2, element.getCredentials());
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

    public static class ParseTeacherDataFn extends DoFn<String, Teacher> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<Teacher> out) {
            String[] data = line.split(",");

            Teacher teacher = new Teacher(Integer.parseInt(data[0]), data[1]);

            out.output(teacher);
        }
    }
}
