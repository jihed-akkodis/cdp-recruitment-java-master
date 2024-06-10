package adeo.leroymerlin.cdp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
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

    /**
     * Deletes an existing event by its ID.
     *
     * It clears the associations with bands to avoid foreign key constraint violations.
     *
     *
     * @param id the ID of the event to delete.
     */
    @Transactional
    public void delete(Long id) {
        Optional<Event> event = eventRepository.findById(id);

        event.ifPresent(existingEvent -> {
            existingEvent.getBands().clear();
            eventRepository.delete(existingEvent);
        });
    }

    /**
     * get filtered events having band members name matches the query passed as param.
     *
     * @param query used to filer band members
     * @return list of events with filtered bands and members based on the query.
     */
    public List<Event> getFilteredEvents(String query) {
        List<Event> events = eventRepository.findAllBy();

        return events.stream().map(event->toEventWithFilteredBands(event,query))
                .filter(event -> !event.getBands().isEmpty())
                .collect(Collectors.toList());

    }

    /**
     * Filters Event bands based on the given query.
     * Update event's title with the count of child elements.
     *
     * @param event original event.
     * @param query used to filer band members.
     * @return Event object with filtered bands.
     */
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

    /**
     * Filters bands based on members name matches the query passed as param.
     *
     * @param bands original bands.
     * @param query used to filer band members.
     * @return bands with filtered members.
     */
    public Set<Band> getFilteredBands(Set<Band> bands,String query) {
        return bands.stream().map(band->toBandWithFilteredMembers(band,query) )
                .filter(band -> !band.getMembers().isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Filters Band members based on the given query.
     * Update band's title with the count of child elements.
     *
     * @param band the original band.
     * @param query used to filer band members.
     * @return Band with filtered members.
     */
    public Band toBandWithFilteredMembers(Band band,String query) {
        Set<Member> filteredMembers = getFilteredMembers(band,query);
        var nameWithCount = band.getName() + "["+filteredMembers.size()+"]";
        Band updatedBand = new Band();
        updatedBand.setName(nameWithCount);
        updatedBand.setMembers(filteredMembers);
        return updatedBand;
    }

    /**
     * Filters Band members based on the given query.
     *
     * @param band the original band.
     * @param query used to filer band members.
     * @return filtered members based on the given query.
     */
    public  Set<Member> getFilteredMembers(Band band,String query) {
        return band.getMembers()
                .stream()
                .filter(member -> member.getName().contains(query))
                .collect(Collectors.toSet());
    }

    /**
     * Updates an event with the provided updated event details.
     *
     * If a band or member in the updated event does not exist they are created.
     *
     * @param id the ID of the event to update.
     * @param updatedEvent event with updated details.
     * @return The updated event.
     */
    @Transactional
    public Optional<Event> update(Long id, Event updatedEvent) {
        return eventRepository.findById(id).map(event -> {

        event.setTitle(updatedEvent.getTitle());
        event.setImgUrl(updatedEvent.getImgUrl());
        event.setNbStars(updatedEvent.getNbStars());
        event.setComment(updatedEvent.getComment());

        Set<Band> updatedBands = new HashSet<>();
        for (Band band : updatedEvent.getBands()) {
            Band eventBand = bandRepository.findByName(band.getName())
                    .orElseGet(() -> {
                        Band newBand = new Band();
                        newBand.setName(band.getName());
                        return bandRepository.save(newBand);
                    });

            Set<Member> updatedMembers = new HashSet<>();
            for (Member member : band.getMembers()) {

                Member bandMember = memberRepository.findByName(member.getName())
                        .orElseGet(() -> {
                            Member newMember = new Member();
                            newMember.setName(member.getName());
                            return memberRepository.save(newMember);
                        });
                updatedMembers.add(bandMember);
            }
            eventBand.setMembers(updatedMembers);
            updatedBands.add(eventBand);
        }
        event.setBands(updatedBands);

        return eventRepository.save(event);

        });
    }
}
