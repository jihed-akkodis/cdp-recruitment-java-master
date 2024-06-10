package adeo.leroymerlin.cdp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private BandRepository bandRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void delete() {
        Long eventId = 1L;
        Event event = mock(Event.class);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.delete(eventId);

        verify(eventRepository).delete(event);
    }

    @Test
    void update() {
        Member member1 = new Member();
        member1.setName("Queen Anika Walsh");

        Member member2 = new Member();
        member2.setName("Queen Aliyah Jarvis");

        Band band1 = new Band();
        band1.setName("Metallica");
        band1.setMembers(new HashSet<>(List.of(member1,member2)));


        Long eventId = 1L;
        Event event = new Event();
        event.setTitle("GrasPop Metal Meeting");
        event.setComment("");
        event.setNbStars(5);
        event.setBands(new HashSet<>(List.of(band1)));
        event.setImgUrl("image.jpg");

        Event updatedEvent = new Event();
        updatedEvent.setTitle("GrasPop Metal Meeting");
        updatedEvent.setComment("super event");
        updatedEvent.setNbStars(3);
        updatedEvent.setBands(new HashSet<>(List.of(band1)));
        updatedEvent.setImgUrl("image.jpg");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bandRepository.findByName("Metallica")).thenReturn(Optional.of(band1));
        when(memberRepository.findByName("Queen Anika Walsh")).thenReturn(Optional.of(member1));
        when(memberRepository.findByName("Queen Aliyah Jarvis")).thenReturn(Optional.of(member2));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
        when(bandRepository.save(any(Band.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Event> result = eventService.update(eventId, updatedEvent);

        assertTrue(result.isPresent());
        assertEquals("GrasPop Metal Meeting", result.get().getTitle());
        assertEquals("image.jpg", result.get().getImgUrl());
        assertEquals("super event", result.get().getComment());
        assertEquals(3, result.get().getNbStars());
        assertEquals(1, result.get().getBands().size());

        Band updatedBand = result.get().getBands().iterator().next();
        assertEquals("Metallica", updatedBand.getName());
        assertEquals(2, updatedBand.getMembers().size());

    }

    @Test
    void getFilteredMembers() {
        String query = "Wa";

        Member member1 = new Member();
        member1.setName("Queen Anika Walsh");

        Member member2 = new Member();
        member2.setName("Queen Aliyah Jarvis");

        Band band1 = new Band();
        band1.setName("Metallica");
        band1.setMembers(new HashSet<>(List.of(member1, member2)));

        Band band2 = new Band();
        band2.setName("Pink Floyd");
        band2.setMembers(new HashSet<>(List.of(member2)));

        Event event1 = new Event();
        event1.setTitle("GrasPop Metal Meeting");
        event1.setComment("super event");
        event1.setNbStars(5);
        event1.setBands(new HashSet<>(List.of(band1,band2)));
        event1.setImgUrl("image.jpg");

        when(eventRepository.findAllBy()).thenReturn(List.of(event1));


        List<Event> filteredEvents = eventService.getFilteredEvents(query);


        assertNotNull(filteredEvents);
        assertEquals(1, filteredEvents.size());
        Event filteredEvent = filteredEvents.get(0);
        assertEquals("GrasPop Metal Meeting[2]", filteredEvent.getTitle());
        assertEquals(1, filteredEvent.getBands().size());
        Band filteredBand = filteredEvent.getBands().iterator().next();
        assertEquals("Metallica[1]", filteredBand.getName());
        assertEquals(1, filteredBand.getMembers().size());
        assertTrue(filteredBand.getMembers().iterator().next().getName().contains(query));

    }

}