CREATE TABLE request_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_request_attachment_request FOREIGN KEY (request_id) REFERENCES request(id),
    INDEX idx_request_attachment_request_id (request_id)
);
