-- Insert sample data into users table for auth service
-- This matches the user data from user-management-service V2__insertInitialData.sql

INSERT INTO users (name, email, password, is_active, created_at)
VALUES 
    ('sadat', 'sadat@gmail.com', '$2a$10$rXwg//1CPd12gxGPFpC/yu3QCwY44SWplkvhKUDYQFC6BSO8p0haK', true, CURRENT_TIMESTAMP),
    ('riyad', 'riyad@gmail.com', '$2a$10$Zsk/ghxxz142M5/ZuVgjget5.APB7tzNlZKKO.Lq3FmdWS9JooCL2', true, CURRENT_TIMESTAMP),
    ('hossain', 'hossain@gmail.com', '$2a$10$GVZLuymconMfPbameqH/kOJ6r69ISqqa8VoqsuoBQpRfKkknOy6lW', true, CURRENT_TIMESTAMP),
    ('miraj', 'miraj@gmail.com', '$2a$10$6IZZ9wGm9DNsGw0ToOlBAucOJAHfjGNZtGv888YpOsShGJy116C02', true, CURRENT_TIMESTAMP),
    ('rafi', 'rafi@gmail.com', '$2a$10$gB.MrtYifbVcdC8e1XT1zObwEXbPUxiLv1D44.gCfZELw8tilp2uS', true, CURRENT_TIMESTAMP),
    ('anik', 'anik@gmail.com', '$2a$10$KPMiAHRKda5gEZnErIevw.6HiDVHiSNHxfG5Jb1HSEZRH8ppFwapy', true, CURRENT_TIMESTAMP),
    ('sawdho', 'sawdho@gmail.com', '$2a$10$8x9l2PA6mfqA2K3KovyfV.RpHKmv6LZX5qakQTZSw/gMvOVlP5/ti', true, CURRENT_TIMESTAMP),
    ('joy', 'joy@gmail.com', '$2a$10$vvpGKDciFbTOKGmDlTbaEe2KDXf9Cs6O5pThZKodrjkauV4LcxTM.', true, CURRENT_TIMESTAMP),
    ('kowsik', 'kowsik@gmail.com', '$2a$10$jHDfJ4xIQi8aZOV7LOy5yOWcxhwViW9a1SSpgod.WUuE2IhxYf0oq', true, CURRENT_TIMESTAMP),
    ('adnan', 'abnan@gmail.com', '$2a$10$5I4GAvjeNShCCosSvxXNfO4RV4SoHK00PtavvqpW0e.yqi4of.HvC', true, CURRENT_TIMESTAMP),
    ('toufiq', 'toufiq@gmail.com', '$2a$10$wT0KqtOnGG6.XmSeMlZnyOmyUfs.CraOto.ZelL/lB6b9C6Cl5icW', true, CURRENT_TIMESTAMP),
    ('afzal', 'afzal@gmail.com', '$2a$10$09oVTIUc4Fbqveu1zmiEzuHQYknzckG0zQyGdAk1DgJ51c70I/YCG', true, CURRENT_TIMESTAMP),
    ('turjoy', 'turjoy@gmail.com', '$2a$10$5ZimHk8QjnCUv34FMfLe9.CJ1nZbgm6IM78iI5OYji6U3nJJRfwq6', true, CURRENT_TIMESTAMP),
    ('amim', 'amim@gmail.com', '$2a$10$/clgMMoshoztzDUOrqrIXeZduwt52Knh313Ap9qLtQruznDW8qUrC', true, CURRENT_TIMESTAMP),
    ('sawpnil', 'sawpnil@gmail.com', '$2a$10$aht.Tro9trJNGJqFe8lHl.fPYaMpJmcW0XNC7JaXKOcOWjfHbQFDW', true, CURRENT_TIMESTAMP),
    ('sagor', 'sagor@gmail.com', '$2a$10$96oa8bYvGKJ/kMMiGBj3tOSc2RgEGR.GUJbyAzCpf5eMiWxZjJV82', true, CURRENT_TIMESTAMP),
    ('soumik', 'soumik@gmail.com', '$2a$10$5vBn9feRkCwGEyqdbm6PEuz7zwv5.RXygzT3i5Y.i1Lo..AMSt5Wi', true, CURRENT_TIMESTAMP),
    ('jakaria', 'jakaria@gmail.com', '$2a$10$brqeIZsSSGDC3VlrftDHG.9RQima6T2IrhiQK.Gpw42H6ZUitbq0W', true, CURRENT_TIMESTAMP),
    ('ehsan', 'ehsan@gmail.com', '$2a$10$g5AJTnbPo.bCrVExxW5fbOZ5epOoI/5rI7r8Epm1hbUKqemWQ4zce', true, CURRENT_TIMESTAMP),
    ('munzeer', 'munzeer@gmail.com', '$2a$10$9Pfy.vpfBQR1hHUSS4gZkeP/.YbdQTF8kY8/B35fh0zBaUnhDDcBW', true, CURRENT_TIMESTAMP),
    ('turad', 'turad@gmail.com', '$2a$10$.SwvK1kPkCgmZvmHrG0ROuh2KykZqqYXRa6Z1FQIHfrBsBAk12Wtq', true, CURRENT_TIMESTAMP);
