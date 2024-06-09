package adeo.leroymerlin.cdp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Transactional
    public void delete(Long id) {
        Optional<Event> event = eventRepository.findById(id);

        event.ifPresent(existingEvent -> {
            existingEvent.getBands().clear();
            eventRepository.delete(existingEvent);
        });
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> events = eventRepository.findAllBy();

        return events.stream().map(event->toEventWithFilteredBands(event,query))
                .filter(event -> !event.getBands().isEmpty())
                .collect(Collectors.toList());

    }

    public Event toEventWithFilteredBands(Event event,String query) {

        Set<Band> filteredBands = getFilteredBands(event.getBands(),query);
        int eventBandsSize = filteredBands.stream().mapToInt(band -> band.getMembers().size()).sum();
        int eventElementsCount = eventBandsSize + filteredBands.size();
        String titleWithCount = event.getTitle() + "[" + eventElementsCount + "]";

        Event updatedEvent = new Event();
        updatedEvent.setId(event.getId());
        updatedEvent.setTitle(titleWithCount);
        updatedEvent.setComment(event.getComment());
        updatedEvent.setNbStars(event.getNbStars());
        updatedEvent.setBands(filteredBands);
        updatedEvent.setImgUrl(event.getImgUrl());
        return updatedEvent;

    }

    public Set<Band> getFilteredBands(Set<Band> bands,String query) {
        return bands.stream().map(band->toBandWithFilteredMembers(band,query) )
                .filter(band -> !band.getMembers().isEmpty())
                .collect(Collectors.toSet());
    }

    public Band toBandWithFilteredMembers(Band band,String query) {
        Set<Member> filteredMembers = getFilteredMembers(band,query);
        var nameWithCount = band.getName() + "["+filteredMembers.size()+"]";
        Band updatedBand = new Band();
        updatedBand.setName(nameWithCount);
        updatedBand.setMembers(filteredMembers);
        return updatedBand;
    }

    public  Set<Member> getFilteredMembers(Band band,String query) {
        return band.getMembers()
                .stream()
                .filter(member -> member.getName().contains(query))
                .collect(Collectors.toSet());
    }

    @Transactional
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
