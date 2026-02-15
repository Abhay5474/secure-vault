

# 🛡️ Project Documentation: Secure Vault

### *An Enterprise-Grade Digital Rights Management (DRM) & Secure File Sharing System*

---

## 1. Executive Summary

**Secure Vault** is a secure file storage and sharing platform designed to solve the critical flaw in traditional cloud storage services (like Google Drive or Dropbox): **Loss of Control**.

In traditional systems, once a file is downloaded, the owner loses all control over who views or shares it. Secure Vault introduces a **"Remote Control"** paradigm using a proprietary encrypted file format (`.sntl`). Files downloaded from the platform remain encrypted at rest and require real-time server authentication to decrypt, ensuring that access can be **revoked instantly**, even after the file has left the cloud.

---

## 2. Problem Statement vs. Solution

| The Problem (Traditional Cloud) | The Secure Vault Solution |
| --- | --- |
| **Data Leakage:** Downloaded files can be copied to USBs or emailed to unauthorized users. | **Device Locking:** Files are encrypted with AES-256. If copied to an unauthorized device/user, the server denies the decryption key. |
| **Permanent Access:** Once shared, you cannot "un-share" a file already on someone's hard drive. | **Instant Revocation:** Owners can revoke access on the dashboard. The next time the user tries to open the local file, it fails. |
| **Standard Formats:** PDFs and Docs are easily scanned by malware or modified by editors. | **Custom Format (`.sntl`):** A proprietary extension that effectively "walls off" the content from standard viewers. |

---

## 3. System Architecture

The system follows a **Decoupled Monolithic Architecture** deployed on a cloud infrastructure, emphasizing security at every layer.

### **3.1 High-Level Data Flow**

1. **Upload:** User uploads a file  React Frontend reads file  Streamed to Backend.
2. **Encryption:** Backend generates a unique **AES-256 Key** for this specific file  Encrypts file bytes in memory.
3. **Storage:** The Encrypted Bytes (Blob) are saved directly to the **TiDB Database** (ensuring persistence). The Metadata (Name, Owner, Key) is stored in linked tables.
4. **Access:** User requests file  Server verifies JWT Token & Permissions  Server decrypts bytes in memory  Streams secure data to Client.

---

## 4. Technology Stack

### **Frontend (The Client)**

* **Framework:** React.js (Vite)
* **Styling:** Tailwind CSS (Responsive, Dark Mode)
* **State Management:** React Hooks (`useState`, `useEffect`, `useContext`)
* **HTTP Client:** Axios (Interceptors for JWT handling)
* **Routing:** React Router DOM (v6)

### **Backend (The Core)**

* **Language:** Java 17+
* **Framework:** Spring Boot 3.0 (Spring Web, Spring Security, Spring Data JPA)
* **Security:** JSON Web Tokens (JWT), BCrypt Password Hashing
* **Cryptography:** Java Cryptography Architecture (JCA) - AES/ECB/PKCS5Padding
* **Build Tool:** Maven

### **Database (The Vault)**

* **Primary DB:** TiDB (Distributed MySQL-Compatible Database)
* **Storage Engine:** Hybrid Transactional/Analytical Processing (HTAP) capabilities.

### **Infrastructure (DevOps)**

* **Frontend Hosting:** Vercel (CI/CD auto-deployment)
* **Backend Hosting:** Render (Containerized Spring Boot)
* **Database Hosting:** TiDB Cloud

---

## 5. Key Features & Modules

### **5.1 Secure Authentication Module**

* Implements **JWT (JSON Web Token)** based stateless authentication.
* Token expiration handling and secure HttpOnly cookie practices.
* Strict password hashing using **BCrypt** (Work factor 10).

### **5.2 Cryptographic Core (The Engine)**

* **Algorithm:** AES-256 (Advanced Encryption Standard).
* **Key Management:** Every file has its own unique 256-bit key, generated cryptographically secure random number generators (CSPRNG).
* **Zero-Knowledge Potential:** The architecture allows for future upgrades where keys are never stored on the server (client-side encryption).

### **5.3 The ".sntl" Proprietary Format**

* Files are not stored as `.pdf` or `.jpg`. They are converted into a binary stream, encrypted, and tagged with a custom `.sntl` extension.
* This prevents Operating System indexing and unauthorized opening by third-party apps.
* **Automatic ID Stamping:** Files are downloaded as `filename_id_3005.sntl` to prevent version conflicts on client machines.

### **5.4 Granular Permission System**

* **Owner Role:** Can View, Download, Share, Revoke, and Delete.
* **Shared User Role:** Can only View/Download.
* **Revocation Logic:** A real-time check against the `file_shares` table. If a row is deleted, the user loses access immediately, regardless of file possession.

---

## 6. Database Schema

The database is normalized to **3NF (Third Normal Form)** to ensure data integrity.

### **Table 1: `users**`

* `id` (PK): Unique User ID.
* `email`: User's email (Unique, Indexed).
* `password`: BCrypt hashed string.
* `role`: User permission level (USER, ADMIN).

### **Table 2: `user_files_v2` (The Vault)**

* `id` (PK): Unique File ID.
* `file_name`: Original name (e.g., "Report.pdf").
* `file_type`: MIME type (e.g., "application/pdf").
* `file_data`: **LONGBLOB** (Stores the actual AES-encrypted byte array).
* `encrypted_key`: The specific AES key for this file (Base64 encoded).
* `owner_id` (FK): Links to `users` table.
* `upload_time`: Timestamp.

### **Table 3: `file_shares_v2` (Access Control List)**

* `id` (PK): Share Record ID.
* `file_id` (FK): Links to `user_files_v2`.
* `shared_to_user_id` (FK): Links to `users`.
* *Constraint:* Unique combination of `file_id` + `shared_to_user_id` (prevents duplicate sharing).

---

## 7. API Documentation (Key Endpoints)

All endpoints are prefixed with `/api` and require a valid `Bearer Token` in the header.

| Method | Endpoint | Description | Access |
| --- | --- | --- | --- |
| **POST** | `/auth/register` | Register a new user | Public |
| **POST** | `/auth/login` | Login and receive JWT | Public |
| **POST** | `/files/upload` | Upload & Encrypt a file | Auth User |
| **GET** | `/files` | Get list of user's files | Auth User |
| **GET** | `/files/download/{id}` | Stream decrypted file content | Owner/Shared |
| **POST** | `/files/share` | Grant access to another email | Owner Only |
| **DELETE** | `/files/revoke` | Revoke access from a user | Owner Only |
| **DELETE** | `/files/delete/{id}` | Permanently delete file | Owner Only |

---

## 8. Security Implementation Details

### **8.1 Encryption Workflow**

```java
// Simplified Logic
SecretKey key = KeyGenerator.getInstance("AES").generateKey();
Cipher cipher = Cipher.getInstance("AES");
cipher.init(Cipher.ENCRYPT_MODE, key);
byte[] encryptedData = cipher.doFinal(file.getBytes());

```

This ensures that raw data is **never** written to the disk or database. Even if a hacker dumps the database, they only see garbled ciphertext.

### **8.2 CORS & Network Security**

* **CORS Configuration:** Strictly allows requests *only* from the Vercel Frontend domain.
* **Traffic Encryption:** All data in transit is secured via HTTPS (TLS 1.2/1.3).
* **Database Connection:** Secure socket connection to TiDB Cloud.

---

## 9. Future Scope

1. **Scalable Storage:** Migration from Database BLOB storage to **AWS S3** with Server-Side Encryption (SSE) for handling terabyte-scale data.
2. **Hardware Binding:** Implementing "Device Fingerprinting" to lock a `.sntl` file to a specific CPU serial number.
3. **Audit Logging:** A comprehensive dashboard showing the owner *exactly* when and where their file was accessed (IP Address + Timestamp).
4. **Zero-Knowledge Architecture:** Moving encryption to the Frontend (React) so the server never sees the unencrypted file or the key.

---

## 10. Conclusion

**Secure Vault** successfully demonstrates a functional, robust implementation of modern Digital Rights Management. By combining strong AES encryption with a centralized permission server, it bridges the gap between **Cloud Convenience** and **Enterprise Security**. The project proves that secure file sharing does not require sacrificing user experience, providing a seamless interface for protecting intellectual property in the digital age.

* File with .sntl extension that cannot be opened anywhere excepts our platform, unless admin has not given permission :
  
<img width="940" height="134" alt="image" src="https://github.com/user-attachments/assets/38a1c21b-2450-4cfb-afbe-eefaaf4db9ae" />

Project Snapshots : 

<img width="940" height="1136" alt="image" src="https://github.com/user-attachments/assets/1975239d-9890-4c76-97e2-cd7ff62e62f2" />
<img width="940" height="1059" alt="image" src="https://github.com/user-attachments/assets/ad5a5224-a7d6-4b2e-b08b-ac11056c9df8" />
<img width="940" height="635" alt="image" src="https://github.com/user-attachments/assets/e70a8c74-205d-4f6a-89a5-9f0a17c3f0c5" />

<img width="940" height="770" alt="image" src="https://github.com/user-attachments/assets/db4179ca-ca82-4570-83a6-45130454c2ac" />
<img width="940" height="507" alt="image" src="https://github.com/user-attachments/assets/2df10acb-d97a-4ee0-9a8d-9b8b14a1d7b0" />
<img width="940" height="579" alt="image" src="https://github.com/user-attachments/assets/383694fc-35ef-4c56-8961-0779a470da21" />
<img width="940" height="423" alt="image" src="https://github.com/user-attachments/assets/174fa56f-1907-48c4-ba40-c10410d177c0" />
<img width="940" height="699" alt="image" src="https://github.com/user-attachments/assets/571daca0-8002-452d-8c92-02e4ab6ed47c" />
<img width="940" height="639" alt="image" src="https://github.com/user-attachments/assets/99d02cbf-8153-4803-a96c-f1a49c870886" />
<img width="940" height="822" alt="image" src="https://github.com/user-attachments/assets/ca8da9ae-6f36-4227-8efb-7994e0808f49" />


Access Website : https://secure-vault-project.vercel.app/


https://secure-vault-backend-bh9z.onrender.com/






