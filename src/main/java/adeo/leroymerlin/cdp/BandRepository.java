package adeo.leroymerlin.cdp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BandRepository extends JpaRepository<Band,Long> {
    Optional<Band> findByName(String name);
}
