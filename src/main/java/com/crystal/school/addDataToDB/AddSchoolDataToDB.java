package com.crystal.school.addDataToDB;

import com.crystal.school.model.School;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

public class AddSchoolDataToDB {
    public static final String CSV_HEADER = "school_id,location,name";

    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);
        String postgresDriver = "org.postgresql.Driver";
        String hostname = "jdbc:postgresql://localhost:5432/school";
        String tableName = "school";

        PCollection<School> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/school_table.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseSchoolDataFn()));

        data.apply(JdbcIO.<School>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(postgresDriver, hostname)
                        .withUsername("postgres")
                        .withPassword(System.getenv("PGPASSWORD")))
                .withStatement(String.format("insert into %s values(?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<School>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getSchoolId());
                    preparedStatement.setString(2, element.getLocation());
                    preparedStatement.setString(3, element.getName());
                }));

        pipeline.run().waitUntilFinish();

        System.out.println("Finished!");
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

    public static class ParseSchoolDataFn extends DoFn<String, School> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<School> out) {
            String[] data = line.split(",");

            School school = new School(Integer.parseInt(data[0]), data[1], data[2]);

            out.output(school);
        }
    }
}
