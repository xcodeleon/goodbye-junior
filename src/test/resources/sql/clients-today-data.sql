INSERT INTO clients_db (id, name, email, phone, message, course_type, status, priority, source, created_at, updated_at) VALUES
-- NEW (5 записей)
(gen_random_uuid(), 'Анна Петрова', 'anna.petrov@gmail.com', '+7 912 345-67-89', 'Хочу записаться на Fullstack', 'FULLSTACK', 'NEW', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Дмитрий Сидоров', 'dmitry.sidorov@yandex.ru', '+7 913 456-78-90', 'Интересует курс Frontend', 'FRONTEND', 'NEW', 'MEDIUM', 'Яндекс.Директ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Екатерина Иванова', 'ekaterina.ivanova@mail.ru', '+7 914 567-89-01', 'Вопросы по оплате', 'BACKEND', 'NEW', 'HIGH', 'Соцсети', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Алексей Козлов', 'alexey.kozlov@rambler.ru', '+7 915 678-90-12', 'Нужна консультация', 'FULLSTACK', 'NEW', 'LOW', 'Рекомендация', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Мария Смирнова', 'maria.smirnova@gmail.com', '+7 916 789-01-23', 'Сколько длится курс?', 'FRONTEND', 'NEW', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- PROCESSED (4 записи)
(gen_random_uuid(), 'Павел Николаев', 'pavel.nikolaev@yandex.ru', '+7 917 890-12-34', 'Оплатила курс', 'BACKEND', 'PROCESSED', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ольга Морозова', 'olga.morozova@mail.ru', '+7 918 901-23-45', 'Прислать программу курса', 'FULLSTACK', 'PROCESSED', 'MEDIUM', 'Яндекс.Директ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Татьяна Васильева', 'tatiana.vasilieva@yandex.ru', '+7 920 123-45-67', 'Не пришло письмо с доступом', 'FRONTEND', 'PROCESSED', 'HIGH', 'Соцсети', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Игорь Федоров', 'igor.fedorov@mail.ru', '+7 921 234-56-78', 'Смена формата обучения', 'BACKEND', 'PROCESSED', 'MEDIUM', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- DONE (3 записи)
(gen_random_uuid(), 'Наталья Ковалева', 'natalya.kovaleva@gmail.com', '+7 922 345-67-89', 'Спасибо за курс!', 'FULLSTACK', 'DONE', 'HIGH', 'Лендинг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Виктор Белов', 'viktor.bелов@yandex.ru', '+7 923 456-78-90', 'Отличный преподаватель', 'FRONTEND', 'DONE', 'LOW', 'Рекомендация', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Юлия Григорьева', 'yulia.grigoryeva@mail.ru', '+7 924 567-89-01', 'Рекомендую вашу школу', 'BACKEND', 'DONE', 'MEDIUM', 'Яндекс.Директ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);