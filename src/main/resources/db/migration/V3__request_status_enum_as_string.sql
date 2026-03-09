-- Fix request_status stored as digit (ordinal) to enum name (STRING)
-- So existing rows work with @Enumerated(EnumType.STRING)
UPDATE request SET request_status = 'PENDING_APPROVAL' WHERE request_status = '0';
UPDATE request SET request_status = 'CREATED' WHERE request_status = '1';
UPDATE request SET request_status = 'ASSIGNED' WHERE request_status = '2';
UPDATE request SET request_status = 'IN_TRANSIT' WHERE request_status = '3';
UPDATE request SET request_status = 'DONE' WHERE request_status = '4';
UPDATE request SET request_status = 'RATED' WHERE request_status = '5';
UPDATE request SET request_status = 'REJECTED' WHERE request_status = '6';

UPDATE request_history SET request_status = 'PENDING_APPROVAL' WHERE request_status = '0';
UPDATE request_history SET request_status = 'CREATED' WHERE request_status = '1';
UPDATE request_history SET request_status = 'ASSIGNED' WHERE request_status = '2';
UPDATE request_history SET request_status = 'IN_TRANSIT' WHERE request_status = '3';
UPDATE request_history SET request_status = 'DONE' WHERE request_status = '4';
UPDATE request_history SET request_status = 'RATED' WHERE request_status = '5';
UPDATE request_history SET request_status = 'REJECTED' WHERE request_status = '6';
