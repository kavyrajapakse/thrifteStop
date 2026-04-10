# ThrifteStop

ThrifteStop is a Java EE web application for buying and managing second-hand fashion items.
It includes a customer storefront, cart and checkout flow, profile management, and an admin panel for products, orders, inventory, and sales.

## Features

- Customer authentication (sign up, sign in, sign out)
- Password reset with email verification code
- Product browsing with filters and search
- Product detail view with related items
- Cart management and checkout flow
- Purchase history and invoice email sending
- Profile and address update support
- Admin dashboard with:
  - Product creation and updates
  - Product status updates
  - Inventory view
  - Order list and order status updates
  - Sales and customer pages

## Tech Stack

- Java EE 7 (Servlet 3)
- Hibernate ORM 4.3.x
- MySQL
- GlassFish Server 5
- NetBeans Ant-based web project
- Frontend: HTML, Tailwind CSS (CDN), vanilla JavaScript
- Libraries: Gson, JavaMail

## Demo Video

Watch the full project demo on YouTube:

- [ThrifteStop Demo](https://youtu.be/TjS59g0gpL8?si=rLN_YClWCrau2LiW)

## Project Structure

- `src/java/controller` - Customer-facing servlets
- `src/java/controller/Admin` - Admin servlets
- `src/java/hibernate` - Entity classes, Hibernate utility, email utility
- `web` - HTML pages and static assets
- `web/resources/items` - Product images
- `web/WEB-INF` - Server descriptors
- `nbproject` - NetBeans project metadata

## Prerequisites

- JDK 11 (project compiles source/target 1.7; current config uses JDK_11)
- NetBeans (recommended for this project layout)
- GlassFish 5.x
- MySQL 8+

## Setup and Run (NetBeans + GlassFish)

1. Clone this repository.
2. Open the project folder in NetBeans.
3. Ensure GlassFish 5 is registered in NetBeans and selected as the project server.
4. Create the MySQL database and required tables (see Database section below).
5. Update database credentials in `src/java/hibernate.cfg.xml`.
6. Update email credentials in `src/java/hibernate/EmailUtil.java`.
7. Clean and Build the project.
8. Run/Deploy to GlassFish.

Default app context path is usually:

- `http://localhost:8080/ThrifteStop/`

Main pages:

- Customer auth: `index.html`
- Home: `home.html`
- Product list: `productList.html`
- Cart: `cart.html`
- Checkout invoice page: `invoice.html`
- Admin login: `adminSignIn.html`
- Admin dashboard: `adminDashboard.html`

## Database Configuration

The Hibernate config file is:

- `src/java/hibernate.cfg.xml`

Default connection format:

- `jdbc:mysql://localhost:3306/thriftdb?useSSL=false`

Update the following before running:

- `hibernate.connection.url`
- `hibernate.connection.username`
- `hibernate.connection.password`

Mapped entities include:

- `User`, `City`, `Address`, `Category`, `Color`, `Condition`, `Size`, `Status`
- `Product`, `ProductImage`, `Cart`
- `CustomerOrder`, `OrderItem`, `OrderStatus`
- `ThriftAdmin`

## Email Configuration

Email sending is implemented in:

- `src/java/hibernate/EmailUtil.java`

Used for:

- Password reset verification codes
- Invoice emails after checkout

Important: Move email credentials out of source code before production use (recommended: environment variables or secure server config).

## Key API Endpoints (Servlet Mappings)

Customer/public:

- `POST /login`
- `POST /register`
- `GET /get-product-list`
- `GET /get-product`
- `GET /get-new-arrivals`
- `GET /get-category-counts`
- `GET /get-related-products`
- `POST /AddToCartServlet`
- `GET /LoadCartServlet`
- `GET /LoadCartSummaryServlet`
- `POST /RemoveCartServlet`
- `GET /LoadCheckoutServlet`
- `POST /CheckOutServlet`
- `POST /send-invoice`
- `POST /send-code`
- `POST /verify-code`
- `POST /update-password`
- `GET /get-profile`
- `POST /update-profile`
- `GET /get-customer-orders`

Admin:

- `POST /SaveProduct`
- `POST /UpdateProduct`
- `GET /get-inventory`
- `GET /get-orders`
- `GET /get-order-items`
- `GET /get-order-statuses`
- `POST /update-order-status`
- `GET /get-product-statuses`
- `POST /update-product-status`
- `POST /AdminLogout`


