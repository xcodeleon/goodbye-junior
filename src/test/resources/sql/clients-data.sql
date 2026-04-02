INSERT INTO clients_db (id, name, email, phone, message, course_type, status, priority, source, created_at, updated_at) VALUES
-- NEW (5 записей)
(RANDOM_UUID(), 'Иван Иванов', 'ivan@test.ru', '+7 900 111-22-33', 'Хочу записаться на курс', 'FULLSTACK', 'NEW', 'LOW', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Мария Петрова', 'maria@test.ru', '+7 900 222-33-44', 'Интересует программа обучения', 'FRONTEND', 'NEW', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Алексей Сидоров', 'alex@test.ru', '+7 900 333-44-55', 'Когда начало занятий?', 'BACKEND', 'NEW', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Елена Смирнова', 'elena@test.ru', '+7 900 444-55-66', 'Есть ли рассрочка?', 'FULLSTACK', 'NEW', 'LOW', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Дмитрий Кузнецов', 'dmitry@test.ru', '+7 900 555-66-77', 'Нужна консультация', 'FRONTEND', 'NEW', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- PROCESSED (6 записей)
(RANDOM_UUID(), 'Анна Соколова', 'anna@test.ru', '+7 900 666-77-88', 'Уже оплатила курс', 'BACKEND', 'PROCESSED', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Сергей Попов', 'sergey@test.ru', '+7 900 777-88-99', 'Прошу прислать чек', 'FULLSTACK', 'PROCESSED', 'LOW', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Ольга Васильева', 'olga@test.ru', '+7 900 888-99-00', 'Не пришло письмо с доступом', 'FRONTEND', 'PROCESSED', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Игорь Федоров', 'igor@test.ru', '+7 900 999-00-11', 'Смена формата обучения', 'BACKEND', 'PROCESSED', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Светлана Михайлова', 'svetlana@test.ru', '+7 900 000-11-22', 'Корпоративное обучение', 'FULLSTACK', 'PROCESSED', 'LOW', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Виктор Морозов', 'viktor@test.ru', '+7 900 123-45-67', 'Вопрос по трудоустройству', 'FRONTEND', 'PROCESSED', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- DONE (4 записи)
(RANDOM_UUID(), 'Наталья Ковалева', 'natalya@test.ru', '+7 900 234-56-78', 'Спасибо за курс!', 'BACKEND', 'DONE', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Андрей Белов', 'andrey@test.ru', '+7 900 345-67-89', 'Отличный преподаватель', 'FULLSTACK', 'DONE', 'LOW', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Юлия Григорьева', 'yulia@test.ru', '+7 900 456-78-90', 'Рекомендую вашу школу', 'FRONTEND', 'DONE', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Максим Дмитриев', 'maxim@test.ru', '+7 900 567-89-01', 'Хочу следующий курс', 'BACKEND', 'DONE', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);