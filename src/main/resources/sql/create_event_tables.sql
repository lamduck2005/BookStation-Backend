-- Tạo bảng event_category
CREATE TABLE event_category (
    id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(255) NOT NULL,
    description NTEXT,
    icon_url NVARCHAR(500),
    is_active BIT DEFAULT 1,
    created_at BIGINT
);

-- Tạo bảng event
CREATE TABLE event (
    id INT IDENTITY(1,1) PRIMARY KEY,
    event_name NVARCHAR(255) NOT NULL,
    description NTEXT,
    event_type VARCHAR(50),
    event_category_id INT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    start_date BIGINT,
    end_date BIGINT,
    max_participants INT,
    current_participants INT DEFAULT 0,
    image_url NVARCHAR(500),
    location NVARCHAR(500),
    rules NTEXT,
    is_online BIT DEFAULT 0,
    created_at BIGINT,
    updated_at BIGINT,
    created_by INT,
    FOREIGN KEY (event_category_id) REFERENCES event_category(id),
    FOREIGN KEY (created_by) REFERENCES [user](id)
);

-- Tạo bảng event_gift
CREATE TABLE event_gift (
    id INT IDENTITY(1,1) PRIMARY KEY,
    event_id INT NOT NULL,
    gift_name NVARCHAR(255) NOT NULL,
    description NTEXT,
    gift_value DECIMAL(10,2),
    quantity INT DEFAULT 1,
    remaining_quantity INT,
    image_url NVARCHAR(500),
    gift_type NVARCHAR(100), -- BOOK, VOUCHER, POINT, PHYSICAL_ITEM
    book_id INT,
    voucher_id INT,
    point_value INT,
    is_active BIT DEFAULT 1,
    created_at BIGINT,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (voucher_id) REFERENCES voucher(id)
);

-- Tạo bảng event_participant
CREATE TABLE event_participant (
    id INT IDENTITY(1,1) PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at BIGINT,
    is_winner BIT DEFAULT 0,
    gift_received_id INT,
    gift_claimed_at BIGINT,
    completion_status VARCHAR(20) DEFAULT 'JOINED', -- JOINED, COMPLETED, WINNER, CLAIMED
    notes NTEXT,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES [user](id),
    FOREIGN KEY (gift_received_id) REFERENCES event_gift(id),
    UNIQUE(event_id, user_id) -- Đảm bảo mỗi user chỉ tham gia 1 lần cho mỗi event
);

-- Tạo bảng event_gift_claim (phiên bản linh hoạt hỗ trợ đa phương thức)
CREATE TABLE event_gift_claim (
    id INT IDENTITY(1,1) PRIMARY KEY,
    event_participant_id INT NOT NULL,
    event_gift_id INT NOT NULL,
    claimed_at BIGINT,
    claim_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, ORDER_CREATED, DELIVERED, REJECTED, EXPIRED
    delivery_method VARCHAR(20) DEFAULT 'ONLINE_SHIPPING', -- ONLINE_SHIPPING, STORE_PICKUP, DIGITAL_DELIVERY, DIRECT_HANDOVER
    delivery_order_id INT, -- Chỉ dùng khi delivery_method = ONLINE_SHIPPING
    store_pickup_code VARCHAR(50), -- Mã nhận quà tại cửa hàng
    pickup_store_id INT, -- ID cửa hàng để nhận quà
    staff_confirmed_by INT, -- ID nhân viên xác nhận trao quà
    auto_delivered BIT DEFAULT 0, -- true cho quà số (voucher, điểm)
    completed_at BIGINT, -- Thời điểm hoàn thành việc nhận quà
    notes NTEXT,
    FOREIGN KEY (event_participant_id) REFERENCES event_participant(id) ON DELETE CASCADE,
    FOREIGN KEY (event_gift_id) REFERENCES event_gift(id),
    FOREIGN KEY (delivery_order_id) REFERENCES [order](id),
    FOREIGN KEY (staff_confirmed_by) REFERENCES [user](id) -- Assuming staff are also users
);

-- Tạo bảng event_history
CREATE TABLE event_history (
    id INT IDENTITY(1,1) PRIMARY KEY,
    event_id INT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    description NTEXT,
    performed_by INT,
    created_at BIGINT,
    old_values NTEXT,
    new_values NTEXT,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES [user](id)
);

-- Tạo các index để tối ưu hiệu suất
CREATE INDEX IX_event_status ON event(status);
CREATE INDEX IX_event_start_date ON event(start_date);
CREATE INDEX IX_event_end_date ON event(end_date);
CREATE INDEX IX_event_category ON event(event_category_id);
CREATE INDEX IX_event_participant_event ON event_participant(event_id);
CREATE INDEX IX_event_participant_user ON event_participant(user_id);
CREATE INDEX IX_event_gift_event ON event_gift(event_id);
