package infrastructure.postgres.sinker;

import model.Employee;
import utils.FileUtils;
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

public class EmployeeDataSinker {

    public static final String CSV_HEADER = "user_id,phone_number,title,role";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "\"Employee\"";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<Employee> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/employees.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseEmployeeDataFn()));

        data.apply(JdbcIO.<Employee>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<Employee>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getUserId());
                    preparedStatement.setString(2, element.getPhoneNumber());
                    preparedStatement.setString(3, element.getTitle());
                    preparedStatement.setString(4, element.getRole());
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

    public static class ParseEmployeeDataFn extends DoFn<String, Employee> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<Employee> out) {
            String[] data = line.split(",");

            Employee employee = new Employee(Integer.parseInt(data[0]), data[1], data[2], data[3]);

            out.output(employee);
        }
    }
}
