package org.datn.bookstation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.*;
import org.datn.bookstation.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final UserRankRepository userRankRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final SupplierRepository supplierRepository;
    private final BookRepository bookRepository;
    private final AuthorBookRepository authorBookRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventRepository eventRepository;
    private final EventGiftRepository eventGiftRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PointRepository pointRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("Starting data initialization...");
            
            // Kiểm tra từng loại dữ liệu và chỉ khởi tạo nếu chưa có
            initializeIfEmpty();

            // Hiển thị trạng thái dữ liệu sau khi hoàn thành
            checkDataStatus();
            
            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during data initialization: ", e);
        }
    }

    private void initializeIfEmpty() {
        // Kiểm tra và khởi tạo Roles
        if (roleRepository.count() == 0) {
            initializeRoles();
        } else {
            log.info("Roles already exist, skipping role initialization.");
        }

        // Kiểm tra và khởi tạo Ranks
        if (rankRepository.count() == 0) {
            initializeRanks();
        } else {
            log.info("Ranks already exist, skipping rank initialization.");
        }

        // Kiểm tra và khởi tạo Users
        if (userRepository.count() == 0) {
            initializeUsers();
        } else {
            log.info("Users already exist, skipping user initialization.");
        }

        // Kiểm tra và khởi tạo UserRanks
        if (userRankRepository.count() == 0) {
            initializeUserRanks();
        } else {
            log.info("User ranks already exist, skipping user rank initialization.");
        }

        // Kiểm tra và khởi tạo Categories
        if (categoryRepository.count() == 0) {
            initializeCategories();
        } else {
            log.info("Categories already exist, skipping category initialization.");
        }

        // Kiểm tra và khởi tạo Authors
        if (authorRepository.count() == 0) {
            initializeAuthors();
        } else {
            log.info("Authors already exist, skipping author initialization.");
        }

        // Kiểm tra và khởi tạo Suppliers
        if (supplierRepository.count() == 0) {
            initializeSuppliers();
        } else {
            log.info("Suppliers already exist, skipping supplier initialization.");
        }

        // Kiểm tra và khởi tạo Books
        if (bookRepository.count() == 0) {
            initializeBooks();
        } else {
            log.info("Books already exist, skipping book initialization.");
        }

        // Kiểm tra và khởi tạo AuthorBooks
        if (authorBookRepository.count() == 0) {
            initializeAuthorBooks();
        } else {
            log.info("Author-book relationships already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Vouchers
        if (voucherRepository.count() == 0) {
            initializeVouchers();
        } else {
            log.info("Vouchers already exist, skipping voucher initialization.");
        }

        // Kiểm tra và khởi tạo UserVouchers
        if (userVoucherRepository.count() == 0) {
            initializeUserVouchers();
        } else {
            log.info("User vouchers already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo FlashSales
        if (flashSaleRepository.count() == 0) {
            initializeFlashSales();
        } else {
            log.info("Flash sales already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo EventCategories
        if (eventCategoryRepository.count() == 0) {
            initializeEventCategories();
        } else {
            log.info("Event categories already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Events
        if (eventRepository.count() == 0) {
            initializeEvents();
        } else {
            log.info("Events already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo EventGifts
        if (eventGiftRepository.count() == 0) {
            initializeEventGifts();
        } else {
            log.info("Event gifts already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Addresses
        if (addressRepository.count() == 0) {
            initializeAddresses();
        } else {
            log.info("Addresses already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Carts
        if (cartRepository.count() == 0) {
            initializeCarts();
        } else {
            log.info("Carts already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Orders
        if (orderRepository.count() == 0) {
            initializeOrders();
        } else {
            log.info("Orders already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Points
        if (pointRepository.count() == 0) {
            initializePoints();
        } else {
            log.info("Points already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo Reviews
        if (reviewRepository.count() == 0) {
            initializeReviews();
        } else {
            log.info("Reviews already exist, skipping initialization.");
        }

        // Kiểm tra và khởi tạo EventParticipants
        if (eventParticipantRepository.count() == 0) {
            initializeEventParticipants();
        } else {
            log.info("Event participants already exist, skipping initialization.");
        }
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        List<Role> roles = Arrays.asList(
            createRole("ADMIN", "Quản trị viên hệ thống"),
            createRole("STAFF", "Nhân viên"),
            createRole("CUSTOMER", "Khách hàng")
        );
        roleRepository.saveAll(roles);
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setRoleName(name);
        role.setDescription(description);
        role.setStatus((byte) 1);
        return role;
    }

    private void initializeRanks() {
        log.info("Initializing ranks...");
        List<Rank> ranks = Arrays.asList(
            createRank("BRONZE", BigDecimal.ZERO, new BigDecimal("1.0")),
            createRank("SILVER", new BigDecimal("1000000"), new BigDecimal("1.2")),
            createRank("GOLD", new BigDecimal("5000000"), new BigDecimal("1.5")),
            createRank("PLATINUM", new BigDecimal("10000000"), new BigDecimal("2.0"))
        );
        rankRepository.saveAll(ranks);
    }

    private Rank createRank(String name, BigDecimal minSpent, BigDecimal pointMultiplier) {
        Rank rank = new Rank();
        rank.setRankName(name);
        rank.setMinSpent(minSpent);
        rank.setPointMultiplier(pointMultiplier);
        rank.setCreatedAt(System.currentTimeMillis());
        rank.setStatus((byte) 1);
        return rank;
    }

    private void initializeUsers() {
        log.info("Initializing users...");
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
        Role staffRole = roleRepository.findByRoleName("STAFF").orElse(null);
        Role customerRole = roleRepository.findByRoleName("CUSTOMER").orElse(null);

        List<User> users = Arrays.asList(
            createUser("admin@bookstation.com", "admin123", "Admin BookStation", adminRole),
            createUser("staff1@bookstation.com", "staff123", "Nguyễn Văn A", staffRole),
            createUser("staff2@bookstation.com", "staff123", "Trần Thị B", staffRole),
            createUser("customer1@gmail.com", "customer123", "Lê Văn C", customerRole),
            createUser("customer2@gmail.com", "customer123", "Phạm Thị D", customerRole),
            createUser("customer3@gmail.com", "customer123", "Hoàng Văn E", customerRole),
            createUser("customer4@gmail.com", "customer123", "Ngô Thị F", customerRole),
            createUser("customer5@gmail.com", "customer123", "Vũ Văn G", customerRole)
        );
        userRepository.saveAll(users);
    }

    private User createUser(String email, String password, String fullName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setPhoneNumber("0123456789");
        user.setStatus((byte) 1);
        user.setTotalPoint(0);
        user.setTotalSpent(BigDecimal.ZERO);
        return user;
    }

    private void initializeUserRanks() {
        log.info("Initializing user ranks...");
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        Rank bronzeRank = rankRepository.findByRankName("BRONZE").orElse(null);
        
        for (User customer : customers) {
            UserRank userRank = new UserRank();
            userRank.setUser(customer);
            userRank.setRank(bronzeRank);
            userRank.setStatus((byte) 1);
            userRank.setCreatedAt(System.currentTimeMillis());
            userRankRepository.save(userRank);
        }
    }

    private void initializeCategories() {
        log.info("Initializing categories...");
        
        // Tạo danh mục cha
        Category fiction = createCategory("Tiểu thuyết", "Các tác phẩm tiểu thuyết", null, 1);
        Category nonFiction = createCategory("Phi tiểu thuyết", "Sách phi tiểu thuyết", null, 1);
        Category education = createCategory("Giáo dục", "Sách giáo dục và học tập", null, 1);
        Category children = createCategory("Thiếu nhi", "Sách dành cho trẻ em", null, 1);
        Category business = createCategory("Kinh doanh", "Sách về kinh doanh và quản lý", null, 1);
        
        categoryRepository.saveAll(Arrays.asList(fiction, nonFiction, education, children, business));
        
        // Tạo danh mục con
        List<Category> subCategories = Arrays.asList(
            createCategory("Trinh thám", "Tiểu thuyết trinh thám", fiction, 1),
            createCategory("Lãng mạn", "Tiểu thuyết lãng mạn", fiction, 1),
            createCategory("Khoa học viễn tưởng", "Tiểu thuyết khoa học viễn tưởng", fiction, 1),
            createCategory("Lịch sử", "Sách về lịch sử", nonFiction, 1),
            createCategory("Khoa học", "Sách khoa học", nonFiction, 1),
            createCategory("Tự truyện", "Sách tự truyện", nonFiction, 1),
            createCategory("Sách giáo khoa", "Sách giáo khoa các cấp", education, 1),
            createCategory("Từ điển", "Từ điển các loại", education, 1),
            createCategory("Truyện tranh", "Truyện tranh thiếu nhi", children, 1),
            createCategory("Sách tô màu", "Sách tô màu cho trẻ", children, 1),
            createCategory("Marketing", "Sách về marketing", business, 1),
            createCategory("Quản lý", "Sách về quản lý", business, 1)
        );
        categoryRepository.saveAll(subCategories);
    }

    private Category createCategory(String name, String description, Category parent, Integer createdBy) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(description);
        category.setParentCategory(parent);
        category.setCreatedBy(createdBy);
        category.setStatus((byte) 1);
        return category;
    }

    private void initializeAuthors() {
        log.info("Initializing authors...");
        List<Author> authors = Arrays.asList(
            createAuthor("Nguyễn Nhật Ánh", "Nhà văn Việt Nam nổi tiếng với các tác phẩm thiếu nhi", LocalDate.of(1955, 5, 7)),
            createAuthor("Tô Hoài", "Nhà văn Việt Nam với tác phẩm Dế Mèn phiêu lưu ký", LocalDate.of(1920, 9, 27)),
            createAuthor("Nam Cao", "Nhà văn hiện thực Việt Nam", LocalDate.of(1915, 10, 29)),
            createAuthor("Vũ Trọng Phung", "Nhà văn hiện thực phê phán", LocalDate.of(1912, 10, 20)),
            createAuthor("Haruki Murakami", "Tiểu thuyết gia Nhật Bản", LocalDate.of(1949, 1, 12)),
            createAuthor("Agatha Christie", "Nữ hoàng trinh thám", LocalDate.of(1890, 9, 15)),
            createAuthor("Stephen King", "Vua tiểu thuyết kinh dị", LocalDate.of(1947, 9, 21)),
            createAuthor("J.K. Rowling", "Tác giả Harry Potter", LocalDate.of(1965, 7, 31)),
            createAuthor("Dale Carnegie", "Tác giả sách self-help", LocalDate.of(1888, 11, 24)),
            createAuthor("Napoleon Hill", "Tác giả Think and Grow Rich", LocalDate.of(1883, 10, 26))
        );
        authorRepository.saveAll(authors);
    }

    private Author createAuthor(String name, String biography, LocalDate birthDate) {
        Author author = new Author();
        author.setAuthorName(name);
        author.setBiography(biography);
        author.setBirthDate(birthDate);
        author.setCreatedBy(1);
        author.setStatus((byte) 1);
        return author;
    }

    private void initializeSuppliers() {
        log.info("Initializing suppliers...");
        List<Supplier> suppliers = Arrays.asList(
            createSupplier("NXB Kim Đồng", "Nguyễn Văn A", "0123456789", "kimDong@publisher.vn", "Hà Nội"),
            createSupplier("NXB Trẻ", "Trần Thị B", "0123456788", "tre@publisher.vn", "TP.HCM"),
            createSupplier("NXB Văn học", "Lê Văn C", "0123456787", "vanhoc@publisher.vn", "Hà Nội"),
            createSupplier("NXB Giáo dục", "Phạm Thị D", "0123456786", "giaoduc@publisher.vn", "Hà Nội"),
            createSupplier("Fahasa", "Hoàng Văn E", "0123456785", "fahasa@bookstore.vn", "TP.HCM"),
            createSupplier("Vinabook", "Ngô Thị F", "0123456784", "vinabook@bookstore.vn", "Hà Nội")
        );
        supplierRepository.saveAll(suppliers);
    }

    private Supplier createSupplier(String name, String contactName, String phone, String email, String address) {
        Supplier supplier = new Supplier();
        supplier.setSupplierName(name);
        supplier.setContactName(contactName);
        supplier.setPhoneNumber(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        supplier.setStatus((byte) 1);
        supplier.setCreatedBy("system"); // Required field
        return supplier;
    }

    private void initializeBooks() {
        log.info("Initializing books...");
        
        List<Category> categories = categoryRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        
        List<Book> books = Arrays.asList(
            createBook("Tôi thấy hoa vàng trên cỏ xanh", "Tiểu thuyết của Nguyễn Nhật Ánh", 
                new BigDecimal("85000"), 100, LocalDate.of(2010, 1, 1), 
                findCategoryByName(categories, "Tiểu thuyết"), suppliers.get(0), 1),
            createBook("Dế Mèn phiêu lưu ký", "Tác phẩm kinh điển của Tô Hoài", 
                new BigDecimal("65000"), 150, LocalDate.of(1941, 1, 1), 
                findCategoryByName(categories, "Thiếu nhi"), suppliers.get(0), 1),
            createBook("Chí Phèo", "Truyện ngắn của Nam Cao", 
                new BigDecimal("45000"), 200, LocalDate.of(1941, 1, 1), 
                findCategoryByName(categories, "Tiểu thuyết"), suppliers.get(2), 1),
            createBook("Norwegian Wood", "Tiểu thuyết của Haruki Murakami", 
                new BigDecimal("120000"), 80, LocalDate.of(1987, 1, 1), 
                findCategoryByName(categories, "Lãng mạn"), suppliers.get(1), 1),
            createBook("Murder on the Orient Express", "Tiểu thuyết trinh thám của Agatha Christie", 
                new BigDecimal("95000"), 90, LocalDate.of(1934, 1, 1), 
                findCategoryByName(categories, "Trinh thám"), suppliers.get(4), 1),
            createBook("Harry Potter và Hòn đá Phù thủy", "Tập 1 series Harry Potter", 
                new BigDecimal("150000"), 120, LocalDate.of(1997, 6, 26), 
                findCategoryByName(categories, "Khoa học viễn tưởng"), suppliers.get(1), 1),
            createBook("Đắc Nhân Tâm", "Sách self-help của Dale Carnegie", 
                new BigDecimal("89000"), 300, LocalDate.of(1936, 1, 1), 
                findCategoryByName(categories, "Kinh doanh"), suppliers.get(4), 1),
            createBook("Think and Grow Rich", "Sách về thành công của Napoleon Hill", 
                new BigDecimal("79000"), 180, LocalDate.of(1937, 1, 1), 
                findCategoryByName(categories, "Kinh doanh"), suppliers.get(5), 1),
            createBook("Toán học lớp 12", "Sách giáo khoa Toán 12", 
                new BigDecimal("25000"), 500, LocalDate.of(2020, 1, 1), 
                findCategoryByName(categories, "Sách giáo khoa"), suppliers.get(3), 1),
            createBook("Từ điển Anh - Việt", "Từ điển Anh Việt cơ bản", 
                new BigDecimal("135000"), 250, LocalDate.of(2018, 1, 1), 
                findCategoryByName(categories, "Từ điển"), suppliers.get(3), 1),
            createBook("Doraemon tập 1", "Truyện tranh Doraemon", 
                new BigDecimal("18000"), 400, LocalDate.of(1970, 1, 1), 
                findCategoryByName(categories, "Truyện tranh"), suppliers.get(0), 1),
            createBook("Marketing 4.0", "Sách về marketing hiện đại", 
                new BigDecimal("189000"), 100, LocalDate.of(2017, 1, 1), 
                findCategoryByName(categories, "Marketing"), suppliers.get(4), 1)
        );
        bookRepository.saveAll(books);
    }

    private Book createBook(String name, String description, BigDecimal price, Integer stock, 
                          LocalDate publicationDate, Category category, Supplier supplier, Integer createdBy) {
        Book book = new Book();
        book.setBookName(name);
        book.setDescription(description);
        book.setPrice(price);
        book.setStockQuantity(stock);
        book.setPublicationDate(publicationDate);
        book.setCategory(category);
        book.setSupplier(supplier);
        book.setCreatedBy(createdBy);
        book.setBookCode("BOOK" + System.currentTimeMillis());
        book.setStatus((byte) 1);
        return book;
    }

    private Category findCategoryByName(List<Category> categories, String name) {
        return categories.stream()
                .filter(cat -> cat.getCategoryName().equals(name))
                .findFirst()
                .orElse(categories.get(0));
    }

    private void initializeAuthorBooks() {
        log.info("Initializing author-book relationships...");
        
        List<Book> books = bookRepository.findAll();
        List<Author> authors = authorRepository.findAll();
        
        // Tạo mối quan hệ tác giả - sách
        createAuthorBook(findBookByName(books, "Tôi thấy hoa vàng trên cỏ xanh"), findAuthorByName(authors, "Nguyễn Nhật Ánh"));
        createAuthorBook(findBookByName(books, "Dế Mèn phiêu lưu ký"), findAuthorByName(authors, "Tô Hoài"));
        createAuthorBook(findBookByName(books, "Chí Phèo"), findAuthorByName(authors, "Nam Cao"));
        createAuthorBook(findBookByName(books, "Norwegian Wood"), findAuthorByName(authors, "Haruki Murakami"));
        createAuthorBook(findBookByName(books, "Murder on the Orient Express"), findAuthorByName(authors, "Agatha Christie"));
        createAuthorBook(findBookByName(books, "Harry Potter và Hòn đá Phù thủy"), findAuthorByName(authors, "J.K. Rowling"));
        createAuthorBook(findBookByName(books, "Đắc Nhân Tâm"), findAuthorByName(authors, "Dale Carnegie"));
        createAuthorBook(findBookByName(books, "Think and Grow Rich"), findAuthorByName(authors, "Napoleon Hill"));
    }

    private void createAuthorBook(Book book, Author author) {
        if (book != null && author != null) {
            AuthorBook authorBook = new AuthorBook();
            AuthorBookId id = new AuthorBookId();
            id.setBookId(book.getId());
            id.setAuthorId(author.getId());
            authorBook.setId(id);
            authorBook.setBook(book);
            authorBook.setAuthor(author);
            authorBookRepository.save(authorBook);
        }
    }

    private Book findBookByName(List<Book> books, String name) {
        return books.stream()
                .filter(book -> book.getBookName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Author findAuthorByName(List<Author> authors, String name) {
        return authors.stream()
                .filter(author -> author.getAuthorName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void initializeVouchers() {
        log.info("Initializing vouchers...");
        
        long currentTime = System.currentTimeMillis();
        long oneMonth = 30L * 24 * 60 * 60 * 1000; // 30 ngày
        
        List<Voucher> vouchers = Arrays.asList(
            createVoucher("WELCOME10", "Voucher chào mừng", "Giảm 10% cho đơn hàng đầu tiên", 
                VoucherType.PERCENTAGE, new BigDecimal("10"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("100000"), new BigDecimal("50000"), 100, 1, "admin"),
            createVoucher("SAVE50K", "Voucher giảm 50K", "Giảm 50.000đ cho đơn từ 500K", 
                VoucherType.FIXED_AMOUNT, null, new BigDecimal("50000"), 
                currentTime, currentTime + oneMonth, new BigDecimal("500000"), null, 50, 1, "admin"),
            createVoucher("FREESHIP", "Miễn phí vận chuyển", "Miễn phí ship cho đơn từ 200K", 
                VoucherType.FREE_SHIPPING, null, null, 
                currentTime, currentTime + oneMonth, new BigDecimal("200000"), null, 200, 1, "admin"),
            createVoucher("SUMMER20", "Voucher hè", "Giảm 20% tối đa 100K", 
                VoucherType.PERCENTAGE, new BigDecimal("20"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("300000"), new BigDecimal("100000"), 75, 1, "admin"),
            createVoucher("NEWBIE15", "Voucher thành viên mới", "Giảm 15% cho khách hàng mới", 
                VoucherType.PERCENTAGE, new BigDecimal("15"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("150000"), new BigDecimal("75000"), 150, 1, "admin")
        );
        voucherRepository.saveAll(vouchers);
    }

    private Voucher createVoucher(String code, String name, String description, VoucherType type,
                                BigDecimal discountPercentage, BigDecimal discountAmount,
                                Long startTime, Long endTime, BigDecimal minOrderValue, 
                                BigDecimal maxDiscountValue, Integer usageLimit, 
                                Integer usageLimitPerUser, String createdBy) {
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setName(name);
        voucher.setDescription(description);
        voucher.setVoucherType(type);
        voucher.setDiscountPercentage(discountPercentage);
        voucher.setDiscountAmount(discountAmount);
        voucher.setStartTime(startTime);
        voucher.setEndTime(endTime);
        voucher.setMinOrderValue(minOrderValue);
        voucher.setMaxDiscountValue(maxDiscountValue);
        voucher.setUsageLimit(usageLimit);
        voucher.setUsedCount(0);
        voucher.setUsageLimitPerUser(usageLimitPerUser);
        voucher.setCreatedBy(createdBy);
        voucher.setStatus((byte) 1);
        return voucher;
    }

    private void initializeUserVouchers() {
        log.info("Initializing user vouchers...");
        
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        List<Voucher> vouchers = voucherRepository.findAll();
        
        // Gán một số voucher cho khách hàng
        for (User customer : customers.subList(0, Math.min(3, customers.size()))) {
            for (Voucher voucher : vouchers.subList(0, Math.min(2, vouchers.size()))) {
                UserVoucher userVoucher = new UserVoucher();
                userVoucher.setUser(customer);
                userVoucher.setVoucher(voucher);
                userVoucher.setUsedCount(0);
                userVoucherRepository.save(userVoucher);
            }
        }
    }

    private void initializeFlashSales() {
        log.info("Initializing flash sales...");
        
        long currentTime = System.currentTimeMillis();
        long oneWeek = 7L * 24 * 60 * 60 * 1000;
        
        // Tạo Flash Sale
        FlashSale flashSale = FlashSale.builder()
                .name("Flash Sale Cuối Tuần")
                .startTime(currentTime)
                .endTime(currentTime + oneWeek)
                .createdBy(1L)
                .status((byte) 1)
                .build();
        flashSaleRepository.save(flashSale);
        
        // Tạo Flash Sale Items
        List<Book> books = bookRepository.findAll();
        for (Book book : books.subList(0, Math.min(5, books.size()))) {
            FlashSaleItem item = FlashSaleItem.builder()
                    .flashSale(flashSale)
                    .book(book)
                    .discountPrice(book.getPrice().multiply(new BigDecimal("0.8"))) // Giảm 20%
                    .discountPercentage(new BigDecimal("20"))
                    .stockQuantity(20)
                    .maxPurchasePerUser(5)
                    .createdBy(1L)
                    .status((byte) 1)
                    .build();
            flashSaleItemRepository.save(item);
        }
    }

    private void initializeEventCategories() {
        log.info("Initializing event categories...");
        
        List<EventCategory> eventCategories = Arrays.asList(
            createEventCategory("Sự kiện sách", "Các sự kiện liên quan đến sách", "📚"),
            createEventCategory("Gặp gỡ tác giả", "Sự kiện gặp gỡ tác giả", "👨‍💼"),
            createEventCategory("Khuyến mãi", "Sự kiện khuyến mãi đặc biệt", "🎉"),
            createEventCategory("Thử thách đọc", "Thử thách đọc sách", "🏆"),
            createEventCategory("Hội thảo", "Các hội thảo về sách", "💼")
        );
        eventCategoryRepository.saveAll(eventCategories);
    }

    private EventCategory createEventCategory(String name, String description, String icon) {
        EventCategory category = new EventCategory();
        category.setCategoryName(name);
        category.setDescription(description);
        category.setIconUrl(icon);
        category.setIsActive(true);
        return category;
    }

    private void initializeEvents() {
        log.info("Initializing events...");
        
        List<EventCategory> categories = eventCategoryRepository.findAll();
        List<User> users = userRepository.findAll();
        
        long currentTime = System.currentTimeMillis();
        long oneWeek = 7L * 24 * 60 * 60 * 1000;
        long oneMonth = 30L * 24 * 60 * 60 * 1000;
        
        List<Event> events = Arrays.asList(
            createEvent("Ra mắt sách mới tháng 7", "Sự kiện ra mắt các đầu sách mới trong tháng", 
                EventType.BOOK_LAUNCH, categories.get(0), EventStatus.ONGOING, 
                currentTime, currentTime + oneWeek, 50, "BookStation HN", false, users.get(0)),
            createEvent("Gặp gỡ Nguyễn Nhật Ánh", "Buổi gặp gỡ và ký tặng sách với tác giả Nguyễn Nhật Ánh", 
                EventType.AUTHOR_MEET, categories.get(1), EventStatus.PUBLISHED, 
                currentTime + oneWeek, currentTime + oneWeek * 2, 100, "BookStation HCM", false, users.get(1)),
            createEvent("Thử thách đọc sách mùa hè", "Thử thách đọc 10 cuốn sách trong mùa hè", 
                EventType.READING_CHALLENGE, categories.get(3), EventStatus.ONGOING, 
                currentTime, currentTime + oneMonth * 2, 200, "Online", true, users.get(0)),
            createEvent("Flash Sale sách kinh tế", "Giảm giá sâu các đầu sách kinh tế", 
                EventType.PROMOTION, categories.get(2), EventStatus.ONGOING, 
                currentTime, currentTime + oneWeek * 2, null, "Online", true, users.get(1)),
            createEvent("Hội thảo xu hướng đọc 2025", "Thảo luận về xu hướng đọc sách năm 2025", 
                EventType.WORKSHOP, categories.get(4), EventStatus.PUBLISHED, 
                currentTime + oneWeek * 3, currentTime + oneWeek * 3 + 24 * 60 * 60 * 1000, 80, "BookStation HN", false, users.get(0))
        );
        eventRepository.saveAll(events);
    }

    private Event createEvent(String name, String description, EventType type, EventCategory category,
                             EventStatus status, Long startDate, Long endDate, Integer maxParticipants, 
                             String location, Boolean isOnline, User createdBy) {
        Event event = new Event();
        event.setEventName(name);
        event.setDescription(description);
        event.setEventType(type);
        event.setEventCategory(category);
        event.setStatus(status);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setMaxParticipants(maxParticipants);
        event.setCurrentParticipants(0);
        event.setLocation(location);
        event.setIsOnline(isOnline);
        event.setCreatedBy(createdBy);
        return event;
    }

    private void initializeEventGifts() {
        log.info("Initializing event gifts...");
        
        List<Event> events = eventRepository.findAll();
        List<Book> books = bookRepository.findAll();
        List<Voucher> vouchers = voucherRepository.findAll();
        
        for (Event event : events) {
            // Tạo gift cho mỗi event
            EventGift bookGift = createEventGift(event, "Sách miễn phí", 
                "Nhận 1 cuốn sách miễn phí", new BigDecimal("100000"), 10, books.get(0), null);
            EventGift voucherGift = createEventGift(event, "Voucher giảm giá", 
                "Voucher giảm 20%", new BigDecimal("50000"), 20, null, vouchers.get(0));
            
            eventGiftRepository.saveAll(Arrays.asList(bookGift, voucherGift));
        }
    }

    private EventGift createEventGift(Event event, String name, String description, 
                                     BigDecimal value, Integer quantity, Book book, Voucher voucher) {
        EventGift gift = new EventGift();
        gift.setEvent(event);
        gift.setGiftName(name);
        gift.setDescription(description);
        gift.setGiftValue(value);
        gift.setQuantity(quantity);
        gift.setRemainingQuantity(quantity);
        gift.setBook(book);
        gift.setVoucher(voucher);
        return gift;
    }

    private void initializeAddresses() {
        log.info("Initializing addresses...");
        
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        
        for (int i = 0; i < customers.size(); i++) {
            User customer = customers.get(i);
            Address address = createAddress(customer, customer.getFullName(), 
                "Địa chỉ " + (i + 1) + ", Phường " + (i + 1) + ", Quận " + (i + 1) + ", TP.HCM", 
                "0123456789", i == 0);
            addressRepository.save(address);
        }
    }

    private Address createAddress(User user, String recipientName, String addressDetail, 
                                String phoneNumber, Boolean isDefault) {
        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(recipientName);
        address.setAddressDetail(addressDetail);
        address.setPhoneNumber(phoneNumber);
        address.setIsDefault(isDefault);
        address.setCreatedBy(user.getId());
        address.setStatus((byte) 1);
        return address;
    }

    private void initializeCarts() {
        log.info("Initializing carts...");
        
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        List<Book> books = bookRepository.findAll();
        
        for (User customer : customers) {
            Cart cart = new Cart();
            cart.setUser(customer);
            cart.setCreatedBy(customer.getId());
            cart.setStatus((byte) 1);
            cartRepository.save(cart);
            
            // Thêm một số item vào cart
            for (int i = 0; i < Math.min(3, books.size()); i++) {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setBook(books.get(i));
                item.setQuantity(i + 1);
                item.setCreatedBy(customer.getId());
                item.setStatus((byte) 1);
                cartItemRepository.save(item);
            }
        }
    }

    private void initializeOrders() {
        log.info("Initializing orders...");
        
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        List<Book> books = bookRepository.findAll();
        List<Address> addresses = addressRepository.findAll();
        
        for (int i = 0; i < Math.min(3, customers.size()); i++) {
            User customer = customers.get(i);
            Address address = addresses.stream()
                    .filter(addr -> addr.getUser().equals(customer))
                    .findFirst()
                    .orElse(addresses.get(0));
            
            Order order = createOrder(customer, address, OrderStatus.DELIVERED, "ONLINE");
            orderRepository.save(order);
            
            // Tạo order details
            BigDecimal subtotal = BigDecimal.ZERO;
            for (int j = 0; j < Math.min(2, books.size()); j++) {
                Book book = books.get(j);
                int quantity = j + 1;
                BigDecimal unitPrice = book.getPrice();
                
                OrderDetail detail = createOrderDetail(order, book, quantity, unitPrice);
                orderDetailRepository.save(detail);
                
                subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            }
            
            // Cập nhật tổng tiền đơn hàng
            order.setSubtotal(subtotal);
            order.setTotalAmount(subtotal.add(order.getShippingFee()));
            orderRepository.save(order);
        }
    }

    private Order createOrder(User customer, Address address, OrderStatus status, String orderType) {
        Order order = new Order();
        order.setUser(customer);
        order.setAddress(address);
        order.setOrderDate(System.currentTimeMillis());
        order.setSubtotal(BigDecimal.ZERO);
        order.setShippingFee(new BigDecimal("30000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setDiscountShipping(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderStatus(status);
        order.setOrderType(orderType);
        order.setCode("ORD" + System.currentTimeMillis());
        order.setCreatedBy(customer.getId());
        order.setStatus((byte) 1);
        return order;
    }

    private OrderDetail createOrderDetail(Order order, Book book, Integer quantity, BigDecimal unitPrice) {
        OrderDetail detail = new OrderDetail();
        OrderDetailId id = new OrderDetailId();
        id.setOrderId(order.getId());
        id.setBookId(book.getId());
        detail.setId(id);
        detail.setOrder(order);
        detail.setBook(book);
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.setCreatedBy(order.getCreatedBy());
        return detail;
    }

    private void initializePoints() {
        log.info("Initializing points...");
        
        List<Order> orders = orderRepository.findAll();
        
        for (Order order : orders) {
            if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                Point point = new Point();
                point.setUser(order.getUser());
                point.setOrder(order);
                point.setPointEarned((int) (order.getTotalAmount().doubleValue() / 1000)); // 1 điểm / 1000đ
                point.setMinSpent(order.getTotalAmount());
                point.setPointSpent(0);
                point.setDescription("Tích điểm từ đơn hàng " + order.getCode());
                point.setCreatedAt(System.currentTimeMillis());
                point.setStatus((byte) 1);
                pointRepository.save(point);
                
                // Cập nhật tổng điểm cho user
                User user = order.getUser();
                user.setTotalPoint((user.getTotalPoint() != null ? user.getTotalPoint() : 0) + point.getPointEarned());
                user.setTotalSpent((user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO).add(order.getTotalAmount()));
                userRepository.save(user);
            }
        }
    }

    private void initializeReviews() {
        log.info("Initializing reviews...");
        
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        List<Book> books = bookRepository.findAll();
        
        String[] comments = {
            "Sách rất hay, nội dung hấp dẫn!",
            "Chất lượng tốt, giao hàng nhanh.",
            "Nội dung phong phú, đáng đọc.",
            "Sách in đẹp, giá cả hợp lý.",
            "Rất hài lòng với sản phẩm này."
        };
        
        for (int i = 0; i < Math.min(customers.size(), books.size()); i++) {
            Review review = Review.builder()
                    .book(books.get(i))
                    .user(customers.get(i))
                    .rating(4 + (i % 2)) // Rating 4 hoặc 5
                    .comment(comments[i % comments.length])
                    .reviewDate(System.currentTimeMillis())
                    .reviewStatus(ReviewStatus.APPROVED)
                    .build();
            reviewRepository.save(review);
        }
    }

    private void initializeEventParticipants() {
        log.info("Initializing event participants...");
        
        List<Event> events = eventRepository.findAll();
        List<User> customers = userRepository.findByRole_RoleName("CUSTOMER");
        
        for (Event event : events) {
            // Thêm một số participant cho mỗi event
            for (int i = 0; i < Math.min(3, customers.size()); i++) {
                EventParticipant participant = new EventParticipant();
                participant.setEvent(event);
                participant.setUser(customers.get(i));
                participant.setJoinedAt(System.currentTimeMillis());
                participant.setIsWinner(i == 0); // Participant đầu tiên là winner
                participant.setCompletionStatus(ParticipantStatus.COMPLETED);
                participant.setNotes("Tham gia sự kiện " + event.getEventName());
                eventParticipantRepository.save(participant);
            }
            
            // Cập nhật số lượng participant hiện tại
            event.setCurrentParticipants(Math.min(3, customers.size()));
            eventRepository.save(event);
        }
    }

    /**
     * Method để reset toàn bộ dữ liệu và khởi tạo lại từ đầu
     * CHỈ SỬ DỤNG TRONG MÔI TRƯỜNG DEVELOPMENT/TEST
     */
    @Transactional
    public void forceReinitializeData() {
        log.warn("FORCE RE-INITIALIZING ALL DATA - THIS WILL DELETE ALL EXISTING DATA!");
        
        try {
            // Xóa dữ liệu theo thứ tự dependency (từ con đến cha)
            eventParticipantRepository.deleteAll();
            reviewRepository.deleteAll();
            pointRepository.deleteAll();
            orderDetailRepository.deleteAll();
            orderRepository.deleteAll();
            cartItemRepository.deleteAll();
            cartRepository.deleteAll();
            addressRepository.deleteAll();
            eventGiftRepository.deleteAll();
            eventRepository.deleteAll();
            eventCategoryRepository.deleteAll();
            flashSaleItemRepository.deleteAll();
            flashSaleRepository.deleteAll();
            userVoucherRepository.deleteAll();
            voucherRepository.deleteAll();
            authorBookRepository.deleteAll();
            bookRepository.deleteAll();
            supplierRepository.deleteAll();
            authorRepository.deleteAll();
            categoryRepository.deleteAll();
            userRankRepository.deleteAll();
            userRepository.deleteAll();
            rankRepository.deleteAll();
            roleRepository.deleteAll();
            
            log.info("All data deleted successfully!");
            
            // Khởi tạo lại dữ liệu
            initializeIfEmpty();
            
            log.info("Data reinitialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during force reinitialization: ", e);
            throw e;
        }
    }

    /**
     * Method để kiểm tra trạng thái dữ liệu hiện tại
     */
    public void checkDataStatus() {
        log.info("=== DATA STATUS CHECK ===");
        log.info("Roles: {}", roleRepository.count());
        log.info("Ranks: {}", rankRepository.count());
        log.info("Users: {}", userRepository.count());
        log.info("User Ranks: {}", userRankRepository.count());
        log.info("Categories: {}", categoryRepository.count());
        log.info("Authors: {}", authorRepository.count());
        log.info("Suppliers: {}", supplierRepository.count());
        log.info("Books: {}", bookRepository.count());
        log.info("Author Books: {}", authorBookRepository.count());
        log.info("Vouchers: {}", voucherRepository.count());
        log.info("User Vouchers: {}", userVoucherRepository.count());
        log.info("Flash Sales: {}", flashSaleRepository.count());
        log.info("Flash Sale Items: {}", flashSaleItemRepository.count());
        log.info("Event Categories: {}", eventCategoryRepository.count());
        log.info("Events: {}", eventRepository.count());
        log.info("Event Gifts: {}", eventGiftRepository.count());
        log.info("Event Participants: {}", eventParticipantRepository.count());
        log.info("Addresses: {}", addressRepository.count());
        log.info("Carts: {}", cartRepository.count());
        log.info("Cart Items: {}", cartItemRepository.count());
        log.info("Orders: {}", orderRepository.count());
        log.info("Order Details: {}", orderDetailRepository.count());
        log.info("Points: {}", pointRepository.count());
        log.info("Reviews: {}", reviewRepository.count());
        log.info("========================");
    }
}
