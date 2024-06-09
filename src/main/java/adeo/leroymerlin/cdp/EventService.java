package adeo.leroymerlin.cdp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BandRepository bandRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public EventService(EventRepository eventRepository,
                        BandRepository bandRepository,
                        MemberRepository memberRepository) {
        this.eventRepository = eventRepository;
        this.bandRepository = bandRepository;
        this.memberRepository = memberRepository;
    }

    public List<Event> getEvents() {
        return eventRepository.findAllBy();
    }

    public void delete(Long id) {
        Optional<Event> event = eventRepository.findById(id);

        event.ifPresent(existingEvent -> {
            existingEvent.getBands().clear();
            eventRepository.delete(existingEvent);
        });
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> events = eventRepository.findAllBy();
        // Filter the events list in pure JAVA here

        return events;
    }

    public Optional<Event> update(Long id, Event updatedEvent) {
        return eventRepository.findById(id).map(event -> {
            event.setComment(updatedEvent.getComment());
            event.setImgUrl(updatedEvent.getImgUrl());
            event.setTitle(updatedEvent.getTitle());
            event.setNbStars(updatedEvent.getNbStars());

            for (Band band : event.getBands()) {
                Band eventBand = bandRepository.findByName(band.getName())
                        .orElseGet(() -> {
                            Band newBand = new Band();
                            newBand.setName(band.getName());
                            return bandRepository.save(newBand);
                        });
                for (Member member : band.getMembers()) {
                    Member bandMember = memberRepository.findByName(member.getName())
                            .orElseGet(() -> {
                                Member newMember = new Member();
                                newMember.setName(member.getName());
                                return memberRepository.save(newMember);
                            });
                    eventBand.getMembers().add(bandMember);
                }
                event.getBands().add(eventBand);
            }

            return eventRepository.save(event);

        });
    }
}
