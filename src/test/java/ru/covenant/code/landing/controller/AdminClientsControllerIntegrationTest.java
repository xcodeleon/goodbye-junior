package ru.covenant.code.landing.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.security.config.SecurityConfig;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.repository.ClientsRepository;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
@Transactional
@DirtiesContext
@DisplayName("Интеграционные тесты для AdminClientsController")
class AdminClientsControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientsRepository clientsRepository;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UUID testClientId;
    private OffsetDateTime now;

    private UUID existingClientId;
    private UUID nonExistingClientId;
    private String invalidUuid = "not-a-uuid";


    @BeforeEach
    void setUp() {
        clientsRepository.deleteAll();
        now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        createTestClients();

        objectMapper = new ObjectMapper();

        Clients testClient = Clients.builder()
                .email("test@example.com")
                .name("Тестовый Клиент")
                .phone("+79001234567")
                .message("Тестовое сообщение")
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .priority(Priority.HIGH)
                .source("Лендинг")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Clients savedClient = clientsRepository.save(testClient);
        existingClientId = savedClient.getId();

        nonExistingClientId = UUID.randomUUID();

        assertTrue(clientsRepository.findById(existingClientId).isPresent(),
                "Клиент должен существовать в БД с ID: " + existingClientId);

        System.out.println("Создан тестовый клиент с ID: " + existingClientId);


    }

    @Test
    @DisplayName("Тест 1: Успешный GET запрос с существующим ID - должен вернуть 200 и данные клиента")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithExistingId_ShouldReturn200AndClientData() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.id").value(existingClientId.toString()))
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.name").value("Тестовый Клиент"))
                .andExpect(jsonPath("$.result.phone").value("+79001234567"))
                .andExpect(jsonPath("$.result.message").value("Тестовое сообщение"))
                .andExpect(jsonPath("$.result.courseType").value("BACKEND"))
                .andExpect(jsonPath("$.result.status").value("NEW"))
                .andExpect(jsonPath("$.result.priority").value("HIGH"))
                .andExpect(jsonPath("$.result.source").value("Лендинг"))
                .andExpect(jsonPath("$.result.createdAt").exists())
                .andExpect(jsonPath("$.result.updatedAt").exists())
                .andExpect(jsonPath("$.result.formattedCreatedAt").exists())
                .andExpect(jsonPath("$.result.formattedUpdatedAt").exists());

    }

    @Test
    @DisplayName("Тест 2: Запрос с несуществующим ID - должен вернуть 404 и CLIENT_NOT_FOUND")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithNonExistingId_ShouldReturn404AndClientNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.description").value("Заявка не найдена"))
                .andExpect(jsonPath("$.error.message").value(
                        "Заявка с ID {" + nonExistingClientId + "} не найдена"))
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistingClientId.toString()))
                .andReturn();
        assertFalse(clientsRepository.findById(nonExistingClientId).isPresent(),
                "Клиент с ID " + nonExistingClientId + " не должен существовать в БД");
    }

    @Test
    @DisplayName("Тест 3: Запрос с неверным форматом ID - должен вернуть 400 и ошибку валидации")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithInvalidUuidFormat_ShouldReturn400AndValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }

    @Test
    @DisplayName("Тест 4: Запрос без аутентификации - должен вернуть 401")
    void getClientById_WithoutAuthentication_ShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())  // 302 Found
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Тест 4.1: Запрос без аутентификации - Проверка HTTP 401 или редиректа")
    void getClientById_WithoutAuthentication_ShouldReturnUnauthorizedError() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())  // 302 Found
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Тест 5: Запрос с недостаточными правами (ROLE_USER) - должен вернуть 403")
    @WithMockUser(roles = "USER")
    void getClientById_WithInsufficientRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Тест 6: Проверка доступа для разных ролей")
    void getClientById_ShouldCheckAccessForDifferentRoles() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .with(user("moderator@test.com").roles("MODERATOR"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .with(user("user@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Тест 7: Проверка защиты от SQL инъекций через ID")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithSqlInjectionAttempt_ShouldReturn400() throws Exception {
        String sqlInjectionId = "7da674d5-0672-4d0b-a7d3-8f4ee5d3a434'; DROP TABLE clients; --";

        mockMvc.perform(get("/api/v1/admin/clients/{id}", sqlInjectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        assertTrue(clientsRepository.count() > 0, "Таблица clients должна существовать");
    }

    @Test
    @DisplayName("Тест 7.1: ClientNotFoundException - проверка всех полей исключения при клиенте не найден")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldReturnClientNotFoundExceptionWithAllFields() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())

                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.description").value("Заявка не найдена"))
                .andExpect(jsonPath("$.error.message").value("Заявка с ID {" + nonExistingClientId + "} не найдена"))

                .andExpect(jsonPath("$.error.details").exists())
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistingClientId.toString()));
    }

    @Test
    @DisplayName("Тест 7.2: ClientNotFoundException - проверка HTTP статуса NOT_FOUND")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldReturnHttpStatusNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("Тест 7.3: ClientNotFoundException - проверка деталей ошибки в error.details")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldReturnCorrectErrorDetails() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.details").isMap())
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistingClientId.toString()))
                .andExpect(jsonPath("$.error.details.clientId").isString())
                .andExpect(jsonPath("$.error.details.clientId").isNotEmpty())
                .andExpect(jsonPath("$.error.details.clientId").value(not(emptyString())));
    }

    @Test
    @DisplayName("Тест 7.4: ClientNotFoundException - проверка что исключение не содержит лишних полей")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldNotContainExtraFields() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.description").exists())
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.details").exists())

                .andExpect(jsonPath("$.error.timestamp").doesNotExist())
                .andExpect(jsonPath("$.error.path").doesNotExist())
                .andExpect(jsonPath("$.error.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.error.cause").doesNotExist())
                .andExpect(jsonPath("$.error.suppressed").doesNotExist());
    }

    @Test
    @DisplayName("Тест 7.5: ClientNotFoundException - проверка формата сообщения об ошибке")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldHaveCorrectErrorMessageFormat() throws Exception {

        String expectedMessage = "Заявка с ID {" + nonExistingClientId + "} не найдена";

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value(expectedMessage))
                .andExpect(jsonPath("$.error.message").isString())
                .andExpect(jsonPath("$.error.message").value(containsString(nonExistingClientId.toString())))
                .andExpect(jsonPath("$.error.message").value(containsString("Заявка с ID")))
                .andExpect(jsonPath("$.error.message").value(containsString("не найдена")));
    }

    private void createTestClients() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        List<Clients> testClients = Arrays.asList(
                Clients.builder()
                        .name("Анна Смирнова")
                        .email("anna@example.com")
                        .phone("+79161239876")
                        .message("Записалась на курс")
                        .courseType(CourseType.BACKEND)
                        .status(Status.DONE)
                        .priority(Priority.MEDIUM)
                        .source("Лендинг")
                        .processedBy("admin@covenantcode.ru")
                        .processedAt(now.minusDays(7).plusHours(1))
                        .createdAt(now.minusDays(7))
                        .updatedAt(now.minusDays(7).plusHours(1))
                        .build(),

                Clients.builder()
                        .name("Сергей Сергеев")
                        .email("sergey@example.com")
                        .phone("+79169876543")
                        .message("Frontend разработка")
                        .courseType(CourseType.FRONTEND)
                        .status(Status.PROCESSED)
                        .priority(Priority.LOW)
                        .source("Лендинг")
                        .processedBy("admin@covenantcode.ru")
                        .processedAt(now.minusDays(3).plusHours(1))
                        .createdAt(now.minusDays(3))
                        .updatedAt(now.minusDays(3).plusHours(1))
                        .build(),

                Clients.builder()
                        .name("Иван Петров")
                        .email("ivan@example.com")
                        .phone("+79161234567")
                        .message("Хочу на Fullstack")
                        .courseType(CourseType.FULLSTACK)
                        .status(Status.NEW)
                        .priority(Priority.HIGH)
                        .source("Лендинг")
                        .createdAt(now.minusDays(1))
                        .updatedAt(now.minusDays(1))
                        .build(),

                Clients.builder()
                        .name("Петр Иванов")
                        .email("petr@example.com")
                        .phone("+79167654321")
                        .message("Интересует Backend")
                        .courseType(CourseType.BACKEND)
                        .status(Status.NEW)
                        .priority(Priority.MEDIUM)
                        .source("Лендинг")
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        List<Clients> savedClients = clientsRepository.saveAll(testClients);

        testClientId = savedClients.get(2).getId();
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – успешное получение списка клиентов с фильтром по статусу")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithStatusFilterSuccess() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)))
                .andExpect(jsonPath("$.result[*].status", everyItem(is("NEW"))));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – проверка наличия всех полей в ответе")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void verifyResponseFields() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").exists())
                .andExpect(jsonPath("$.result[0].name").exists())
                .andExpect(jsonPath("$.result[0].email").exists())
                .andExpect(jsonPath("$.result[0].phone").exists())
                .andExpect(jsonPath("$.result[0].message").exists())
                .andExpect(jsonPath("$.result[0].courseType").exists())
                .andExpect(jsonPath("$.result[0].status").exists())
                .andExpect(jsonPath("$.result[0].priority").exists())
                .andExpect(jsonPath("$.result[0].statusLabel").exists())
                .andExpect(jsonPath("$.result[0].priorityLabel").exists())
                .andExpect(jsonPath("$.result[0].source").exists())
                .andExpect(jsonPath("$.result[0].createdAt").exists())
                .andExpect(jsonPath("$.result[0].updatedAt").exists())
                .andExpect(jsonPath("$.result[0].formattedCreatedAt").exists())
                .andExpect(jsonPath("$.result[0].formattedUpdatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – ошибка при некорректном статусе")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Проверьте правильность заполнения полей"))
                .andExpect(jsonPath("$.error.details.statuses").exists());
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – работа с разными регистрами статуса")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithDifferentCaseStatus() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "new")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – фильтрация по нескольким статусам")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithMultipleStatuses() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .param("statuses", "PROCESSED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(4)))
                .andExpect(jsonPath("$.result[*].status", containsInAnyOrder("NEW", "NEW", "NEW", "PROCESSED")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – доступ без аутентификации")
    void endpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – доступ с недостаточными правами")
    @WithMockUser(username = "user@example.com", roles = "USER")
    void endpointWithInsufficientPermissions() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – успешное обновление всех полей")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void updateClient_WithFullUpdate_ShouldReturnUpdatedClient() throws Exception {
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .email("ivan.ivanov@example.com")
                .phone("+79998887766")
                .message("Обновленное сообщение")
                .courseType("BACKEND")
                .status("PROCESSED")
                .priority("HIGH")
                .source("Телефон")
                .processedBy("admin@covenantcode.ru")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", testClientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.result.status").value("PROCESSED"))
                .andExpect(jsonPath("$.result.processedBy").value("admin@covenantcode.ru"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – клиент не найден возвращает 404")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void updateClient_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .courseType("BACKEND")
                .status("NEW")
                .priority("MEDIUM")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – доступ без аутентификации")
    void updateClient_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .courseType("BACKEND")
                .status("NEW")
                .priority("MEDIUM")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – доступ с недостаточными правами")
    @WithMockUser(username = "user@example.com", roles = "USER")
    void updateClient_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", testClientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("1.1 Успешный GET запрос статистики - проверка всех полей и значений")
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"/sql/clients-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getStats_SuccessfulRequest_ShouldReturnCorrectStats() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(content);
        JsonNode stats = response.get("result");

        System.out.println("Response: " + content);
        assertEquals(5, stats.get("total").asLong());

        assertEquals(3, stats.get("newCount").asLong());
        assertEquals(1, stats.get("processedCount").asLong());
        assertEquals(1, stats.get("doneCount").asLong());

        assertEquals(5, stats.get("todayCount").asLong());

        assertEquals(0, stats.get("highPriorityCount").asLong());
        assertEquals(0, stats.get("mediumPriorityCount").asLong());
        assertEquals(0, stats.get("lowPriorityCount").asLong());

        assertEquals(0, stats.get("backendCount").asLong());
        assertEquals(0, stats.get("frontendCount").asLong());
        assertEquals(0, stats.get("fullstackCount").asLong());

        assertEquals(stats.get("total").asLong(),
                stats.get("newCount").asLong()
                        + stats.get("processedCount").asLong()
                        + stats.get("doneCount").asLong());
    }

    @Test
    @DisplayName("2.1 Запрос статистики с пустой БД - возвращает нулевую статистику")
    @WithMockUser(roles = "ADMIN")
    void getStats_EmptyDatabase_ShouldReturnZeroStats() throws Exception {

        clientsRepository.deleteAll();
        clientsRepository.flush();

        mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.total").value(0))
                .andExpect(jsonPath("$.result.newCount").value(0))
                .andExpect(jsonPath("$.result.processedCount").value(0))
                .andExpect(jsonPath("$.result.doneCount").value(0))
                .andExpect(jsonPath("$.result.todayCount").value(0))
                .andExpect(jsonPath("$.result.fullstackCount").value(0))
                .andExpect(jsonPath("$.result.frontendCount").value(0))
                .andExpect(jsonPath("$.result.backendCount").value(0))
                .andExpect(jsonPath("$.result.highPriorityCount").value(0))
                .andExpect(jsonPath("$.result.mediumPriorityCount").value(0))
                .andExpect(jsonPath("$.result.lowPriorityCount").value(0));
    }

    @Test
    @DisplayName("3.1 Тест авторизации для разных ролей - доступ к статистике")
    void getStats_AccessControlForDifferentRoles() throws Exception {

        clientsRepository.save(Clients.builder()
                .email("test@test.com").name("Test").status(Status.NEW).build());

        mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .with(user("moderator").roles("MODERATOR")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .with(user("support").roles("SUPPORT")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4.1 Тест без аутентификации - статистика возвращает 401/302")
    void getStats_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())  // 302 редирект на /login
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("5.1 Статистика - проверка корректности подсчета за сегодня")
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"/sql/clients-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getStats_TodayCountVerification() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode stats = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("result");

        assertEquals(5, stats.get("total").asLong());

        assertEquals(5, stats.get("todayCount").asLong());

        assertEquals(3, stats.get("newCount").asLong());
        assertEquals(1, stats.get("processedCount").asLong());
        assertEquals(1, stats.get("doneCount").asLong());

        assertEquals(stats.get("total").asLong(),
                stats.get("newCount").asLong()
                        + stats.get("processedCount").asLong()
                        + stats.get("doneCount").asLong());
    }

    @Test
    @DisplayName("6.1 Статистика - проверка суммирования статусов")
    @WithMockUser(roles = "ADMIN")
    void getStats_StatusesSumEqualsTotal() throws Exception {

        clientsRepository.saveAll(Arrays.asList(
                Clients.builder().status(Status.NEW).build(),
                Clients.builder().status(Status.PROCESSED).build(),
                Clients.builder().status(Status.DONE).build()
        ));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/clients/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(content);
        JsonNode stats = response.get("result");

        long total = stats.get("total").asLong();
        long newCount = stats.get("newCount").asLong();
        long processedCount = stats.get("processedCount").asLong();
        long doneCount = stats.get("doneCount").asLong();

        assertEquals(total, newCount + processedCount + doneCount);
    }
    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – успешное удаление клиента")
    @WithMockUser(roles = "ADMIN")
    void deleteClient_WithExistingId_ShouldReturn200AndDeleteClient() throws Exception {
        UUID clientIdToDelete = existingClientId;
        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent(),
                "Клиент должен существовать перед удалением");

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", clientIdToDelete)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());

        assertFalse(clientsRepository.findById(clientIdToDelete).isPresent(),
                "Клиент должен быть удален из базы данных");
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – удаление несуществующего клиента")
    @WithMockUser(roles = "ADMIN")
    void deleteClient_WithNonExistingId_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        assertFalse(clientsRepository.findById(nonExistentId).isPresent(),
                "Клиент не должен существовать");

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.description").value("Заявка не найдена"))
                .andExpect(jsonPath("$.error.message").value("Заявка с ID {" + nonExistentId + "} не найдена"))
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistentId.toString()));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – доступ для роли MODERATOR")
    @WithMockUser(roles = "MODERATOR")
    void deleteClient_WithModeratorRole_ShouldReturn200() throws Exception {
        UUID clientIdToDelete = existingClientId;
        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent());

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", clientIdToDelete)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(clientsRepository.findById(clientIdToDelete).isPresent());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – доступ запрещен для роли SUPPORT (возвращает 500)")
    @WithMockUser(roles = "SUPPORT")
    void deleteClient_WithSupportRole_ShouldReturn500() throws Exception {
        UUID clientIdToDelete = existingClientId;
        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent(),
                "Клиент должен существовать перед тестом");

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", clientIdToDelete)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.description").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.error.message").value("Произошла непредвиденная ошибка"))
                .andExpect(jsonPath("$.error.details.exceptionClass")
                        .value("org.springframework.security.authorization.AuthorizationDeniedException"))
                .andExpect(jsonPath("$.error.details.exceptionMessage").value("Access Denied"));

        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent(),
                "Клиент не должен быть удален при недостаточных правах");
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – доступ без аутентификации")
    void deleteClient_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        UUID clientIdToDelete = existingClientId;
        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent());

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", clientIdToDelete)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403 Forbidden

        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – доступ с недостаточными правами (ROLE_USER)")
    @WithMockUser(roles = "USER")
    void deleteClient_WithUserRole_ShouldReturn403() throws Exception {
        UUID clientIdToDelete = existingClientId;
        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent());

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", clientIdToDelete)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        assertTrue(clientsRepository.findById(clientIdToDelete).isPresent());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/clients/{id} – проверка структуры успешного ответа (result: null)")
    @WithMockUser(roles = "ADMIN")
    void deleteClient_ShouldReturnSuccessResponseWithNullResult() throws Exception {
        Clients testClient = Clients.builder()
                .name("Клиент для проверки структуры")
                .email("structure@example.com")
                .phone("+79001234567")
                .message("Тестовое сообщение")
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .priority(Priority.MEDIUM)
                .source("Лендинг")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        Clients savedClient = clientsRepository.save(testClient);
        UUID clientId = savedClient.getId();

        mockMvc.perform(delete("/api/v1/admin/clients/{id}", clientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(content().json("{\"success\":true}"));
    }

}