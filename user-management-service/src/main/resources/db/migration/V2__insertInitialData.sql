-- Insert dummy data into users
INSERT INTO users (name, email, password)
VALUES 
    ('sadat', 'sadat@gmail.com', '$2a$10$rXwg//1CPd12gxGPFpC/yu3QCwY44SWplkvhKUDYQFC6BSO8p0haK'),
    ('riyad', 'riyad@gmail.com', '$2a$10$Zsk/ghxxz142M5/ZuVgjget5.APB7tzNlZKKO.Lq3FmdWS9JooCL2'),
    ('hossain', 'hossain@gmail.com', '$2a$10$GVZLuymconMfPbameqH/kOJ6r69ISqqa8VoqsuoBQpRfKkknOy6lW');

-- Insert data with all columns except id
INSERT INTO users (
    name, email, password, profile_picture_url, profile_picture_data, designation, birthdate, join_date, predicted_burnout_risk
) VALUES
    ('miraj', 'miraj@gmail.com', '$2a$10$6IZZ9wGm9DNsGw0ToOlBAucOJAHfjGNZtGv888YpOsShGJy116C02', NULL, NULL, NULL, NULL, NULL, NULL),
    ('rafi', 'rafi@gmail.com', '$2a$10$gB.MrtYifbVcdC8e1XT1zObwEXbPUxiLv1D44.gCfZELw8tilp2uS', NULL, NULL, NULL, NULL, NULL, NULL),
    ('anik', 'anik@gmail.com', '$2a$10$KPMiAHRKda5gEZnErIevw.6HiDVHiSNHxfG5Jb1HSEZRH8ppFwapy', NULL, NULL, NULL, NULL, NULL, NULL),
    ('sawdho', 'sawdho@gmail.com', '$2a$10$8x9l2PA6mfqA2K3KovyfV.RpHKmv6LZX5qakQTZSw/gMvOVlP5/ti', NULL, NULL, NULL, NULL, NULL, NULL),
    ('joy', 'joy@gmail.com', '$2a$10$vvpGKDciFbTOKGmDlTbaEe2KDXf9Cs6O5pThZKodrjkauV4LcxTM.', NULL, NULL, NULL, NULL, NULL, NULL),
    ('kowsik', 'kowsik@gmail.com', '$2a$10$jHDfJ4xIQi8aZOV7LOy5yOWcxhwViW9a1SSpgod.WUuE2IhxYf0oq', NULL, NULL, NULL, NULL, NULL, NULL),
    ('adnan', 'abnan@gmail.com', '$2a$10$5I4GAvjeNShCCosSvxXNfO4RV4SoHK00PtavvqpW0e.yqi4of.HvC', NULL, NULL, NULL, NULL, NULL, NULL),
    ('toufiq', 'toufiq@gmail.com', '$2a$10$wT0KqtOnGG6.XmSeMlZnyOmyUfs.CraOto.ZelL/lB6b9C6Cl5icW', NULL, NULL, NULL, NULL, NULL, NULL),
    ('afzal', 'afzal@gmail.com', '$2a$10$09oVTIUc4Fbqveu1zmiEzuHQYknzckG0zQyGdAk1DgJ51c70I/YCG', NULL, NULL, NULL, NULL, NULL, NULL),
    ('turjoy', 'turjoy@gmail.com', '$2a$10$5ZimHk8QjnCUv34FMfLe9.CJ1nZbgm6IM78iI5OYji6U3nJJRfwq6', NULL, NULL, NULL, NULL, NULL, NULL),
    ('amim', 'amim@gmail.com', '$2a$10$/clgMMoshoztzDUOrqrIXeZduwt52Knh313Ap9qLtQruznDW8qUrC', NULL, NULL, NULL, NULL, NULL, NULL),
    ('sawpnil', 'sawpnil@gmail.com', '$2a$10$aht.Tro9trJNGJqFe8lHl.fPYaMpJmcW0XNC7JaXKOcOWjfHbQFDW', NULL, NULL, NULL, NULL, NULL, NULL),
    ('sagor', 'sagor@gmail.com', '$2a$10$96oa8bYvGKJ/kMMiGBj3tOSc2RgEGR.GUJbyAzCpf5eMiWxZjJV82', NULL, NULL, NULL, NULL, NULL, NULL),
    ('soumik', 'soumik@gmail.com', '$2a$10$5vBn9feRkCwGEyqdbm6PEuz7zwv5.RXygzT3i5Y.i1Lo..AMSt5Wi', NULL, NULL, NULL, NULL, NULL, NULL),
    ('jakaria', 'jakaria@gmail.com', '$2a$10$brqeIZsSSGDC3VlrftDHG.9RQima6T2IrhiQK.Gpw42H6ZUitbq0W', NULL, NULL, NULL, NULL, NULL, NULL),
    ('ehsan', 'ehsan@gmail.com', '$2a$10$g5AJTnbPo.bCrVExxW5fbOZ5epOoI/5rI7r8Epm1hbUKqemWQ4zce', NULL, NULL, NULL, NULL, NULL, NULL),
    ('munzeer', 'munzeer@gmail.com', '$2a$10$9Pfy.vpfBQR1hHUSS4gZkeP/.YbdQTF8kY8/B35fh0zBaUnhDDcBW', NULL, NULL, NULL, NULL, NULL, NULL),
    ('turad', 'turad@gmail.com', '$2a$10$.SwvK1kPkCgmZvmHrG0ROuh2KykZqqYXRa6Z1FQIHfrBsBAk12Wtq', NULL, NULL, NULL, NULL, NULL, NULL),
    ('test user', 'a@b.com', '$2a$10$rXwg//1CPd12gxGPFpC/yu3QCwY44SWplkvhKUDYQFC6BSO8p0haK', NULL, NULL, 'Software Engineer', '1990-01-01', '2020-01-01', false);