import MySQL.Sender;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainSender {

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {

//      Парсим messageSettings заполняя настройки для подключения к SMTP
        FileReader reader = new FileReader("src/main/resources/messageSettings.json");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        String login = (String) jsonObject.get("login");
        String password = (String) jsonObject.get("password");
        String subject = (String) jsonObject.get("subject");
        String text = (String) jsonObject.get("text");

//      Подключаем hibernate.cfg
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
        Session session = sessionFactory.openSession();

//      Вытаскиваем из таблицы Sender все новые записи
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Sender> query = builder.createQuery(Sender.class);
        CriteriaUpdate<Sender> criteriaUpdate = builder.createCriteriaUpdate(Sender.class);
        Root<Sender> rootUpdate = criteriaUpdate.from(Sender.class);
        Root<Sender> rootQuery = query.from(Sender.class);
        query.select(rootQuery).where(builder.equal(rootQuery.get("status"), "NEW"));
        List<Sender> senderList = session.createQuery(query).getResultList();

//      Отправляем письмо на почту, заполняя статус отправки
        senderList.forEach(sender -> {
            String mailTO = sender.getMail();
            String attachedPath = sender.getPath();
//          если вложение > 25Mb, то abort, добавив запись в базу
            if ((new File(sender.getPath()).length()) > 25000000) {
                criteriaUpdate.set("status", "TOO BIG");
                criteriaUpdate.where(builder.equal(rootUpdate.get("idSender"), sender.getIdSender()));
                Transaction transaction = session.beginTransaction();
                session.createQuery(criteriaUpdate).executeUpdate();
                transaction.commit();
            } else {
                SSLGmailSender sslSender = new SSLGmailSender(login, password);
                try {
                    String statusSender = sslSender.send(subject, text, attachedPath, mailTO);
                    criteriaUpdate.set("status", statusSender);
                    criteriaUpdate.where(builder.equal(rootUpdate.get("idSender"), sender.getIdSender()));
                    Transaction transaction = session.beginTransaction();
                    session.createQuery(criteriaUpdate).executeUpdate();
                    transaction.commit();
                } catch (Exception e) {
                    System.out.println("MAIN ERROR: " + e.getMessage());
                    if (e.getMessage().equals("Unknown SMTP host: smtp.gmail.com")) {
                        criteriaUpdate.set("status", "NO INTERNET");
                        criteriaUpdate.where(builder.equal(rootUpdate.get("idSender"), sender.getIdSender()));
                        Transaction transaction = session.beginTransaction();
                        session.createQuery(criteriaUpdate).executeUpdate();
                        transaction.commit();
                    } else {
                        criteriaUpdate.set("status", "ERROR");
                        criteriaUpdate.where(builder.equal(rootUpdate.get("idSender"), sender.getIdSender()));
                        Transaction transaction = session.beginTransaction();
                        session.createQuery(criteriaUpdate).executeUpdate();
                        transaction.commit();
                    }
                }
            }
        });
        session.close();
    }
}