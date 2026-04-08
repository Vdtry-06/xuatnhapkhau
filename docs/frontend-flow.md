# Frontend Flow

## 1. So do cac file frontend

```mermaid
flowchart LR
    A["index.js"] --> B["App.js"]
    B --> C["AuthContext.js"]
    B --> D["PrivateRoute.js"]
    B --> E["LoginPage.js"]
    B --> F["agent/ProductsPage.js"]
    B --> G["agent/OrdersPage.js"]
    B --> H["supplier/OrdersPage.js"]

    F --> I["Navbar.js"]
    G --> I
    H --> I
    E --> C
    D --> C

    F --> J["services/api.js"]
    G --> J
    H --> J
    E --> J
```

## 2. Luong dai ly con dat hang online

```mermaid
flowchart TD
    A["LoginPage.js"] -->|"login()"| B["AuthContext.js"]
    B -->|"role = AGENT"| C["App.js -> /agent/products"]
    C --> D["agent/ProductsPage.js"]
    D -->|"GET /api/products"| E["services/api.js"]
    D -->|"POST /api/orders"| E
    D --> F["Them vao gio / nhap dia chi / xac nhan dat hang"]
    F --> G["Order da tao voi status PENDING"]
    G --> H["agent/OrdersPage.js"]
```

## 3. Luong supplier giao xuat hang

```mermaid
flowchart TD
    A["LoginPage.js"] -->|"login()"| B["AuthContext.js"]
    B -->|"role = SUPPLIER"| C["App.js -> /supplier/orders"]
    C --> D["supplier/OrdersPage.js"]
    D -->|"GET /api/orders/supplier/{supplierId}"| E["services/api.js"]
    D --> F["Xem cac don duoc gan supplier"]
    F -->|"PUT /api/orders/{id}/supplier/{supplierId}/ship"| E
    E --> G["Don chuyen sang SHIPPED"]
```

## 4. Luong dai ly con xac nhan da nhan hang

```mermaid
flowchart TD
    A["agent/OrdersPage.js"] -->|"GET /api/orders/agent/{agentId}"| B["services/api.js"]
    A --> C["Chon don dang SHIPPED"]
    C -->|"PUT /api/orders/{id}/agent/{agentId}/deliver"| B
    B --> D["Don chuyen sang DELIVERED"]
```

## 5. Vai tro tung file

| File | Vai tro |
|---|---|
| `frontend/src/App.js` | Khai bao route, gioi han man hinh theo role |
| `frontend/src/context/AuthContext.js` | Luu user, token, role, agentId, supplierId |
| `frontend/src/components/PrivateRoute.js` | Chan truy cap sai role |
| `frontend/src/components/Navbar.js` | Hien menu dung theo role AGENT/SUPPLIER |
| `frontend/src/pages/LoginPage.js` | Dang nhap va dieu huong vao flow dung |
| `frontend/src/pages/agent/ProductsPage.js` | Catalogue + gio hang + tao don online |
| `frontend/src/pages/agent/OrdersPage.js` | Xem don va xac nhan DELIVERED |
| `frontend/src/pages/supplier/OrdersPage.js` | Xem don duoc gan va cap nhat SHIPPED |
| `frontend/src/services/api.js` | Axios client, gan JWT vao request |
