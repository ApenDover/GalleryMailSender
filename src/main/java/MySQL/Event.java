package MySQL;

import javax.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event", nullable = false)
    private int idEvent;
    @ManyToOne(cascade = CascadeType.ALL)
    private Company company;
    @Column(name = "date", nullable = false)
    private Date date;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "events")
    private List<Sender> senders = new ArrayList<>();

}
