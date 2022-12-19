package infrastructure.postgres.sinker;

import model.FileUtils;
import model.User;
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

public class UserDataSinker {
    public static final String CSV_HEADER = "user_id,first_name,last_name,email,gender,biography,password,department_id,is_employee";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "users";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<User> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/users.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseUserDataFn()));

        data.apply(JdbcIO.<User>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?, ?, ?, ?, ?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<User>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getUserId());
                    preparedStatement.setString(2, element.getFirstName());
                    preparedStatement.setString(3, element.getLastName());
                    preparedStatement.setString(4, element.getEmail());
                    preparedStatement.setString(5, element.getGender());
                    preparedStatement.setString(6, element.getBiography());
                    preparedStatement.setString(7, element.getPassword());
                    preparedStatement.setInt(8, element.getDepartmentId());
                    preparedStatement.setBoolean(9, element.isEmployee());
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

    public static class ParseUserDataFn extends DoFn<String, User> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<User> out) {
            String[] data = line.split(",");

            User user = new User(Integer.parseInt(data[0]), data[1], data[2], data[3], data[4], data[5], data[6],
                    Integer.parseInt(data[7]), Boolean.valueOf(data[8]));

            out.output(user);
        }
    }
}
