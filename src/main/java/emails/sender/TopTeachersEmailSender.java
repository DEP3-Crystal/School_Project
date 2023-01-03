package emails.sender;

import model.emails.Subscriber;
import model.emails.TopTeacher;
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
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class TopTeachersEmailSender {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {

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

        PCollection<TopTeacher> topTeachers = pipeline.apply(JdbcIO.<TopTeacher>read()
                .withDataSourceConfiguration
                        (JdbcIO.DataSourceConfiguration
                                .create(driver, hostName)
                                .withUsername(username)
                                .withPassword(password))
                .withQuery("SELECT u.first_name, u.last_name, ROUND(t.rating_sum::decimal / t.rating_count, 2)"
                        + " AS rating FROM users u INNER JOIN teacher t ON u.user_id = t.user_id "
                        + "ORDER BY rating DESC LIMIT 6")
                .withRowMapper(new JdbcIO.RowMapper<TopTeacher>() {
                    @Override
                    public TopTeacher mapRow(ResultSet resultSet) throws SQLException {
                        return new TopTeacher(resultSet.getString(1),
                                resultSet.getString(2),
                                resultSet.getDouble(3));
                    }
                })
                .withCoder(SerializableCoder.of(TopTeacher.class)));

        PCollection<Subscriber> subscribers = pipeline.apply(JdbcIO.<Subscriber>read()
                .withDataSourceConfiguration(
                        JdbcIO.DataSourceConfiguration.create(
                                        driver, hostName)
                                .withUsername(username)
                                .withPassword(password))
                .withQuery("SELECT first_name, last_name, email FROM subscribers")
                .withRowMapper(new JdbcIO.RowMapper<Subscriber>() {
                    @Override
                    public Subscriber mapRow(ResultSet resultSet) throws Exception {
                        Subscriber subscriber = new Subscriber();
                        subscriber.setFirstName(resultSet.getString("first_name"));
                        subscriber.setLastName(resultSet.getString("last_name"));
                        subscriber.setEmail(resultSet.getString("email"));
                        return subscriber;
                    }
                })
                .withCoder(SerializableCoder.of(Subscriber.class)));

        topTeachers.apply(ParDo.of(new EmailSenderFn(subscribers, emailHostName, emailUsername, emailPassword, smtpPort)));

        pipeline.run().waitUntilFinish();
    }

    public static class EmailSenderFn extends DoFn<TopTeacher, Void> implements Serializable {
        private final PCollection<Subscriber> subscribers;
        private final String emailHostName;
        private final String emailUsername;
        private final String emailPassword;
        private final String smtpPort;


        public EmailSenderFn(PCollection<Subscriber> subscribers, String emailHostName, String emailUsername, String emailPassword, String smtpPort) {
            this.subscribers = subscribers;
            this.emailHostName = emailHostName;
            this.emailUsername = emailUsername;
            this.emailPassword = emailPassword;
            this.smtpPort = smtpPort;
        }

        @ProcessElement
        public void processElement(@Element TopTeacher teacher, OutputReceiver<Void> out) throws EmailException {
            HtmlEmail email = new HtmlEmail();
            email.setHostName(emailHostName);
            email.setSmtpPort(Integer.parseInt(smtpPort));
            email.setAuthenticator(new DefaultAuthenticator(emailUsername, emailPassword));
            email.setSSLOnConnect(true);
            email.setFrom(emailUsername);
            email.setSubject("Best rated teachers");
            email.setHtmlMsg("<p>Here are the best rated teachers:</p>" +
                    "<ul>" +
                    "<li>" + teacher.getFirstName() + " " + teacher.getLastName() + " :" + teacher.getRating() + "</li>" +
                    "</ul");
            subscribers.apply(ParDo.of(new AddSubscriberToEmailFn(email)));
            email.send();


        }
    }

    public static class AddSubscriberToEmailFn extends DoFn<Subscriber, Void> implements Serializable {

        private final HtmlEmail email;

        public AddSubscriberToEmailFn(HtmlEmail email) {
            this.email = email;
        }

        @ProcessElement
        public void processElement(@Element Subscriber subscriber) {
            try {
                email.addTo(subscriber.getEmail());
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SendEmailToSubscriber extends DoFn<Void, Void> implements Serializable {
        private final HtmlEmail email;

        public SendEmailToSubscriber(HtmlEmail email) {
            this.email = email;
        }

        @ProcessElement
        public void processElement(@Element Void element, OutputReceiver<Void> out) {
            try {
                email.send();
            } catch (EmailException emailException) {
                emailException.printStackTrace();
            }
        }
    }
}
