CREATE TABLE application (
                             id INT(11) NOT NULL AUTO_INCREMENT,
                             created_at DATETIME NOT NULL,
                             destroy_reason VARCHAR(255) DEFAULT NULL,
                             interview_result VARCHAR(255) DEFAULT NULL,
                             interview_result_note VARCHAR(255) DEFAULT NULL,
                             interview_time DATETIME DEFAULT NULL,
                             status VARCHAR(255) NOT NULL,
                             updated_at DATETIME DEFAULT NULL,
                             candidate_id INT(11) NOT NULL,
                             recruitment_position_id INT(11) NOT NULL,
                             cv_url VARCHAR(255) NOT NULL,
                             interview_confirmed TINYINT(1) NOT NULL DEFAULT 0,
                             PRIMARY KEY (id)
);
CREATE TABLE candidate (
                           id INT(11) NOT NULL AUTO_INCREMENT,
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL,
                           password VARCHAR(255) NOT NULL,
                           phone VARCHAR(255) DEFAULT NULL,
                           experience INT(11) DEFAULT NULL,
                           gender VARCHAR(255) DEFAULT NULL,
                           status VARCHAR(255) DEFAULT NULL,
                           description VARCHAR(255) DEFAULT NULL,
                           dob DATE DEFAULT NULL,
                           is_deleted TINYINT(1) DEFAULT 0,
                           created_at DATETIME NOT NULL,
                           updated_at DATETIME NOT NULL,
                           role VARCHAR(255) DEFAULT NULL,
                           PRIMARY KEY (id)
);
CREATE TABLE candidate_technology (
                                      candidate_id INT NOT NULL,
                                      technology_id INT NOT NULL,
                                      PRIMARY KEY (candidate_id, technology_id),
                                      FOREIGN KEY (candidate_id) REFERENCES candidate(id),
                                      FOREIGN KEY (technology_id) REFERENCES technology(id)
);
CREATE TABLE recruitment_position (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      created_date DATE NOT NULL,
                                      deleted_at DATE DEFAULT NULL,
                                      description VARCHAR(255),
                                      expired_date DATE DEFAULT NULL,
                                      is_deleted TINYINT(1) NOT NULL,
                                      max_salary DECIMAL(10,2) NOT NULL,
                                      min_experience INT NOT NULL,
                                      min_salary DECIMAL(10,2) NOT NULL,
                                      name VARCHAR(255) NOT NULL,
                                      category VARCHAR(255),
                                      location VARCHAR(255)
);
CREATE TABLE recruitment_position_technology (
                                                 recruitment_position_id INT NOT NULL,
                                                 technology_id INT NOT NULL,
                                                 PRIMARY KEY (recruitment_position_id, technology_id),
                                                 FOREIGN KEY (recruitment_position_id) REFERENCES recruitment_position(id),
                                                 FOREIGN KEY (technology_id) REFERENCES technology(id)
);

CREATE TABLE technology (
                            id INT(11) NOT NULL AUTO_INCREMENT,
                            name VARCHAR(255) NOT NULL,
                            is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                            deleted_at DATETIME DEFAULT NULL,
                            PRIMARY KEY (id)
);
