-- Tablas base (sin FKs)
CREATE TABLE company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(500),
    company_type VARCHAR(50)
);

CREATE TABLE profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE menu_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255),
    uri VARCHAR(500),
    position INT NOT NULL DEFAULT 0,
    type VARCHAR(50)
);

-- Tablas que dependen de company, profile, menu_item
CREATE TABLE profile_menu_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_menu_id BIGINT,
    profile_id BIGINT,
    CONSTRAINT fk_pmi_menu_item FOREIGN KEY (item_menu_id) REFERENCES menu_item(id),
    CONSTRAINT fk_pmi_profile FOREIGN KEY (profile_id) REFERENCES profile(id)
);

CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    gender VARCHAR(50),
    phone VARCHAR(100),
    email VARCHAR(255),
    company_id BIGINT,
    employee BOOLEAN NOT NULL DEFAULT FALSE,
    hour_price DECIMAL(19, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_customer_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE site (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(500),
    comments VARCHAR(1000),
    address VARCHAR(500),
    phone VARCHAR(100),
    company_id BIGINT,
    CONSTRAINT fk_site_company FOREIGN KEY (company_id) REFERENCES company(id)
);

-- user: tabla "users" para evitar palabra reservada en MySQL
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    customer_id BIGINT,
    profile_id BIGINT,
    site_id BIGINT,
    CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_user_profile FOREIGN KEY (profile_id) REFERENCES profile(id),
    CONSTRAINT fk_user_site FOREIGN KEY (site_id) REFERENCES site(id)
);

CREATE TABLE service_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(500)
);

CREATE TABLE service_sub_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(500),
    service_category_id BIGINT,
    CONSTRAINT fk_ssc_category FOREIGN KEY (service_category_id) REFERENCES service_category(id)
);

CREATE TABLE request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_category_id BIGINT,
    service_sub_category_id BIGINT,
    location VARCHAR(500),
    description VARCHAR(2000),
    site_id BIGINT,
    user_id BIGINT,
    request_status VARCHAR(50),
    create_date DATETIME,
    priority VARCHAR(50),
    CONSTRAINT fk_request_site FOREIGN KEY (site_id) REFERENCES site(id),
    CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_request_service_category FOREIGN KEY (service_category_id) REFERENCES service_category(id),
    CONSTRAINT fk_request_service_sub_category FOREIGN KEY (service_sub_category_id) REFERENCES service_sub_category(id)
);

CREATE TABLE request_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT,
    request_status VARCHAR(50),
    user_id BIGINT,
    rating BIGINT,
    comments VARCHAR(2000),
    create_date DATETIME,
    CONSTRAINT fk_rh_request FOREIGN KEY (request_id) REFERENCES request(id),
    CONSTRAINT fk_rh_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE request_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT,
    user_id BIGINT,
    CONSTRAINT fk_ra_request FOREIGN KEY (request_id) REFERENCES request(id),
    CONSTRAINT fk_ra_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE hour_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE,
    hour DECIMAL(19, 2),
    customer_id BIGINT,
    site_id BIGINT,
    CONSTRAINT fk_hr_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_hr_site FOREIGN KEY (site_id) REFERENCES site(id)
);
