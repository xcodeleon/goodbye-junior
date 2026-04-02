package ru.covenant.code.landing.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционные тесты репозитория ClientsRepository")
class ClientsRepositoryTest {

    @Autowired
    private ClientsRepository clientsRepository;

    private OffsetDateTime now;
    private OffsetDateTime threeDaysAgo;
    private OffsetDateTime twoDaysAgo;
    private OffsetDateTime oneDayAgo;
    private OffsetDateTime tomorrow;
    private OffsetDateTime yesterday;

    @BeforeEach
    void setUp() {
        //  clientsRepository.deleteAllInBatch();
        clientsRepository.deleteAll();
        clientsRepository.flush();

        now = OffsetDateTime.now(ZoneOffset.UTC);
        threeDaysAgo = now.minusDays(3).minusNanos(1);
        twoDaysAgo = now.minusDays(2);
        oneDayAgo = now.minusDays(1);
        tomorrow = now.plusDays(1);
        yesterday = now.minusDays(1);
    }

    private List<Clients> createTestClients() {
        Clients client1 = Clients.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Хочу на Backend")
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .priority(Priority.HIGH)
                .source("Лендинг")
                .createdAt(threeDaysAgo)
                .updatedAt(threeDaysAgo)
                .build();

        Clients client2 = Clients.builder()
                .name("Петр Петров")
                .email("petr@example.com")
                .phone("+79167654321")
                .message("Интересует Frontend")
                .courseType(CourseType.FRONTEND)
                .status(Status.PROCESSED)
                .priority(Priority.MEDIUM)
                .source("Лендинг")
                .createdAt(twoDaysAgo)
                .updatedAt(twoDaysAgo)
                .processedAt(oneDayAgo)
                .processedBy("admin@example.com")
                .build();

        Clients client3 = Clients.builder()
                .name("Сергей Сергеев")
                .email("sergey@example.com")
                .phone("+79169876543")
                .message("Хочу на Fullstack")
                .courseType(CourseType.FULLSTACK)
                .status(Status.NEW)
                .priority(Priority.LOW)
                .source("Лендинг")
                .createdAt(oneDayAgo)
                .updatedAt(oneDayAgo)
                .build();

        Clients client4 = Clients.builder()
                .name("Анна Аннова")
                .email("anna@example.com")
                .phone("+79165554433")
                .message("Записалась на Backend")
                .courseType(CourseType.BACKEND)
                .status(Status.DONE)
                .priority(Priority.MEDIUM)
                .source("Лендинг")
                .createdAt(now)
                .updatedAt(now)
                .processedAt(now)
                .processedBy("admin@example.com")
                .build();

        List<Clients> clients = List.of(client1, client2, client3, client4);

        clientsRepository.saveAll(clients);
        clientsRepository.flush();

        return clients;
    }

    private Clients createClient(String email, String name, Status status, CourseType courseType, Priority priority) {
        return Clients.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name(name)
                .phone("+71234567890")
                .message("Тестовое сообщение для клиента " + name)
                .courseType(courseType)
                .status(status)
                .priority(priority)
                .source("Лендинг")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    private Clients createClientForStats(String email, String name, Status status,
                                         CourseType courseType, Priority priority) {
        Clients client = new Clients();
        client.setEmail(email);
        client.setName(name);
        client.setStatus(status);
        client.setCourseType(courseType);
        client.setPriority(priority);
        return client;
    }

    private Clients createClientWithDate(String email, String name, OffsetDateTime createdAt) {
        Clients client = Clients.builder()
                .email(email)
                .name(name)
                .createdAt(createdAt)
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .phone("123456789")
                .source("TEST")
                .build();
        return clientsRepository.saveAndFlush(client);
    }


    @Test
    @DisplayName("countByStatus - проверка подсчета клиентов по статусу NEW")
    void countByStatus_ShouldCountNewClients() {

        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.BACKEND, Priority.MEDIUM),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.LOW)
        );
        clientsRepository.saveAll(clientsToSave);

        long newCount = clientsRepository.countByStatus(Status.NEW);

        assertEquals(2L, newCount);
    }

    @Test
    @DisplayName("countByStatus - проверка подсчета клиентов по статусу PROCESSED")
    void countByStatus_ShouldCountProcessedClients() {

        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.PROCESSED, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.BACKEND, Priority.MEDIUM),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.LOW)
        );
        clientsRepository.saveAll(clientsToSave);

        long processedCount = clientsRepository.countByStatus(Status.PROCESSED);

        assertEquals(2L, processedCount);
    }


    @Test
    @DisplayName("countByStatus - проверка подсчета клиентов по статусу DONE")
    void countByStatus_ShouldCountDoneClients() {

        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.DONE, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.BACKEND, Priority.MEDIUM),
                createClientForStats("client3@test.com", "Клиент 3", Status.DONE, CourseType.FULLSTACK, Priority.LOW)
        );
        clientsRepository.saveAll(clientsToSave);

        long doneCount = clientsRepository.countByStatus(Status.DONE);

        assertEquals(2L, doneCount, "Должно быть 2 клиента со статусом DONE");
    }

    @Test
    @DisplayName("countByCreatedAtAfter - проверка подсчета за разные периоды")
    void countByCreatedAtAfter_ShouldCountCorrectly() {

        OffsetDateTime cutoff = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        createClientWithDate("after@example.com", "After", cutoff.plusHours(1));

        long count = clientsRepository.countByCreatedAtAfter(cutoff);

        assertEquals(1L, count, "Должен найти 1 клиента после cutoff времени");
    }

    @Test
    @DisplayName("countByCreatedAtBetween - проверка подсчета за сегодня")
    void countByCreatedAtBetween_ShouldCountTodayClients() {

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1).withHour(12);

        Clients client1 = createClientWithDate("client1@test.com", "Клиент 1", now);
        Clients client2 = createClientWithDate("client2@test.com", "Клиент 2", now);


        clientsRepository.saveAll(List.of(client1, client2));

        OffsetDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        long todayCount = clientsRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        assertEquals(2L, todayCount, "За сегодня должно быть 2 клиента");
    }

    @Test
    @DisplayName("countByCourseType - проверка подсчета клиентов по типу курса FULLSTACK")
    void countByCourseType_ShouldCountFullstackClients() {

        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.FULLSTACK, Priority.MEDIUM),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.MEDIUM),
                createClientForStats("crrlient3@test.com", "Клиент 4", Status.PROCESSED, CourseType.FULLSTACK, Priority.MEDIUM)
        );
        clientsRepository.saveAll(clientsToSave);

        long fullstackCount = clientsRepository.countByCourseType(CourseType.FULLSTACK);

        assertEquals(3L, fullstackCount, "Должно быть 3 клиента на FULLSTACK курс");
    }

    @Test
    @DisplayName("countByCourseType - проверка подсчета клиентов по типу курса FRONTEND")
    void countByCourseType_ShouldCountFrontendClients() {
        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.BACKEND, Priority.MEDIUM),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.MEDIUM),
                createClientForStats("crrlient3@test.com", "Клиент 4", Status.PROCESSED, CourseType.FRONTEND, Priority.MEDIUM)
        );
        clientsRepository.saveAll(clientsToSave);

        long frontendCount = clientsRepository.countByCourseType(CourseType.FRONTEND);

        assertEquals(2L, frontendCount, "Должно быть 2 клиента на FRONTEND курс");
    }


    @Test
    @DisplayName("countByCourseType - проверка подсчета клиентов по типу курса BACKEND")
    void countByCourseType_ShouldCountBackendClients() {
        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.BACKEND, Priority.HIGH),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.LOW)
        );
        clientsRepository.saveAll(clientsToSave);

        long backendCount = clientsRepository.countByCourseType(CourseType.BACKEND);

        assertEquals(1L, backendCount, "Должно быть 2 клиента на BACKEND курс");
    }


    @Test
    @DisplayName("countByPriority - проверка подсчета клиентов с приоритетом HIGH")
    void countByPriority_ShouldCountHighPriorityClients() {

        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.BACKEND, Priority.HIGH),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.LOW)
        );
        clientsRepository.saveAll(clientsToSave);
        long newCount = clientsRepository.countByPriority(Priority.HIGH);
        assertEquals(2L, newCount);
    }

    @Test
    @DisplayName("countByPriority - проверка подсчета клиентов с приоритетом MEDIUM")
    void countByPriority_ShouldCountMediumPriorityClients() {

        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.BACKEND, Priority.MEDIUM),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.MEDIUM),
                createClientForStats("crrlient3@test.com", "Клиент 4", Status.PROCESSED, CourseType.FULLSTACK, Priority.MEDIUM)
        );
        clientsRepository.saveAll(clientsToSave);

        long mediumPriorityCount = clientsRepository.countByPriority(Priority.MEDIUM);

        assertEquals(3L, mediumPriorityCount, "Должно быть 3 клиента со средним приоритетом");
    }

    @Test
    @DisplayName("countByPriority - проверка подсчета клиентов с приоритетом LOW")
    void countByPriority_ShouldCountLowPriorityClients() {
        List<Clients> clientsToSave = List.of(
                createClientForStats("client1@test.com", "Клиент 1", Status.NEW, CourseType.BACKEND, Priority.LOW),
                createClientForStats("client2@test.com", "Клиент 2", Status.NEW, CourseType.FRONTEND, Priority.HIGH),
                createClientForStats("client3@test.com", "Клиент 3", Status.PROCESSED, CourseType.FULLSTACK, Priority.LOW),
                createClientForStats("crrlient3@test.com", "Клиент 4", Status.PROCESSED, CourseType.FULLSTACK, Priority.MEDIUM)
        );
        clientsRepository.saveAll(clientsToSave);

        long lowPriorityCount = clientsRepository.countByPriority(Priority.LOW);

        assertEquals(2L, lowPriorityCount, "Должно быть 2 клиента с низким приоритетом");
    }


    @Test
    @DisplayName("Граничный тест: подсчет при отсутствии данных")
    void countMethods_WithEmptyDatabase_ShouldReturnZero() {
        // When
        long total = clientsRepository.count();
        long newCount = clientsRepository.countByStatus(Status.NEW);
        long processedCount = clientsRepository.countByStatus(Status.PROCESSED);
        long doneCount = clientsRepository.countByStatus(Status.DONE);
        long fullstackCount = clientsRepository.countByCourseType(CourseType.FULLSTACK);
        long frontendCount = clientsRepository.countByCourseType(CourseType.FRONTEND);
        long backendCount = clientsRepository.countByCourseType(CourseType.BACKEND);
        long highPriorityCount = clientsRepository.countByPriority(Priority.HIGH);
        long mediumPriorityCount = clientsRepository.countByPriority(Priority.MEDIUM);
        long lowPriorityCount = clientsRepository.countByPriority(Priority.LOW);

        assertEquals(0L, total);
        assertEquals(0L, newCount);
        assertEquals(0L, processedCount);
        assertEquals(0L, doneCount);
        assertEquals(0L, fullstackCount);
        assertEquals(0L, frontendCount);
        assertEquals(0L, backendCount);
        assertEquals(0L, highPriorityCount);
        assertEquals(0L, mediumPriorityCount);
        assertEquals(0L, lowPriorityCount);
    }

    @Test
    @DisplayName("Граничный тест: подсчет с большим количеством данных")
    void countMethods_WithLargeDataSet_ShouldReturnCorrectCounts() {

        List<Clients> allClients = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Status status = i % 3 == 0 ? Status.NEW : (i % 3 == 1 ? Status.PROCESSED : Status.DONE);
            CourseType courseType = i % 3 == 0 ? CourseType.BACKEND : (i % 3 == 1 ? CourseType.FRONTEND : CourseType.FULLSTACK);
            Priority priority = i % 3 == 0 ? Priority.HIGH : (i % 3 == 1 ? Priority.MEDIUM : Priority.LOW);

            Clients client = createClientForStats(
                    "client" + i + "@test.com",
                    "Клиент " + i,
                    status,
                    courseType,
                    priority
            );
            allClients.add(client);
        }

        clientsRepository.saveAll(allClients);

        long total = clientsRepository.count();
        long newCount = clientsRepository.countByStatus(Status.NEW);
        long processedCount = clientsRepository.countByStatus(Status.PROCESSED);
        long doneCount = clientsRepository.countByStatus(Status.DONE);
        long backendCount = clientsRepository.countByCourseType(CourseType.BACKEND);
        long frontendCount = clientsRepository.countByCourseType(CourseType.FRONTEND);
        long fullstackCount = clientsRepository.countByCourseType(CourseType.FULLSTACK);
        long highPriorityCount = clientsRepository.countByPriority(Priority.HIGH);
        long mediumPriorityCount = clientsRepository.countByPriority(Priority.MEDIUM);
        long lowPriorityCount = clientsRepository.countByPriority(Priority.LOW);

        assertEquals(100L, total);
        assertEquals(34L, newCount);
        assertEquals(33L, processedCount);
        assertEquals(33L, doneCount);
        assertEquals(34L, backendCount);
        assertEquals(33L, frontendCount);
        assertEquals(33L, fullstackCount);
        assertEquals(34L, highPriorityCount);
        assertEquals(33L, mediumPriorityCount);
        assertEquals(33L, lowPriorityCount);
    }


    @Test
    @DisplayName("Работа с временными зонами - проверка countByCreatedAtBetween с разными зонами")
    void countByCreatedAtBetween_ShouldWorkWithTimeZones() {
        clientsRepository.deleteAll();

        ZonedDateTime now = ZonedDateTime.ofInstant(
                Instant.now(),
                ZoneId.of("UTC")
        );

        System.out.println("Исходное время (UTC): " + now);

        OffsetDateTime utcTime = OffsetDateTime.parse("2024-04-01T21:00:00Z");
        System.out.println("UTC время для сохранения: " + utcTime);
        System.out.println("UTC epoch секунд: " + utcTime.toEpochSecond());

        Clients client = new Clients();
        client.setEmail("test@example.com");
        client.setName("Test User");
        client.setCourseType(CourseType.BACKEND);
        client.setCreatedAt(now.toOffsetDateTime());
        clientsRepository.saveAndFlush(client);

        Clients saved = clientsRepository.findAll().get(0);
        System.out.println("Сохранено в БД: " + saved.getCreatedAt());

        OffsetDateTime start = now.toOffsetDateTime().minusMinutes(5);
        OffsetDateTime end = now.toOffsetDateTime().plusMinutes(5);

        System.out.println("Поиск от: " + start + " до: " + end);

        long count = clientsRepository.countByCreatedAtBetween(start, end);

        assertEquals(1L, count,
                "Ожидали 1 запись, нашли " + count +
                        ". Диапазон: " + start + " - " + end +
                        ", сохранено: " + saved.getCreatedAt());
    }

    @Test
    @DisplayName("findByStatus с сортировкой по createdAt DESC")
    void findByStatus_ShouldFilterAndSort() {
        // Given
        createTestClients();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Clients> newClients = clientsRepository.findByStatus(Status.NEW, sort);

        assertThat(newClients)
                .isNotNull()
                .hasSize(2)
                .extracting(Clients::getStatus)
                .containsOnly(Status.NEW);

        assertThat(newClients.get(0).getCreatedAt())
                .isAfter(newClients.get(1).getCreatedAt());
        assertThat(newClients.get(0).getName()).isEqualTo("Сергей Сергеев");
        assertThat(newClients.get(1).getName()).isEqualTo("Иван Иванов");
    }

    @Test
    @DisplayName("findByStatus для всех значений Status enum")
    void findByStatus_ShouldWorkForAllStatusEnums() {
        createTestClients();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Clients> newClients = clientsRepository.findByStatus(Status.NEW, sort);
        assertThat(newClients).hasSize(2);
        assertThat(newClients).allMatch(c -> c.getStatus() == Status.NEW);

        List<Clients> processedClients = clientsRepository.findByStatus(Status.PROCESSED, sort);
        assertThat(processedClients).hasSize(1);
        assertThat(processedClients.get(0).getStatus()).isEqualTo(Status.PROCESSED);
        assertThat(processedClients.get(0).getName()).isEqualTo("Петр Петров");

        List<Clients> doneClients = clientsRepository.findByStatus(Status.DONE, sort);
        assertThat(doneClients).hasSize(1);
        assertThat(doneClients.get(0).getStatus()).isEqualTo(Status.DONE);
        assertThat(doneClients.get(0).getName()).isEqualTo("Анна Аннова");
    }

    @Test
    @DisplayName("countByStatus возвращает правильное количество")
    void countByStatus_ShouldReturnCorrectCount() {
        createTestClients();

        assertThat(clientsRepository.countByStatus(Status.NEW)).isEqualTo(2);
        assertThat(clientsRepository.countByStatus(Status.PROCESSED)).isEqualTo(1);
        assertThat(clientsRepository.countByStatus(Status.DONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("countByCreatedAtBetween с пустым периодом возвращает 0")
    void countByCreatedAtBetween_WithEmptyPeriod_ShouldReturnZero() {
        createTestClients();

        long count = clientsRepository.countByCreatedAtBetween(
                now.minusDays(100),
                now.minusDays(50)
        );
        assertThat(count).isZero();

        count = clientsRepository.countByCreatedAtBetween(
                now.plusDays(1),
                now.plusDays(10)
        );
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("countByCreatedAtBetween с обратным порядком дат")
    void countByCreatedAtBetween_WithReversedDates_ShouldReturnZero() {
        createTestClients();

        long count = clientsRepository.countByCreatedAtBetween(tomorrow, twoDaysAgo);
        assertThat(count).isZero();
    }
}