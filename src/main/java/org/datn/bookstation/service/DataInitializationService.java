package org.datn.bookstation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.*;
import org.datn.bookstation.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// @Service - Temporarily disabled to avoid Session/EntityManager is closed error
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
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final AuthorBookRepository authorBookRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
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

        // Kiểm tra và khởi tạo Publishers
        if (publisherRepository.count() == 0) {
            initializePublishers();
        } else {
            log.info("Publishers already exist, skipping publisher initialization.");
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
            initializeTrendingOrderData(); // ✅ THÊM: Tạo thêm dữ liệu cho trending
            
            // ✅ THÊM: Tạo dữ liệu đơn hàng test theo thời gian cho Lê Văn C (chỉ khi chưa có đơn hàng nào)
            initializeTestOrdersForLeVanC();
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
            initializeTrendingReviewData(); // ✅ THÊM: Tạo thêm review cho trending
        } else {
            log.info("Reviews already exist, skipping initialization.");
        }
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        List<Role> roles = Arrays.asList(
            createRole(RoleName.ADMIN, "Quản trị viên hệ thống"),
            createRole(RoleName.STAFF, "Nhân viên"),
            createRole(RoleName.CUSTOMER, "Khách hàng")
        );
        roleRepository.saveAll(roles);
    }

    private Role createRole(RoleName roleName, String description) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setStatus((byte) 1);
        return role;
    }

    private void initializeRanks() {
        log.info("Initializing ranks...");
        List<Rank> ranks = Arrays.asList(
            createRank("VÀNG", new BigDecimal("5000000"), new BigDecimal("1.5")),
            createRank("BẠC", new BigDecimal("1000000"), new BigDecimal("1.2")),
            createRank("KIM CƯƠNG", new BigDecimal("10000000"), new BigDecimal("2.0"))
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
        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN).orElse(null);
        Role staffRole = roleRepository.findByRoleName(RoleName.STAFF).orElse(null);
        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER).orElse(null);

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
        // Thêm hàng loạt voucher test cho Lê Văn C
        addTestVouchersForLeVanC();
    }

    /**
     * Tạo và gán nhiều loại voucher cho user Lê Văn C để test đủ trường hợp
     */
    private void addTestVouchersForLeVanC() {
        User leVanC = userRepository.findByEmail("customer1@gmail.com").orElse(null);
        if (leVanC == null) return;

        long now = System.currentTimeMillis();
        long oneMonth = 30L * 24 * 60 * 60 * 1000;
        List<Voucher> vouchersToAdd = new java.util.ArrayList<>();

        // 5 voucher miễn phí vận chuyển (FREESHIP)
        for (int i = 1; i <= 5; i++) {
            vouchersToAdd.add(createVoucher(
                "FREESHIP" + i,
                "Miễn phí vận chuyển",
                "Voucher miễn phí vận chuyển cho đơn bất kỳ",
                VoucherCategory.SHIPPING,
                DiscountType.FIXED_AMOUNT,
                null,
                new BigDecimal("30000"), // Giảm tối đa 30k tiền ship
                now,
                now + oneMonth,
                BigDecimal.ZERO,
                new BigDecimal("30000"),
                1,
                1,
                "admin"
            ));
        }

        // 5 voucher giảm 20k tiền ship cho đơn từ 50k
        for (int i = 1; i <= 5; i++) {
            vouchersToAdd.add(createVoucher(
                "SHIP20K" + i,
                "Giảm 20K tiền ship",
                "Giảm 20.000đ phí vận chuyển cho đơn từ 50.000đ",
                VoucherCategory.SHIPPING,
                DiscountType.FIXED_AMOUNT,
                null,
                new BigDecimal("20000"),
                now,
                now + oneMonth,
                new BigDecimal("50000"),
                new BigDecimal("20000"),
                1,
                1,
                "admin"
            ));
        }

        // 5 voucher thường giảm 40k cho đơn từ 100k
        for (int i = 1; i <= 5; i++) {
            vouchersToAdd.add(createVoucher(
                "SALE40K" + i,
                "Giảm 40K cho đơn từ 100K",
                "Giảm 40.000đ cho đơn hàng từ 100.000đ",
                VoucherCategory.NORMAL,
                DiscountType.FIXED_AMOUNT,
                null,
                new BigDecimal("40000"),
                now,
                now + oneMonth,
                new BigDecimal("100000"),
                new BigDecimal("40000"),
                1,
                1,
                "admin"
            ));
        }

        // 5 voucher giảm theo % đơn hàng (10%, 15%, 20%, 25%, 30%)
        int[] percents = {10, 15, 20, 25, 30};
        for (int i = 0; i < percents.length; i++) {
            vouchersToAdd.add(createVoucher(
                "SALE" + percents[i] + "PCT",
                "Giảm " + percents[i] + "%",
                "Giảm " + percents[i] + "% tối đa " + (percents[i] * 1000) + "đ cho đơn từ " + (percents[i] * 10000) + "đ",
                VoucherCategory.NORMAL,
                DiscountType.PERCENTAGE,
                new BigDecimal(percents[i]),
                null,
                now,
                now + oneMonth,
                new BigDecimal(percents[i] * 10000),
                new BigDecimal(percents[i] * 1000),
                1,
                1,
                "admin"
            ));
        }

        // Shopee style: các voucher đặc biệt
        vouchersToAdd.add(createVoucher(
            "WELCOME",
            "Voucher chào mừng đặc biệt",
            "Giảm 15% tối đa 50.000đ cho đơn từ 100.000đ - Voucher chào mừng thành viên mới",
            VoucherCategory.NORMAL,
            DiscountType.PERCENTAGE,
            new BigDecimal("15"),
            null,
            now,
            now + oneMonth,
            new BigDecimal("100000"),
            new BigDecimal("50000"),
            1,
            1,
            "admin"
        ));

        vouchersToAdd.add(createVoucher(
            "FLASHSALE50K",
            "Flash Sale Giảm 50K",
            "Giảm 50.000đ cho đơn từ 300.000đ, chỉ áp dụng trong khung giờ vàng",
            VoucherCategory.NORMAL,
            DiscountType.FIXED_AMOUNT,
            null,
            new BigDecimal("50000"),
            now,
            now + (3L * 24 * 60 * 60 * 1000), // 3 ngày
            new BigDecimal("300000"),
            new BigDecimal("50000"),
            1,
            1,
            "admin"
        ));

        vouchersToAdd.add(createVoucher(
            "NEWUSER100K",
            "Giảm 100K cho khách mới",
            "Giảm 100.000đ cho đơn từ 500.000đ, chỉ cho khách mới",
            VoucherCategory.NORMAL,
            DiscountType.FIXED_AMOUNT,
            null,
            new BigDecimal("100000"),
            now,
            now + oneMonth,
            new BigDecimal("500000"),
            new BigDecimal("100000"),
            1,
            1,
            "admin"
        ));

        vouchersToAdd.add(createVoucher(
            "SHIPMAX",
            "Miễn phí ship tối đa 50K",
            "Miễn phí vận chuyển tối đa 50.000đ cho đơn từ 200.000đ",
            VoucherCategory.SHIPPING,
            DiscountType.FIXED_AMOUNT,
            null,
            new BigDecimal("50000"),
            now,
            now + oneMonth,
            new BigDecimal("200000"),
            new BigDecimal("50000"),
            1,
            1,
            "admin"
        ));

        // Lưu voucher vào DB
        voucherRepository.saveAll(vouchersToAdd);

        // Gán cho user Lê Văn C
        for (Voucher v : vouchersToAdd) {
            for (int i = 0; i < v.getUsageLimitPerUser(); i++) {
                UserVoucher uv = new UserVoucher();
                uv.setUser(leVanC);
                uv.setVoucher(v);
                uv.setUsedCount(0);
                userVoucherRepository.save(uv);
            }
        }
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
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        Rank goldRank = rankRepository.findByRankName("VÀNG").orElse(null);
        for (int i = 0; i < Math.min(3, customers.size()); i++) {
            User customer = customers.get(i);
            UserRank userRank = new UserRank();
            userRank.setUser(customer);
            userRank.setRank(goldRank);
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

    private void initializePublishers() {
        log.info("Initializing publishers...");
        List<Publisher> publishers = Arrays.asList(
            createPublisher("NXB Kim Đồng", "55 Quang Trung, Hai Bà Trưng, Hà Nội", "contact@kimdong.com.vn", "024-3971-0999", "https://nxbkimdong.com.vn", "Nhà xuất bản chuyên về sách thiếu nhi", 1954),
            createPublisher("NXB Trẻ", "161B Lý Chính Thắng, Quận 3, TP.HCM", "contact@nxbtre.com.vn", "028-3930-5001", "https://nxbtre.com.vn", "Nhà xuất bản chuyên về sách giáo dục và văn học", 1981),
            createPublisher("NXB Văn học", "18 Nguyễn Trường Tộ, Ba Đình, Hà Nội", "contact@nxbvanhoc.vn", "024-3825-4091", "https://nxbvanhoc.vn", "Chuyên xuất bản văn học trong nước và nước ngoài", 1957),
            createPublisher("NXB Giáo dục Việt Nam", "81 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội", "contact@nxbgd.vn", "024-3822-5340", "https://nxbgiaoduc.vn", "Nhà xuất bản sách giáo khoa và giáo trình", 1957),
            createPublisher("NXB Thông tin và Truyền thông", "115 Trần Duy Hưng, Cầu Giấy, Hà Nội", "contact@nxbtttt.vn", "024-3568-8244", "https://nxbtttt.vn", "Chuyên về sách công nghệ thông tin", 2008),
            createPublisher("NXB Lao động", "175 Giảng Võ, Đống Đa, Hà Nội", "contact@nxblaodong.vn", "024-3851-3341", "https://nxblaodong.vn", "Nhà xuất bản về lao động và xã hội", 1958)
        );
        publisherRepository.saveAll(publishers);
    }

    private Publisher createPublisher(String name, String address, String email, String phone, String website, String description, Integer establishedYear) {
        Publisher publisher = new Publisher();
        publisher.setPublisherName(name);
        publisher.setAddress(address);
        publisher.setEmail(email);
        publisher.setPhoneNumber(phone);
        publisher.setWebsite(website);
        publisher.setDescription(description);
        publisher.setEstablishedYear(establishedYear);
        publisher.setStatus((byte) 1);
        publisher.setCreatedBy(1);
        publisher.setUpdatedBy(1);
        return publisher;
    }

    private void initializeBooks() {
        log.info("Initializing books...");
        
        List<Category> categories = categoryRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        List<Publisher> publishers = publisherRepository.findAll();
        
        // Tạo timestamp cho các năm xuất bản (milliseconds since Unix epoch)
        long year2010 = 1262304000000L; // 2010-01-01
        long year1941 = -915148800000L; // 1941-01-01
        long year1987 = 536457600000L;  // 1987-01-01
        long year1934 = -1136073600000L; // 1934-01-01
        long year1997_june26 = 867283200000L; // 1997-06-26
        long year1936 = -1073001600000L; // 1936-01-01
        long year1937 = -1041379200000L; // 1937-01-01
        long year2020 = 1577836800000L; // 2020-01-01
        long year2018 = 1514764800000L; // 2018-01-01
        long year1970 = 0L; // 1970-01-01
        long year2017 = 1483228800000L; // 2017-01-01
        
        List<Book> books = Arrays.asList(
            createBook("Tôi thấy hoa vàng trên cỏ xanh", "Tiểu thuyết của Nguyễn Nhật Ánh", 
                new BigDecimal("85000"), 100, year2010, 
                findCategoryByName(categories, "Tiểu thuyết"), suppliers.get(0), 
                findPublisherByName(publishers, "NXB Trẻ"), 1),
            createBook("Dế Mèn phiêu lưu ký", "Tác phẩm kinh điển của Tô Hoài", 
                new BigDecimal("65000"), 150, year1941, 
                findCategoryByName(categories, "Thiếu nhi"), suppliers.get(0), 
                findPublisherByName(publishers, "NXB Kim Đồng"), 1),
            createBook("Chí Phèo", "Truyện ngắn của Nam Cao", 
                new BigDecimal("45000"), 200, year1941, 
                findCategoryByName(categories, "Tiểu thuyết"), suppliers.get(2), 
                findPublisherByName(publishers, "NXB Văn học"), 1),
            createBook("Norwegian Wood", "Tiểu thuyết của Haruki Murakami", 
                new BigDecimal("120000"), 80, year1987, 
                findCategoryByName(categories, "Lãng mạn"), suppliers.get(1), 
                findPublisherByName(publishers, "NXB Trẻ"), 1),
            createBook("Murder on the Orient Express", "Tiểu thuyết trinh thám của Agatha Christie", 
                new BigDecimal("95000"), 90, year1934, 
                findCategoryByName(categories, "Trinh thám"), suppliers.get(4), 
                findPublisherByName(publishers, "NXB Văn học"), 1),
            createBook("Harry Potter và Hòn đá Phù thủy", "Tập 1 series Harry Potter", 
                new BigDecimal("150000"), 120, year1997_june26, 
                findCategoryByName(categories, "Khoa học viễn tưởng"), suppliers.get(1), 
                findPublisherByName(publishers, "NXB Trẻ"), 1),
            createBook("Đắc Nhân Tâm", "Sách self-help của Dale Carnegie", 
                new BigDecimal("89000"), 300, year1936, 
                findCategoryByName(categories, "Kinh doanh"), suppliers.get(4), 
                findPublisherByName(publishers, "NXB Lao động"), 1),
            createBook("Think and Grow Rich", "Sách về thành công của Napoleon Hill", 
                new BigDecimal("79000"), 180, year1937, 
                findCategoryByName(categories, "Kinh doanh"), suppliers.get(5), 
                findPublisherByName(publishers, "NXB Lao động"), 1),
            createBook("Toán học lớp 12", "Sách giáo khoa Toán 12", 
                new BigDecimal("25000"), 500, year2020, 
                findCategoryByName(categories, "Sách giáo khoa"), suppliers.get(3), 
                findPublisherByName(publishers, "NXB Giáo dục Việt Nam"), 1),
            createBook("Từ điển Anh - Việt", "Từ điển Anh Việt cơ bản", 
                new BigDecimal("135000"), 250, year2018, 
                findCategoryByName(categories, "Từ điển"), suppliers.get(3), 
                findPublisherByName(publishers, "NXB Giáo dục Việt Nam"), 1),
            createBook("Doraemon tập 1", "Truyện tranh Doraemon", 
                new BigDecimal("18000"), 400, year1970, 
                findCategoryByName(categories, "Truyện tranh"), suppliers.get(0), 
                findPublisherByName(publishers, "NXB Kim Đồng"), 1),
            createBook("Marketing 4.0", "Sách về marketing hiện đại", 
                new BigDecimal("189000"), 100, year2017, 
                findCategoryByName(categories, "Marketing"), suppliers.get(4), 
                findPublisherByName(publishers, "NXB Thông tin và Truyền thông"), 1)
        );
        bookRepository.saveAll(books);
    }

    private Book createBook(String name, String description, BigDecimal price, Integer stock, 
                          Long publicationDate, Category category, Supplier supplier, Publisher publisher, Integer createdBy) {
        Book book = new Book();
        book.setBookName(name);
        book.setDescription(description);
        book.setPrice(price);
        book.setStockQuantity(stock);
        book.setPublicationDate(publicationDate);
        book.setCategory(category);
        book.setSupplier(supplier);
        book.setPublisher(publisher);
        book.setCreatedBy(createdBy);
        book.setBookCode("BOOK" + System.currentTimeMillis());
        book.setStatus((byte) 1);
        // Thêm dữ liệu mẫu cho images (nhiều ảnh, cách nhau bằng dấu phẩy)
        book.setImages("https://yourdomain.com/uploads/products/sample1.jpg,https://yourdomain.com/uploads/products/sample2.jpg");
        return book;
    }

    private Category findCategoryByName(List<Category> categories, String name) {
        return categories.stream()
                .filter(cat -> cat.getCategoryName().equals(name))
                .findFirst()
                .orElse(categories.get(0));
    }

    private Publisher findPublisherByName(List<Publisher> publishers, String name) {
        return publishers.stream()
                .filter(pub -> pub.getPublisherName().equals(name))
                .findFirst()
                .orElse(publishers.get(0));
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
            createVoucher("WELCOME", "Voucher chào mừng", "Giảm 15% cho đơn hàng đầu tiên, tối đa 50K", 
                VoucherCategory.NORMAL, DiscountType.PERCENTAGE, new BigDecimal("15"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("100000"), new BigDecimal("50000"), 100, 1, "admin"),
            createVoucher("WELCOME10", "Voucher chào mừng", "Giảm 10% cho đơn hàng đầu tiên", 
                VoucherCategory.NORMAL, DiscountType.PERCENTAGE, new BigDecimal("10"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("100000"), new BigDecimal("50000"), 100, 1, "admin"),
            createVoucher("SAVE50K", "Voucher giảm 50K", "Giảm 50.000đ cho đơn từ 500K", 
                VoucherCategory.NORMAL, DiscountType.FIXED_AMOUNT, null, new BigDecimal("50000"), 
                currentTime, currentTime + oneMonth, new BigDecimal("500000"), null, 50, 1, "admin"),
            createVoucher("FREESHIP", "Miễn phí vận chuyển", "Miễn phí ship cho đơn từ 200K", 
                VoucherCategory.SHIPPING, DiscountType.FIXED_AMOUNT, null, null, 
                currentTime, currentTime + oneMonth, new BigDecimal("200000"), null, 200, 1, "admin"),
            createVoucher("SUMMER20", "Voucher hè", "Giảm 20% tối đa 100K", 
                VoucherCategory.NORMAL, DiscountType.PERCENTAGE, new BigDecimal("20"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("300000"), new BigDecimal("100000"), 75, 1, "admin"),
            createVoucher("NEWBIE15", "Voucher thành viên mới", "Giảm 15% cho khách hàng mới", 
                VoucherCategory.NORMAL, DiscountType.PERCENTAGE, new BigDecimal("15"), null, 
                currentTime, currentTime + oneMonth, new BigDecimal("150000"), new BigDecimal("75000"), 150, 1, "admin")
        );
        voucherRepository.saveAll(vouchers);
    }

    private Voucher createVoucher(String code, String name, String description, VoucherCategory category, DiscountType discountType,
                                BigDecimal discountPercentage, BigDecimal discountAmount,
                                Long startTime, Long endTime, BigDecimal minOrderValue, 
                                BigDecimal maxDiscountValue, Integer usageLimit, 
                                Integer usageLimitPerUser, String createdBy) {
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setName(name);
        voucher.setDescription(description);
        voucher.setVoucherCategory(category);
        voucher.setDiscountType(discountType);
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
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
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

    private void initializeAddresses() {
        log.info("Initializing addresses...");
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        
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
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
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
                item.setSelected(true);
                cartItemRepository.save(item);
            }
        }
    }

    private void initializeOrders() {
        log.info("Initializing orders...");
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        List<Book> books = bookRepository.findAll();
        List<Address> addresses = addressRepository.findAll();
        
        // Tạo đơn hàng DELIVERED cho 3 khách hàng đầu
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
        
        // ✅ THÊM MỚI: Tạo đơn hàng với các trạng thái hoàn hàng để test
        if (customers.size() >= 2 && books.size() >= 2) {
            User testCustomer = customers.get(0); // Lê Văn C (customer1@gmail.com)
            Address testAddress = addresses.stream()
                    .filter(addr -> addr.getUser().equals(testCustomer))
                    .findFirst()
                    .orElse(addresses.get(0));
            
            // 1. Đơn hàng đang chờ admin xem xét yêu cầu hoàn trả
            Order refundRequestedOrder = createOrder(testCustomer, testAddress, OrderStatus.REFUND_REQUESTED, "ONLINE");
            refundRequestedOrder.setCancelReason("Khách hàng yêu cầu hoàn trả vì sản phẩm không đúng mô tả");
            orderRepository.save(refundRequestedOrder);
            
            // Thêm order details cho đơn REFUND_REQUESTED
            Book book1 = books.get(0);
            OrderDetail detail1 = createOrderDetail(refundRequestedOrder, book1, 2, book1.getPrice());
            orderDetailRepository.save(detail1);
            
            BigDecimal subtotal1 = book1.getPrice().multiply(BigDecimal.valueOf(2));
            refundRequestedOrder.setSubtotal(subtotal1);
            refundRequestedOrder.setTotalAmount(subtotal1.add(refundRequestedOrder.getShippingFee()));
            orderRepository.save(refundRequestedOrder);
            
            // 2. Đơn hàng đang trong quá trình hoàn tiền
            Order refundingOrder = createOrder(testCustomer, testAddress, OrderStatus.REFUNDING, "ONLINE");
            refundingOrder.setCancelReason("Admin đã chấp nhận yêu cầu hoàn trả, đang xử lý hoàn tiền");
            orderRepository.save(refundingOrder);
            
            // Thêm order details cho đơn REFUNDING
            Book book2 = books.get(1);
            OrderDetail detail2 = createOrderDetail(refundingOrder, book2, 1, book2.getPrice());
            orderDetailRepository.save(detail2);
            
            BigDecimal subtotal2 = book2.getPrice();
            refundingOrder.setSubtotal(subtotal2);
            refundingOrder.setTotalAmount(subtotal2.add(refundingOrder.getShippingFee()));
            orderRepository.save(refundingOrder);
            
            // 3. Đơn hàng đã hoàn tiền hoàn tất
            Order refundedOrder = createOrder(testCustomer, testAddress, OrderStatus.REFUNDED, "ONLINE");
            refundedOrder.setCancelReason("Đã hoàn trả thành công cho khách hàng");
            orderRepository.save(refundedOrder);
            
            // Thêm order details cho đơn REFUNDED
            Book book3 = books.size() > 2 ? books.get(2) : books.get(0);
            OrderDetail detail3 = createOrderDetail(refundedOrder, book3, 1, book3.getPrice());
            orderDetailRepository.save(detail3);
            
            BigDecimal subtotal3 = book3.getPrice();
            refundedOrder.setSubtotal(subtotal3);
            refundedOrder.setTotalAmount(subtotal3.add(refundedOrder.getShippingFee()));
            orderRepository.save(refundedOrder);
            
            log.info("Created test orders with refund statuses: REFUND_REQUESTED, REFUNDING, REFUNDED");
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
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        List<Book> books = bookRepository.findAll();
        
        String[] positiveComments = {
            "Sách rất hay, nội dung hấp dẫn!",
            "Chất lượng tốt, giao hàng nhanh.",
            "Nội dung phong phú, đáng đọc.",
            "Sách in đẹp, giá cả hợp lý.",
            "Rất hài lòng với sản phẩm này.",
            "Đọc xong thấy rất bổ ích!",
            "Recommend cho mọi người!",
            "Tác giả viết rất hay và dễ hiểu."
        };
        
        String[] negativeComments = {
            "Nội dung không như mong đợi.",
            "Giao hàng hơi chậm.",
            "Chất lượng bình thường thôi.",
            "Giá hơi cao so với chất lượng."
        };
        
        // Tạo review cho từng sách với tỉ lệ tích cực khác nhau
        for (int bookIndex = 0; bookIndex < books.size(); bookIndex++) {
            Book book = books.get(bookIndex);
            
            // Tạo 3-6 review cho mỗi sách để có đủ dữ liệu test (không phụ thuộc số customers)
            int reviewCount = 3 + (bookIndex % 4); // 3-6 reviews
            
            for (int i = 0; i < reviewCount; i++) {
                // Sử dụng customers theo kiểu circular để đảm bảo mọi sách đều có review
                User customer = customers.get(i % customers.size());
                
                // Tỉ lệ tích cực khác nhau cho từng sách để test API
                boolean isPositive;
                int rating;
                String comment;
                
                // ✅ ĐẶC BIỆT: Marketing 4.0 có 100% đánh giá tích cực
                if (book.getBookName().equals("Marketing 4.0")) {
                    isPositive = true; // 100% tích cực
                    rating = 5; // Tất cả đều 5 sao
                    comment = positiveComments[i % positiveComments.length];
                } else if (bookIndex < 7) {
                    // 7 sách đầu có tỉ lệ tích cực cao (>= 75%)
                    isPositive = (i < reviewCount * 0.85); // 85% tích cực
                    rating = isPositive ? (4 + (i % 2)) : (2 + (i % 2)); // 4-5 hoặc 2-3
                    comment = isPositive ? positiveComments[i % positiveComments.length] 
                                        : negativeComments[i % negativeComments.length];
                } else if (bookIndex < 10) {
                    // 3 sách tiếp theo có tỉ lệ tích cực vừa phải (50-70%)
                    isPositive = (i < reviewCount * 0.6); // 60% tích cực
                    rating = isPositive ? (3 + (i % 2)) : (2 + (i % 2)); // 3-4 hoặc 2-3
                    comment = isPositive ? positiveComments[i % positiveComments.length] 
                                        : negativeComments[i % negativeComments.length];
                } else {
                    // Sách còn lại có tỉ lệ tích cực thấp (<50%)
                    isPositive = (i < reviewCount * 0.3); // 30% tích cực
                    rating = isPositive ? (3 + (i % 2)) : (1 + (i % 2)); // 3-4 hoặc 1-2
                    comment = isPositive ? positiveComments[i % positiveComments.length] 
                                        : negativeComments[i % negativeComments.length];
                }
                
                Review review = Review.builder()
                        .book(book)
                        .user(customer)
                        .rating(rating)
                        .comment(comment)
                        .isPositive(isPositive) // ✅ THÊM: Thiết lập isPositive
                        .reviewDate(System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)) // Tạo thời gian khác nhau
                        .reviewStatus(ReviewStatus.APPROVED)
                        .build();
                reviewRepository.save(review);
            }
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
            reviewRepository.deleteAll();
            pointRepository.deleteAll();
            orderDetailRepository.deleteAll();
            orderRepository.deleteAll();
            cartItemRepository.deleteAll();
            cartRepository.deleteAll();
            addressRepository.deleteAll();
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
        log.info("Addresses: {}", addressRepository.count());
        log.info("Carts: {}", cartRepository.count());
        log.info("Cart Items: {}", cartItemRepository.count());
        log.info("Orders: {}", orderRepository.count());
        log.info("Order Details: {}", orderDetailRepository.count());
        log.info("Points: {}", pointRepository.count());
        log.info("Reviews: {}", reviewRepository.count());
        log.info("========================");
    }
    
    /**
     * ✅ THÊM METHOD: Tạo thêm dữ liệu đơn hàng để có sản phẩm xu hướng
     * Tạo nhiều đơn hàng trong 30 ngày qua với số lượng khác nhau cho các sách
     */
    private void initializeTrendingOrderData() {
        log.info("Initializing trending order data...");
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        List<Book> books = bookRepository.findAll();
        List<Address> addresses = addressRepository.findAll();
        
        if (customers.isEmpty() || books.isEmpty() || addresses.isEmpty()) {
            log.warn("Not enough data to create trending orders");
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long thirtyDaysAgo = currentTime - (30L * 24 * 60 * 60 * 1000); // 30 ngày trước
        
        // Tạo orders cho các sách khác nhau với tần suất khác nhau để mô phỏng trending
        int[] trendingPattern = {15, 12, 10, 8, 6, 4, 3, 2, 1, 1}; // Số đơn hàng cho từng sách
        
        for (int bookIndex = 0; bookIndex < Math.min(books.size(), trendingPattern.length); bookIndex++) {
            Book book = books.get(bookIndex);
            int orderCount = trendingPattern[bookIndex];
            
            // Tạo nhiều đơn hàng cho sách này trong 30 ngày qua
            for (int orderIndex = 0; orderIndex < orderCount; orderIndex++) {
                User customer = customers.get(orderIndex % customers.size());
                Address address = addresses.stream()
                        .filter(addr -> addr.getUser().equals(customer))
                        .findFirst()
                        .orElse(addresses.get(0));
                
                // Random thời gian trong 30 ngày qua
                long orderTime = thirtyDaysAgo + (long)(Math.random() * (currentTime - thirtyDaysAgo));
                
                Order order = createTrendingOrder(customer, address, OrderStatus.DELIVERED, "NORMAL", orderTime);
                orderRepository.save(order);
                
                // Tạo order detail với số lượng random
                int quantity = 1 + (int)(Math.random() * 3); // 1-3 cuốn
                BigDecimal unitPrice = book.getPrice();
                
                OrderDetail detail = createOrderDetail(order, book, quantity, unitPrice);
                orderDetailRepository.save(detail);
                
                // Cập nhật tổng tiền đơn hàng
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                order.setSubtotal(subtotal);
                order.setTotalAmount(subtotal.add(order.getShippingFee()));
                orderRepository.save(order);
                
                // Thêm một số đơn hàng có nhiều sách
                if (orderIndex % 3 == 0 && bookIndex < books.size() - 1) {
                    Book secondBook = books.get(bookIndex + 1);
                    int secondQuantity = 1 + (int)(Math.random() * 2);
                    BigDecimal secondUnitPrice = secondBook.getPrice();
                    
                    OrderDetail secondDetail = createOrderDetail(order, secondBook, secondQuantity, secondUnitPrice);
                    orderDetailRepository.save(secondDetail);
                    
                    BigDecimal additionalAmount = secondUnitPrice.multiply(BigDecimal.valueOf(secondQuantity));
                    order.setSubtotal(order.getSubtotal().add(additionalAmount));
                    order.setTotalAmount(order.getTotalAmount().add(additionalAmount));
                    orderRepository.save(order);
                }
            }
        }
        
        log.info("Created {} trending orders", Arrays.stream(trendingPattern).sum());
    }
    
    /**
     * Tạo Order với thời gian tùy chỉnh cho trending data
     */
    private Order createTrendingOrder(User customer, Address address, OrderStatus status, String orderType, long orderTime) {
        Order order = new Order();
        order.setUser(customer);
        order.setAddress(address);
        order.setOrderDate(orderTime); // Sử dụng thời gian tùy chỉnh
        order.setSubtotal(BigDecimal.ZERO);
        order.setShippingFee(new BigDecimal("30000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setDiscountShipping(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderStatus(status);
        order.setOrderType(orderType);
        order.setCode("TRD" + orderTime + "_" + customer.getId()); // Code khác để phân biệt
        order.setCreatedBy(customer.getId());
        order.setCreatedAt(orderTime);
        order.setUpdatedAt(orderTime);
        order.setStatus((byte) 1);
        return order;
    }

    /**
     * ✅ THÊM METHOD: Tạo thêm review để có đánh giá cho trending products
     */
    private void initializeTrendingReviewData() {
        log.info("Initializing trending review data...");
        
        List<User> customers = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        List<Book> books = bookRepository.findAll();
        
        if (customers.isEmpty() || books.isEmpty()) {
            log.warn("Not enough data to create trending reviews");
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long thirtyDaysAgo = currentTime - (30L * 24 * 60 * 60 * 1000);
        
        String[] positiveComments = {
            "Sách tuyệt vời! Rất hữu ích và hay.",
            "Nội dung phong phú, viết rất dễ hiểu.",
            "Đáng đọc! Tôi học được nhiều điều.",
            "Sách chất lượng cao, giá cả hợp lý.",
            "Rất thích cách tác giả trình bày.",
            "Một trong những cuốn sách hay nhất tôi từng đọc.",
            "Nội dung sâu sắc và ý nghĩa.",
            "Đọc xong có cảm giác rất thỏa mãn.",
            "Sách được viết rất tâm huyết và chất lượng.",
            "Recommend cho mọi người đọc!"
        };
        
        String[] neutralComments = {
            "Sách bình thường, có thể đọc.",
            "Nội dung ổn, phù hợp với một số người.",
            "Không quá hay nhưng cũng không tệ.",
            "Có những phần hay, có những phần chưa thực sự ấn tượng."
        };
        
        // Tạo review cho các sách với pattern khác nhau
        int[] reviewPattern = {25, 20, 18, 15, 12, 10, 8, 6, 4, 3}; // Số review cho từng sách
        double[] ratingPattern = {4.8, 4.6, 4.5, 4.3, 4.2, 4.0, 3.8, 3.5, 3.2, 3.0}; // Rating trung bình
        
        for (int bookIndex = 0; bookIndex < Math.min(books.size(), reviewPattern.length); bookIndex++) {
            Book book = books.get(bookIndex);
            int reviewCount = reviewPattern[bookIndex];
            double avgRating = ratingPattern[bookIndex];
            
            for (int reviewIndex = 0; reviewIndex < reviewCount; reviewIndex++) {
                User customer = customers.get(reviewIndex % customers.size());
                
                // Tạo rating xung quanh average rating
                int rating;
                if (avgRating >= 4.5) {
                    rating = (Math.random() < 0.8) ? 5 : 4; // 80% rating 5, 20% rating 4
                } else if (avgRating >= 4.0) {
                    rating = (Math.random() < 0.6) ? 5 : ((Math.random() < 0.8) ? 4 : 3); // 60% rating 5, 20% rating 4, 20% rating 3
                } else if (avgRating >= 3.5) {
                    rating = (Math.random() < 0.4) ? 4 : ((Math.random() < 0.7) ? 3 : 2); // 40% rating 4, 30% rating 3, 30% rating 2
                } else {
                    rating = (Math.random() < 0.3) ? 4 : ((Math.random() < 0.6) ? 3 : 2); // 30% rating 4, 30% rating 3, 40% rating 2
                }
                
                // Chọn comment phù hợp với rating và xác định isPositive
                String comment;
                Boolean isPositive;
                if (rating >= 4) {
                    comment = positiveComments[(int)(Math.random() * positiveComments.length)];
                    isPositive = true; // Rating >= 4 được coi là tích cực
                } else if (rating == 3) {
                    comment = neutralComments[(int)(Math.random() * neutralComments.length)];
                    isPositive = Math.random() < 0.5 ? true : false; // Rating 3 có thể tích cực hoặc tiêu cực
                } else {
                    comment = neutralComments[(int)(Math.random() * neutralComments.length)];
                    isPositive = false; // Rating <= 2 được coi là tiêu cực
                }
                
                // Random thời gian review trong 30 ngày qua
                long reviewTime = thirtyDaysAgo + (long)(Math.random() * (currentTime - thirtyDaysAgo));
                
                Review review = Review.builder()
                        .book(book)
                        .user(customer)
                        .rating(rating)
                        .comment(comment)
                        .isPositive(isPositive) // ✅ THÊM: Thiết lập isPositive
                        .reviewDate(reviewTime)
                        .reviewStatus(ReviewStatus.APPROVED)
                        .createdAt(reviewTime)
                        .updatedAt(reviewTime)
                        .build();
                reviewRepository.save(review);
            }
        }
        
        log.info("Created {} trending reviews", Arrays.stream(reviewPattern).sum());
    }
    
    /**
     * ✅ THÊM METHOD: Tạo dữ liệu đơn hàng test theo thời gian cho Lê Văn C 
     * Mua sách "Đắc Nhân Tâm" từ 2023 đến nay với tần suất khác nhau
     */
    private void initializeTestOrdersForLeVanC() {
        log.info("Initializing test orders for Lê Văn C with time-based data...");
        
        // Tìm user Lê Văn C
        User leVanC = userRepository.findByEmail("customer1@gmail.com").orElse(null);
        if (leVanC == null) {
            log.warn("Lê Văn C not found, skipping test order creation");
            return;
        }
        
        // Tìm sách "Đắc Nhân Tâm"
        Book dacNhanTam = bookRepository.findAll().stream()
                .filter(book -> book.getBookName().contains("Đắc Nhân Tâm"))
                .findFirst().orElse(null);
        if (dacNhanTam == null) {
            log.warn("Book 'Đắc Nhân Tâm' not found, skipping test order creation");
            return;
        }
        
        // Tìm địa chỉ của Lê Văn C
        Address address = addressRepository.findAll().stream()
                .filter(addr -> addr.getUser().equals(leVanC))
                .findFirst().orElse(null);
        if (address == null) {
            log.warn("Address for Lê Văn C not found, skipping test order creation");
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long oneDay = 24L * 60 * 60 * 1000;
        long oneMonth = 30L * oneDay;
        long oneYear = 365L * oneDay;
        
        // Pattern: Tạo đơn hàng theo các mốc thời gian khác nhau
        List<OrderTestData> orderPattern = Arrays.asList(
            // === 2023 - Ít đơn hàng ===
            new OrderTestData(currentTime - oneYear - 6 * oneMonth, 1), // Tháng 2/2023
            new OrderTestData(currentTime - oneYear - 3 * oneMonth, 1), // Tháng 5/2023  
            new OrderTestData(currentTime - oneYear - oneMonth, 2),     // Tháng 7/2023
            new OrderTestData(currentTime - oneYear, 1),               // Tháng 8/2023
            
            // === Q4/2023 - Tăng dần ===
            new OrderTestData(currentTime - oneYear + oneMonth, 2),    // Tháng 9/2023
            new OrderTestData(currentTime - oneYear + 2 * oneMonth, 3), // Tháng 10/2023
            new OrderTestData(currentTime - oneYear + 3 * oneMonth, 2), // Tháng 11/2023
            new OrderTestData(currentTime - oneYear + 4 * oneMonth, 4), // Tháng 12/2023
            
            // === Q1/2024 - Khá tốt ===
            new OrderTestData(currentTime - 7 * oneMonth, 3),          // Tháng 1/2024
            new OrderTestData(currentTime - 6 * oneMonth, 4),          // Tháng 2/2024  
            new OrderTestData(currentTime - 5 * oneMonth, 5),          // Tháng 3/2024
            
            // === Q2/2024 - Giảm ===
            new OrderTestData(currentTime - 4 * oneMonth, 2),          // Tháng 4/2024
            new OrderTestData(currentTime - 3 * oneMonth, 1),          // Tháng 5/2024
            new OrderTestData(currentTime - 2 * oneMonth, 3),          // Tháng 6/2024
            
            // === Q3/2024 (Tháng 7) - Tăng mạnh ===
            new OrderTestData(currentTime - oneMonth, 8),              // Tháng 7/2024
            
            // === 1 tuần gần đây - Mua rất nhiều ===
            new OrderTestData(currentTime - 6 * oneDay, 3),           // 6 ngày trước
            new OrderTestData(currentTime - 5 * oneDay, 2),           // 5 ngày trước
            new OrderTestData(currentTime - 4 * oneDay, 4),           // 4 ngày trước  
            new OrderTestData(currentTime - 3 * oneDay, 1),           // 3 ngày trước
            new OrderTestData(currentTime - 2 * oneDay, 5),           // 2 ngày trước
            new OrderTestData(currentTime - oneDay, 3),               // 1 ngày trước
            new OrderTestData(currentTime, 2)                        // Hôm nay
        );
        
        int totalOrders = 0;
        
        // Tạo đơn hàng theo pattern
        for (OrderTestData testData : orderPattern) {
            for (int i = 0; i < testData.orderCount; i++) {
                // Thêm random offset để đơn hàng không bị trùng timestamp
                long randomOffset = (long) (Math.random() * 2 * 60 * 60 * 1000); // Random 0-2 giờ
                long orderTime = testData.timestamp + randomOffset;
                
                // Tạo đơn hàng
                Order order = createTestOrder(leVanC, address, OrderStatus.DELIVERED, "ONLINE", orderTime);
                orderRepository.save(order);
                
                // Tạo order detail cho sách Đắc Nhân Tâm
                int quantity = 1 + (int) (Math.random() * 2); // Mua 1-2 cuốn
                OrderDetail detail = createOrderDetail(order, dacNhanTam, quantity, dacNhanTam.getPrice());
                orderDetailRepository.save(detail);
                
                // Cập nhật tổng tiền đơn hàng
                BigDecimal subtotal = dacNhanTam.getPrice().multiply(BigDecimal.valueOf(quantity));
                order.setSubtotal(subtotal);
                order.setTotalAmount(subtotal.add(order.getShippingFee()));
                orderRepository.save(order);
                
                totalOrders++;
            }
        }
        
        log.info("Created {} test orders for Lê Văn C spanning from 2023 to present", totalOrders);
        log.info("Orders distributed across different quarters and months for testing");
        log.info("Recent week has the most orders, previous month has good amount");
    }
    
    /**
     * Tạo Order với thời gian tùy chỉnh cho test data
     */
    private Order createTestOrder(User customer, Address address, OrderStatus status, String orderType, long orderTime) {
        Order order = new Order();
        order.setUser(customer);
        order.setAddress(address);
        order.setOrderDate(orderTime); // Sử dụng thời gian tùy chỉnh
        order.setSubtotal(BigDecimal.ZERO);
        order.setShippingFee(new BigDecimal("30000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setDiscountShipping(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderStatus(status);
        order.setOrderType(orderType);
        order.setCode("TEST" + System.currentTimeMillis() + "_" + Math.random());
        order.setCreatedBy(customer.getId());
        order.setStatus((byte) 1);
        return order;
    }
    
    /**
     * Helper class cho test data pattern
     */
    private static class OrderTestData {
        long timestamp;
        int orderCount;
        
        OrderTestData(long timestamp, int orderCount) {
            this.timestamp = timestamp;
            this.orderCount = orderCount;
        }
    }
}
