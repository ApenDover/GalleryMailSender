package MySQL;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Sender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sender", nullable = false)
    private int idSender;
    @Column(name = "mail", nullable = false)
    private String mail;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "status", nullable = true)
    private String status;
    @ManyToOne(cascade = CascadeType.ALL)
    private Event events;

    public int getIdSender() {
        return idSender;
    }

    public void setIdSender(int idSender) {
        this.idSender = idSender;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String isStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Event getEvents() {
        return events;
    }

    public void setEvents(Event events) {
        this.events = events;
    }
}
