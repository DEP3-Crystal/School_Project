package infrastructure.postgres.sinker;

import utils.FileUtils;
import model.Room;
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


public class RoomDataSinker {

    public static final String CSV_HEADER = "room_id,school_id,floor,door_number,type";

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");
        String tableName = "room";

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<Room> data = pipeline
                .apply("Read data from csv file", TextIO.read().from("src/main/resources/input_data/room_table.csv"))
                .apply(ParDo.of(new FilterHeaderFn(CSV_HEADER)))
                .apply(ParDo.of(new ParseRoomDataFn()));


        data.apply(JdbcIO.<Room>write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                        .create(driver, hostName)
                        .withUsername(username)
                        .withPassword(password))
                .withStatement(String.format("insert into %s values(?, ?, ?, ?, ?)", tableName))
                .withPreparedStatementSetter((JdbcIO.PreparedStatementSetter<Room>) (element, preparedStatement) -> {

                    preparedStatement.setInt(1, element.getRoomId());
                    preparedStatement.setInt(2, element.getSchoolId());
                    preparedStatement.setInt(3, element.getFloor());
                    preparedStatement.setInt(4, element.getDoorNumber());
                    preparedStatement.setString(5, element.getType());
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

    public static class ParseRoomDataFn extends DoFn<String, Room> {

        @ProcessElement
        public void processElement(@Element String line, OutputReceiver<Room> out) {
            String[] data = line.split(",");

            Room room = new Room(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]), data[4]);

            out.output(room);
        }
    }
}
