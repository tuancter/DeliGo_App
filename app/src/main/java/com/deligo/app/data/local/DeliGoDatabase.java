package com.deligo.app.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.deligo.app.data.local.dao.CartDao;
import com.deligo.app.data.local.dao.CartItemsDao;
import com.deligo.app.data.local.dao.CategoriesDao;
import com.deligo.app.data.local.dao.ComplaintsDao;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.dao.OrderDetailsDao;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.dao.ReviewsDao;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.CartEntity;
import com.deligo.app.data.local.entity.CartItemEntity;
import com.deligo.app.data.local.entity.CategoryEntity;
import com.deligo.app.data.local.entity.ComplaintEntity;
import com.deligo.app.data.local.entity.FoodEntity;
import com.deligo.app.data.local.entity.OrderDetailEntity;
import com.deligo.app.data.local.entity.OrderEntity;
import com.deligo.app.data.local.entity.ReviewEntity;
import com.deligo.app.data.local.entity.UserEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                UserEntity.class,
                CategoryEntity.class,
                FoodEntity.class,
                OrderEntity.class,
                OrderDetailEntity.class,
                ReviewEntity.class,
                ComplaintEntity.class,
                CartEntity.class,
                CartItemEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class DeliGoDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "deligo_db";
    private static volatile DeliGoDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();
    private static final String DEFAULT_ADMIN_EMAIL = "admin@deligo.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    public abstract UsersDao usersDao();

    public abstract CategoriesDao categoriesDao();

    public abstract FoodsDao foodsDao();

    public abstract OrdersDao ordersDao();

    public abstract OrderDetailsDao orderDetailsDao();

    public abstract CartDao cartDao();

    public abstract CartItemsDao cartItemsDao();

    public abstract ReviewsDao reviewsDao();

    public abstract ComplaintsDao complaintsDao();

    public static void resetInstance(@NonNull Context context) {
        synchronized (DeliGoDatabase.class) {
            if (INSTANCE != null) {
                try {
                    INSTANCE.close();
                } finally {
                    INSTANCE = null;
                }
            }
        }
        context.deleteDatabase(DATABASE_NAME);
    }

    public static DeliGoDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (DeliGoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DeliGoDatabase.class,
                                    DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Keep admin account ensured upon initial DB creation
                                    seedDefaultAdminAccountAsync();
                                    // Mock data seeding is triggered from app startup (MainActivity)
                                    // via ensureMockDataSeededAsync() to avoid duplicate seeding.
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Keep admin account in sync on every open, but only seed mock data on first create
                                    seedDefaultAdminAccountAsync();
                                }

                                private void seedDefaultAdminAccountAsync() {
                                    databaseWriteExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            seedDefaultAdminAccount();
                                        }
                                    });
                                }

                                private void seedMockDataAsync() {
                                    databaseWriteExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            seedMockData();
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedDefaultAdminAccount() {
        DeliGoDatabase database = INSTANCE;
        if (database == null) {
            return;
        }

        UsersDao usersDao = database.usersDao();
        UserEntity existingAdmin = usersDao.getUserByEmail(DEFAULT_ADMIN_EMAIL);
        if (existingAdmin == null) {
            UserEntity adminUser = new UserEntity();
            adminUser.setFullName("Quản trị DeliGo");
            adminUser.setEmail(DEFAULT_ADMIN_EMAIL);
            adminUser.setPassword(DEFAULT_ADMIN_PASSWORD);
            adminUser.setRole("Admin");
            adminUser.setStatus("Active");
            usersDao.insert(adminUser);
            return;
        }

        boolean needsUpdate = false;

        if (!DEFAULT_ADMIN_PASSWORD.equals(existingAdmin.getPassword())) {
            existingAdmin.setPassword(DEFAULT_ADMIN_PASSWORD);
            needsUpdate = true;
        }

        if (!"Admin".equalsIgnoreCase(existingAdmin.getRole())) {
            existingAdmin.setRole("Admin");
            needsUpdate = true;
        }

        if (!"Active".equalsIgnoreCase(existingAdmin.getStatus())) {
            existingAdmin.setStatus("Active");
            needsUpdate = true;
        }

        if (existingAdmin.getFullName() == null || existingAdmin.getFullName().trim().isEmpty()) {
            existingAdmin.setFullName("Quản trị DeliGo");
            needsUpdate = true;
        }

        if (needsUpdate) {
            usersDao.update(existingAdmin);
        }
    }

    private static void seedMockData() {
        DeliGoDatabase db = INSTANCE;
        if (db == null) return;

        // 1) Seed Users (Owner, Customers, Shippers)
        UsersDao usersDao = db.usersDao();
        List<Long> customerIds = new ArrayList<>();
        List<Long> shipperIds = new ArrayList<>();

        // Owner
        UserEntity owner = new UserEntity();
        owner.setFullName("Chủ quán DeliGo");
        owner.setEmail("owner@deligo.com");
        owner.setPhone("0900000001");
        owner.setPassword("123456");
        owner.setRole("Owner");
        owner.setStatus("Active");
        owner.setCreatedAt("2025-01-01 08:00:00");
        long ownerId = usersDao.insert(owner);

        // 5 Customers
        for (int i = 1; i <= 5; i++) {
            UserEntity c = new UserEntity();
            c.setFullName("Khách hàng " + i);
            c.setEmail("customer" + i + "@mail.com");
            c.setPhone("09000010" + i);
            c.setPassword("123456");
            c.setRole("Customer");
            c.setStatus("Active");
            c.setCreatedAt("2025-01-01 09:0" + i + ":00");
            long id = usersDao.insert(c);
            customerIds.add(id);
        }

        // 2 Shippers
        for (int i = 1; i <= 2; i++) {
            UserEntity s = new UserEntity();
            s.setFullName("Shipper " + i);
            s.setEmail("shipper" + i + "@mail.com");
            s.setPhone("09000020" + i);
            s.setPassword("123456");
            s.setRole("Shipper");
            s.setStatus("Active");
            s.setCreatedAt("2025-01-01 10:0" + i + ":00");
            long id = usersDao.insert(s);
            shipperIds.add(id);
        }

        // 2) Seed Categories (5)
        CategoriesDao categoriesDao = db.categoriesDao();
        List<String> categoryNames = Arrays.asList("Pizza", "Burger", "Đồ uống", "Tráng miệng", "Mì Ý");
        List<Long> categoryIds = new ArrayList<>();
        for (String name : categoryNames) {
            CategoryEntity cat = new CategoryEntity();
            cat.setCategoryName(name);
            long id = categoriesDao.insert(cat);
            categoryIds.add(id);
        }

        // 3) Seed Foods (2 per category)
        FoodsDao foodsDao = db.foodsDao();
        List<FoodEntity> foods = new ArrayList<>();

        // Helper to add food
        java.util.function.BiConsumer<String, FoodEntity> finalizeFood = (n, f) -> {
            f.setName(n);
            f.setAvailable(true);
            long id = foodsDao.insert(f);
            f.setFoodId(id);
            foods.add(f);
        };

        // Pizza
        {
            FoodEntity f1 = new FoodEntity(); f1.setCategoryId(categoryIds.get(0)); f1.setDescription("Pizza xúc xích thơm ngon"); f1.setPrice(99000); f1.setImageUrl("https://img.dominos.vn/cach-lam-banh-pizza-xuc-xich-4.jpg");
            finalizeFood.accept("Pizza Xúc Xích", f1);
            FoodEntity f2 = new FoodEntity(); f2.setCategoryId(categoryIds.get(0)); f2.setDescription("Pizza hải sản đặc biệt"); f2.setPrice(129000); f2.setImageUrl("https://thepizzacompany.vn/images/thumbs/000/0002214_sf-deluxe_500.png");
            finalizeFood.accept("Pizza Hải Sản", f2);
        }
        // Burger
        {
            FoodEntity f1 = new FoodEntity(); f1.setCategoryId(categoryIds.get(1)); f1.setDescription("Burger bò phô mai"); f1.setPrice(59000); f1.setImageUrl("https://product.hstatic.net/200000848723/product/2_356e19838a61405292c8b5bb03ce4075_master.jpg");
            finalizeFood.accept("Burger Bò", f1);
            FoodEntity f2 = new FoodEntity(); f2.setCategoryId(categoryIds.get(1)); f2.setDescription("Burger gà giòn"); f2.setPrice(49000); f2.setImageUrl("https://product.hstatic.net/200000848723/product/37_4032a7a363934af3bf9d15a93ff08e19_grande.jpg");
            finalizeFood.accept("Burger Gà", f2);
        }
        // Drinks
        {
            FoodEntity f1 = new FoodEntity(); f1.setCategoryId(categoryIds.get(2)); f1.setDescription("Nước ngọt Coca-Cola"); f1.setPrice(15000); f1.setImageUrl("https://www.lottemart.vn/media/catalog/product/cache/0x0/8/9/8935049501503-2.jpg.webp");
            finalizeFood.accept("Coca-Cola", f1);
            FoodEntity f2 = new FoodEntity(); f2.setCategoryId(categoryIds.get(2)); f2.setDescription("Trà sữa trân châu"); f2.setPrice(35000); f2.setImageUrl("https://vietnam-tea.com/wp-content/uploads/2022/02/boba-milk-tea.jpg");
            finalizeFood.accept("Trà Sữa", f2);
        }
        // Desserts
        {
            FoodEntity f1 = new FoodEntity(); f1.setCategoryId(categoryIds.get(3)); f1.setDescription("Bánh phô mai"); f1.setPrice(45000); f1.setImageUrl("https://defencebakery.in/cdn/shop/files/Baked_Blueberry_Cheese_Cake_Slice_ef445093-dd01-4787-abff-71405d70fadc.png?v=1756545001");
            finalizeFood.accept("Cheesecake", f1);
            FoodEntity f2 = new FoodEntity(); f2.setCategoryId(categoryIds.get(3)); f2.setDescription("Bánh flan caramel"); f2.setPrice(25000); f2.setImageUrl("https://scruffandsteph.com/wp-content/uploads/2020/11/Banh-Flan-5.jpg");
            finalizeFood.accept("Bánh Flan", f2);
        }
        // Pasta
        {
            FoodEntity f1 = new FoodEntity(); f1.setCategoryId(categoryIds.get(4)); f1.setDescription("Mì Ý bò bằm truyền thống"); f1.setPrice(79000); f1.setImageUrl("https://file.hstatic.net/200000700229/article/mi-y-sot-bo-bam-thumb_033c1c63fa4f4f1c89c6440b9326da21.jpg");
            finalizeFood.accept("Mì Ý Bò Bằm", f1);
            FoodEntity f2 = new FoodEntity(); f2.setCategoryId(categoryIds.get(4)); f2.setDescription("Mì Ý hải sản sốt kem"); f2.setPrice(99000); f2.setImageUrl("https://beptruong.edu.vn/wp-content/uploads/2018/07/mon-mi-y-hai-san.jpg");
            finalizeFood.accept("Mì Ý Hải Sản", f2);
        }

        // 4) Seed Carts for first 5 customers
        CartDao cartDao = db.cartDao();
        CartItemsDao cartItemsDao = db.cartItemsDao();
        List<Long> cartIds = new ArrayList<>();
        for (int i = 0; i < Math.min(5, customerIds.size()); i++) {
            CartEntity cart = new CartEntity();
            cart.setUserId(customerIds.get(i));
            cart.setCreatedAt("2025-01-02 08:0" + (i + 1) + ":00");
            cart.setUpdatedAt("2025-01-02 09:0" + (i + 1) + ":00");
            long cartId = cartDao.insert(cart);
            cartIds.add(cartId);

            // Add 2 items into each cart
            if (!foods.isEmpty()) {
                int idx1 = (i * 2) % foods.size();
                int idx2 = (i * 2 + 1) % foods.size();
                FoodEntity a = foods.get(idx1);
                FoodEntity b = foods.get(idx2);

                CartItemEntity ci1 = new CartItemEntity();
                ci1.setCartId(cartId);
                ci1.setFoodId(a.getFoodId());
                ci1.setQuantity(1 + (i % 2));
                ci1.setPrice(a.getPrice());
                ci1.setNote("Thêm phô mai");
                cartItemsDao.insert(ci1);

                CartItemEntity ci2 = new CartItemEntity();
                ci2.setCartId(cartId);
                ci2.setFoodId(b.getFoodId());
                ci2.setQuantity(1);
                ci2.setPrice(b.getPrice());
                ci2.setNote("");
                cartItemsDao.insert(ci2);
            }
        }

        // 5) Seed Orders with OrderDetails (>=5)
        OrdersDao ordersDao = db.ordersDao();
        for (int i = 0; i < 5; i++) {
            long customerId = customerIds.get(i % customerIds.size());
            Long shipperId = (i % 2 == 0 && !shipperIds.isEmpty()) ? shipperIds.get(i % shipperIds.size()) : null;

            OrderEntity order = new OrderEntity();
            order.setCustomerId(customerId);
            order.setShipperId(shipperId);
            order.setDeliveryAddress("Số " + (10 + i) + ", Đường ABC, Quận 1, TP.HCM");
            order.setPaymentMethod(i % 2 == 0 ? "Tiền mặt" : "Thẻ");
            order.setPaymentStatus(i % 3 == 0 ? "Đã thanh toán" : "Chưa thanh toán");
            String status;
            switch (i) {
                case 0: status = "Chờ xác nhận"; break;
                case 1: status = "Đang chuẩn bị"; break;
                case 2: status = "Sẵn sàng giao"; break;
                case 3: status = "Đang giao"; break;
                default: status = "Hoàn tất"; break;
            }
            order.setOrderStatus(status);
            order.setPhone("09123456" + i);
            order.setNote(i % 2 == 0 ? "Gọi trước khi giao" : "");
            order.setCreatedAt("2025-01-03 12:0" + (i + 1) + ":00");

            // Details: pick two foods
            List<OrderDetailEntity> details = new ArrayList<>();
            int idx1 = (i * 2) % foods.size();
            int idx2 = (i * 2 + 1) % foods.size();
            FoodEntity fa = foods.get(idx1);
            FoodEntity fb = foods.get(idx2);

            OrderDetailEntity d1 = new OrderDetailEntity();
            d1.setFoodId(fa.getFoodId());
            d1.setQuantity(1);
            d1.setUnitPrice(fa.getPrice());
            details.add(d1);

            OrderDetailEntity d2 = new OrderDetailEntity();
            d2.setFoodId(fb.getFoodId());
            d2.setQuantity(2);
            d2.setUnitPrice(fb.getPrice());
            details.add(d2);

            double total = d1.getUnitPrice() * d1.getQuantity() + d2.getUnitPrice() * d2.getQuantity();
            order.setTotalAmount(total);

            ordersDao.insertOrderWithDetails(order, details);
        }

        // 6) Seed Reviews (>=5)
        ReviewsDao reviewsDao = db.reviewsDao();
        for (int i = 0; i < 5; i++) {
            ReviewEntity r = new ReviewEntity();
            r.setUserId(customerIds.get(i % customerIds.size()));
            r.setFoodId(foods.get(i % foods.size()).getFoodId());
            r.setRating(4 + (i % 2));
            r.setComment("Rất ngon! " + (i + 1));
            r.setCreatedAt("2025-01-04 15:0" + (i + 1) + ":00");
            reviewsDao.insert(r);
        }

        // 7) Seed Complaints (>=5) linked to first 5 orders
        ComplaintsDao complaintsDao = db.complaintsDao();
        for (int i = 0; i < 5; i++) {
            // We don't have direct order IDs returned from insertOrderWithDetails above,
            // because it returns inside the DAO. To ensure we can reference orders for complaints,
            // we'll insert additional simple orders to reference here.
            OrderEntity refOrder = new OrderEntity();
            long custId = customerIds.get(i % customerIds.size());
            refOrder.setCustomerId(custId);
            refOrder.setShipperId(null);
            refOrder.setDeliveryAddress("Số " + (100 + i) + ", Đường XYZ, Q.3, TP.HCM");
            refOrder.setPaymentMethod("Tiền mặt");
            refOrder.setPaymentStatus("Chưa thanh toán");
            refOrder.setOrderStatus("Bị hủy");
            refOrder.setPhone("09876543" + i);
            refOrder.setNote("Đặt nhầm món");
            refOrder.setCreatedAt("2025-01-05 10:1" + i + ":00");
            long refOrderId = ordersDao.insertOrder(refOrder);

            ComplaintEntity comp = new ComplaintEntity();
            comp.setUserId(custId);
            comp.setOrderId(refOrderId);
            comp.setContent("Phàn nàn số " + (i + 1));
            comp.setStatus("Chờ xử lý");
            comp.setCreatedAt("2025-01-05 11:1" + i + ":00");
            complaintsDao.insert(comp);
        }
    }

    // Ensures mock data is present when app starts (e.g., from MainActivity)
    // Runs on background thread and only seeds when key tables are empty to avoid duplicates.
    public static void ensureMockDataSeededAsync() {
        final DeliGoDatabase database = INSTANCE;
        if (database == null) {
            return;
        }
        databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long categoryCount = database.categoriesDao().count();
                    if (categoryCount == 0) {
                        // Admin account is handled separately on open; seed the rest now.
                        seedMockData();
                    }
                } catch (Exception ignored) {
                    // No-op: avoid crashing app if seeding check fails
                }
            }
        });
    }
}
