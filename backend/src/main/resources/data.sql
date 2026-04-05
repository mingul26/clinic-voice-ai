-- Seed doctors (insert only if table is empty)
INSERT INTO doctor (name, specialization, available_slots)
SELECT 'Dr. Priya Sharma', 'Pathology',
       '["2024-04-10T09:00","2024-04-10T10:00","2024-04-10T11:00","2024-04-11T09:00","2024-04-11T14:00"]'
WHERE NOT EXISTS (SELECT 1 FROM doctor LIMIT 1);

INSERT INTO doctor (name, specialization, available_slots)
SELECT 'Dr. Arjun Mehta', 'Radiology',
       '["2024-04-10T10:00","2024-04-10T15:00","2024-04-11T10:00","2024-04-12T09:00"]'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE name = 'Dr. Arjun Mehta');

INSERT INTO doctor (name, specialization, available_slots)
SELECT 'Dr. Sunita Rao', 'General Medicine',
       '["2024-04-10T08:00","2024-04-10T09:00","2024-04-11T08:00","2024-04-12T11:00"]'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE name = 'Dr. Sunita Rao');

-- Seed patients
INSERT INTO patient (name, phone, language)
SELECT 'Ravi Kumar', '+919876543210', 'Hindi'
WHERE NOT EXISTS (SELECT 1 FROM patient LIMIT 1);

INSERT INTO patient (name, phone, language)
SELECT 'Meena Patel', '+919123456789', 'Gujarati'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE phone = '+919123456789');

INSERT INTO patient (name, phone, language)
SELECT 'Suresh Nair', '+919988776655', 'English'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE phone = '+919988776655');
