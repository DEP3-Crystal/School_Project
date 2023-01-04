package emails.sender;

import model.emails.Subscriber;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import utils.FileUtils;

import java.io.IOException;
import java.util.Properties;

public class ChangesInTheScheduleEmailSender {

    public static void main(String[] args) throws IOException {

        Properties properties = FileUtils.loadProperties("database.properties");
        Properties emailProperties = FileUtils.loadProperties("email.properties");

        String driver = properties.getProperty("PGDRIVER");
        String hostName = properties.getProperty("PGHOSTNAME");
        String username = properties.getProperty("PGUSERNAME");
        String password = properties.getProperty("PGPASSWORD");

        String emailHostName = emailProperties.getProperty("HOSTNAME");
        String smtpPort = emailProperties.getProperty("SMTPPORT");
        String emailUsername = emailProperties.getProperty("USERNAME");
        String emailPassword = emailProperties.getProperty("PASSWORD");

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<Subscriber> subscribers = pipeline.apply(
                JdbcIO.<Subscriber>read()
                        .withDataSourceConfiguration(
                                JdbcIO.DataSourceConfiguration.create(
                                                driver, hostName)
                                        .withUsername(username)
                                        .withPassword(password))
                        .withQuery("SELECT first_name, last_name, email FROM student_registration r " +
                                "INNER JOIN users u ON u.user_id = r.user_id WHERE r.session_id == {{sessionId}}")
                        .withRowMapper((JdbcIO.RowMapper<Subscriber>) resultSet -> {
                            Subscriber subscriber = new Subscriber();
                            subscriber.setFirstName(resultSet.getString("first_name"));
                            subscriber.setLastName(resultSet.getString("last_name"));
                            subscriber.setEmail(resultSet.getString("email"));
                            return subscriber;
                        })
                        .withCoder(SerializableCoder.of(Subscriber.class)));

        String htmlTemplate = "Dear %s %s," +
                "<br><br> We hope this email finds you well. We wanted to let you know about a change in the schedule of the student" +
                "registration. We apologize for any inconvenience this may cause and appreciate your understanding. If you have any " +
                "question or concerns, please don't hesitate to reach out." +
                "<br><br>Best regards";

        PCollection<Void> sendEmail = subscribers.apply(
                ParDo.of(new DoFn<Subscriber, Void>() {
                    @ProcessElement
                    public void processElement(ProcessContext context) throws EmailException {
                        Subscriber subscriber = context.element();

                        HtmlEmail email = new HtmlEmail();
                        email.setHostName(emailHostName);
                        email.setSmtpPort(Integer.parseInt(smtpPort));
                        email.setAuthenticator(new DefaultAuthenticator(emailUsername, emailPassword));
                        email.setSSLOnConnect(true);
                        email.setFrom(emailUsername);
                        email.addTo(subscriber.getEmail());
                        email.setSubject("Changes in the schedule");
                        email.setHtmlMsg(String.format(htmlTemplate, subscriber.getFirstName(), subscriber.getLastName()));
                        email.send();
                    }
                })
        );

        pipeline.run().waitUntilFinish();
    }
}
