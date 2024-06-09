package adeo.leroymerlin.cdp;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "band_members",
            joinColumns = @JoinColumn(name = "band_id"),
            inverseJoinColumns = @JoinColumn(name = "members_id"))
    private Set<Member> members;

    public Band() {
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
