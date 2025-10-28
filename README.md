# 🗺️ Luồng Xử Lý Gợi Ý Địa Chỉ (Address Autocomplete Flow)

**Dự án:** AloTra Website  
**Ngày cập nhật:** 28/10/2025  
**Công nghệ:** Nominatim OpenStreetMap (Frontend) + Nominatim/Google Maps (Backend)

---

## 📋 Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Kiến Trúc Hệ Thống](#2-kiến-trúc-hệ-thống)
3. [Flow Frontend - Autocomplete](#3-flow-frontend---autocomplete)
4. [Flow Backend - Geocoding](#4-flow-backend---geocoding)
5. [Flow Hoàn Chỉnh - Từ Input đến Database](#5-flow-hoàn-chỉnh---từ-input-đến-database)
6. [Các Trường Hợp Xử Lý](#6-các-trường-hợp-xử-lý)
7. [Code Examples](#7-code-examples)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Tổng Quan

### 🎯 Mục đích
Cho phép user nhập địa chỉ giao hàng với **autocomplete (gợi ý tự động)** sử dụng dữ liệu từ **OpenStreetMap** (miễn phí), đồng thời tự động lấy **tọa độ GPS** để tính phí ship và tìm chi nhánh gần nhất.

### 🔑 Điểm Quan Trọng
- ✅ **Frontend ưu tiên Nominatim** (OpenStreetMap) - Miễn phí
- ✅ **Backend có fallback** sang Google Maps nếu Nominatim thất bại
- ✅ **Tọa độ từ client được ưu tiên** (giảm API calls)
- ✅ **Server chỉ geocode khi cần thiết** (không có tọa độ từ client)

### 📊 Ưu Điểm
| Khía cạnh | Lợi ích |
|-----------|---------|
| **Chi phí** | Miễn phí hoàn toàn (Nominatim) |
| **Tốc độ** | Không cần chờ server geocode nếu client đã có tọa độ |
| **Độ chính xác** | Tọa độ từ autocomplete chính xác hơn geocoding text |
| **Trải nghiệm** | User chọn từ dropdown thay vì gõ thủ công |

---

## 2. Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Input: "227 Nguyễn Văn Cừ..."                          │   │
│  │  ↓ (user gõ >= 3 ký tự)                                 │   │
│  │  Dropdown gợi ý hiện ra:                                │   │
│  │  • 227 Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM         │   │
│  │  • 227 Nguyễn Văn Cừ, Phường An Hòa, Ninh Kiều, CT    │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND JAVASCRIPT                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  File: google-maps-loader.js                            │   │
│  │  Class: GoogleMapsLoader                                │   │
│  │                                                          │   │
│  │  createNominatimAutocomplete(inputElement)              │   │
│  │    ↓                                                     │   │
│  │    • Delay 500ms sau khi user ngừng gõ                  │   │
│  │    • Gọi API: nominatim.openstreetmap.org/search        │   │
│  │    • Params: q=<query>&countrycodes=vn&limit=5          │   │
│  │    • Hiển thị dropdown với kết quả                       │   │
│  │    • User click chọn → Trigger event 'nominatim-select' │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                 FRONTEND PAGE HANDLER                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  File: profile.js / checkout.js / register-branch.js    │   │
│  │                                                          │   │
│  │  line1Input.addEventListener('nominatim-select', e => { │   │
│  │    const { address, lat, lng } = e.detail;              │   │
│  │                                                          │   │
│  │    // Parse địa chỉ thành các phần                      │   │
│  │    document.getElementById('line1').value = street;     │   │
│  │    document.getElementById('ward').value = ward;        │   │
│  │    document.getElementById('city').value = city;        │   │
│  │                                                          │   │
│  │    // Lưu tọa độ vào biến global                        │   │
│  │    modalAddressLat = lat;                               │   │
│  │    modalAddressLng = lng;                               │   │
│  │  });                                                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    SUBMIT FORM TO SERVER                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  btnSaveAddress.addEventListener('click', async () => {  │   │
│  │    const address = {                                     │   │
│  │      recipient: "Nguyễn Văn A",                          │   │
│  │      phone: "0901234567",                                │   │
│  │      line1: "227 Nguyễn Văn Cừ",                         │   │
│  │      ward: "Phường 4",                                   │   │
│  │      city: "Thành phố Hồ Chí Minh",                      │   │
│  │      latitude: 10.762622,    // ✅ Từ autocomplete       │   │
│  │      longitude: 106.660172,  // ✅ Từ autocomplete       │   │
│  │      isDefault: true                                     │   │
│  │    };                                                    │   │
│  │                                                          │   │
│  │    // POST to API                                        │   │
│  │    await fetch('/api/addresses', {                       │   │
│  │      method: 'POST',                                     │   │
│  │      headers: {                                          │   │
│  │        'Content-Type': 'application/json',               │   │
│  │        'Authorization': 'Bearer ' + token                │   │
│  │      },                                                  │   │
│  │      body: JSON.stringify(address)                       │   │
│  │    });                                                   │   │
│  │  });                                                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND REST API                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  File: AddressApiController.java                        │   │
│  │                                                          │   │
│  │  @PostMapping                                            │   │
│  │  public ResponseEntity<AddressDTO> create(              │   │
│  │      @RequestBody AddressDTO dto                         │   │
│  │  ) {                                                     │   │
│  │      Long userId = userService.getCurrentUserId();      │   │
│  │      AddressDTO saved =                                  │   │
│  │          addressService.createAddress(userId, dto);     │   │
│  │      return ResponseEntity.ok(saved);                    │   │
│  │  }                                                       │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  File: AddressService.java                              │   │
│  │                                                          │   │
│  │  @Transactional                                          │   │
│  │  public AddressDTO createAddress(userId, dto) {         │   │
│  │                                                          │   │
│  │    // 1. Tạo Address entity                             │   │
│  │    Address address = new Address();                     │   │
│  │    address.setLine1(dto.line1());                       │   │
│  │    address.setWard(dto.ward());                         │   │
│  │    address.setCity(dto.city());                         │   │
│  │                                                          │   │
│  │    // 2. XỬ LÝ TỌA ĐỘ - ƯU TIÊN CLIENT                  │   │
│  │    boolean hasClientCoords =                            │   │
│  │        setIfValidCoordinates(                            │   │
│  │            address,                                      │   │
│  │            dto.latitude(),   // Từ frontend             │   │
│  │            dto.longitude()   // Từ frontend             │   │
│  │        );                                                │   │
│  │                                                          │   │
│  │    // 3. NẾU KHÔNG CÓ TỌA ĐỘ → GEOCODE SERVER-SIDE     │   │
│  │    if (!hasClientCoords) {                              │   │
│  │        String fullAddr = address.getFullAddress();      │   │
│  │        Optional<LatLng> coords =                        │   │
│  │            geocodingService.geocodeAddress(fullAddr);   │   │
│  │                                                          │   │
│  │        coords.ifPresent(ll -> {                         │   │
│  │            address.setLatitude(ll.latitude());          │   │
│  │            address.setLongitude(ll.longitude());        │   │
│  │        });                                               │   │
│  │    }                                                     │   │
│  │                                                          │   │
│  │    // 4. Lưu vào database                               │   │
│  │    Address saved = addressRepository.save(address);     │   │
│  │    return AddressDTO.from(saved);                       │   │
│  │  }                                                       │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    GEOCODING SERVICE                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  File: GeocodingService.java                            │   │
│  │                                                          │   │
│  │  @Cacheable("geocodeCache")                             │   │
│  │  public Optional<LatLng> geocodeAddress(String addr) {  │   │
│  │                                                          │   │
│  │    // 1. Kiểm tra cache trước                           │   │
│  │    // (Spring tự động xử lý)                            │   │
│  │                                                          │   │
│  │    // 2. Thử Google Maps API (nếu có key)               │   │
│  │    if (hasGoogleApiKey()) {                             │   │
│  │        Optional<LatLng> googleResult =                  │   │
│  │            geocodeViaGoogle(addr);                       │   │
│  │        if (googleResult.isPresent())                    │   │
│  │            return googleResult;                         │   │
│  │    }                                                     │   │
│  │                                                          │   │
│  │    // 3. Fallback: Nominatim OpenStreetMap             │   │
│  │    return geocodeViaNominatim(addr);                    │   │
│  │  }                                                       │   │
│  │                                                          │   │
│  │  private Optional<LatLng> geocodeViaNominatim(addr) {   │   │
│  │    // Delay 1.2s để tuân thủ rate limit                 │   │
│  │    Thread.sleep(1200);                                  │   │
│  │                                                          │   │
│  │    // Call API                                           │   │
│  │    String url = "https://nominatim.openstreetmap.org/   │   │
│  │                  search?q=" + encode(addr) +            │   │
│  │                  "&format=json&countrycodes=vn";        │   │
│  │                                                          │   │
│  │    ResponseEntity<String> resp =                        │   │
│  │        restTemplate.getForEntity(url, String.class);    │   │
│  │                                                          │   │
│  │    // Parse JSON                                         │   │
│  │    JsonNode results = objectMapper.readTree(resp);      │   │
│  │    double lat = results.get(0).get("lat").asDouble();   │   │
│  │    double lon = results.get(0).get("lon").asDouble();   │   │
│  │                                                          │   │
│  │    return Optional.of(new LatLng(lat, lon));            │   │
│  │  }                                                       │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         DATABASE                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Table: address                                          │   │
│  │  ┌────┬──────────┬──────┬───────┬────────┬─────┬─────┐  │   │
│  │  │ id │ line1    │ ward │ city  │ lat    │ lng │ ... │  │   │
│  │  ├────┼──────────┼──────┼───────┼────────┼─────┼─────┤  │   │
│  │  │ 1  │ 227 NVC  │ P.4  │ TP.HCM│ 10.762 │106.6│ ... │  │   │
│  │  └────┴──────────┴──────┴───────┴────────┴─────┴─────┘  │   │
│  │                                                          │   │
│  │  ✅ Địa chỉ đã lưu với tọa độ GPS chính xác!            │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Flow Frontend - Autocomplete

### 📱 Bước 1: User Nhập Địa Chỉ

**File:** `profile.js`, `checkout.js`, `register-branch.js`

```javascript
// Khi trang load, khởi tạo autocomplete
await initAddressAutocomplete();

async function initAddressAutocomplete() {
    const line1Input = document.getElementById("line1");
    
    // Gọi loader để tạo autocomplete
    const autocomplete = await window.googleMapsLoader
        .createAutocomplete(line1Input);
    
    // Lắng nghe event khi user chọn địa chỉ
    line1Input.addEventListener('nominatim-select', (e) => {
        handleAddressSelected(e.detail);
    });
}
```

### 🔍 Bước 2: Gọi API Nominatim

**File:** `google-maps-loader.js` → `createNominatimAutocomplete()`

```javascript
createNominatimAutocomplete(input) {
    let timeout = null;
    let dropdown = null;
    
    // Lắng nghe sự kiện nhập liệu
    input.addEventListener('input', (e) => {
        const query = e.target.value.trim();
        
        // Chỉ search khi >= 3 ký tự
        if (query.length < 3) {
            hideDropdown();
            return;
        }
        
        // Debounce: Đợi 500ms sau khi user ngừng gõ
        clearTimeout(timeout);
        timeout = setTimeout(async () => {
            // Gọi API Nominatim
            const url = 
                `https://nominatim.openstreetmap.org/search?` +
                `q=${encodeURIComponent(query)}&` +
                `format=json&` +
                `countrycodes=vn&` +      // Chỉ tìm ở VN
                `limit=5&` +               // Tối đa 5 kết quả
                `addressdetails=1`;        // Bao gồm chi tiết
            
            const res = await fetch(url, {
                headers: { 
                    'User-Agent': 'AloTraWebsite/1.0' 
                }
            });
            
            const data = await res.json();
            showDropdown(data);  // Hiển thị dropdown
            
        }, 500);  // Delay 500ms
    });
}
```

**Ví dụ Request:**
```
GET https://nominatim.openstreetmap.org/search?
    q=227%20Nguy%E1%BB%85n%20V%C4%83n%20C%E1%BB%AB&
    format=json&
    countrycodes=vn&
    limit=5&
    addressdetails=1

User-Agent: AloTraWebsite/1.0
```

**Ví dụ Response:**
```json
[
  {
    "place_id": 123456,
    "display_name": "227, Nguyễn Văn Cừ, Phường 4, Quận 5, Thành phố Hồ Chí Minh, Việt Nam",
    "lat": "10.7626220",
    "lon": "106.6601720",
    "address": {
      "house_number": "227",
      "road": "Nguyễn Văn Cừ",
      "suburb": "Phường 4",
      "city_district": "Quận 5",
      "city": "Thành phố Hồ Chí Minh",
      "country": "Việt Nam"
    }
  }
]
```

### 🎨 Bước 3: Hiển Thị Dropdown

```javascript
const showDropdown = (suggestions) => {
    // Tạo dropdown element
    dropdown = document.createElement('div');
    dropdown.className = 'nominatim-autocomplete-dropdown';
    dropdown.style.cssText = `
        position: fixed;
        background: white;
        border: 1px solid #ddd;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.15);
        max-height: 300px;
        overflow-y: auto;
        z-index: 9999;
    `;
    
    // Tạo item cho mỗi suggestion
    suggestions.forEach(item => {
        const div = document.createElement('div');
        div.className = 'nominatim-suggestion-item';
        div.textContent = item.display_name;
        
        // Xử lý khi click
        div.onclick = () => {
            input.value = item.display_name;
            
            // ⭐ QUAN TRỌNG: Trigger custom event
            const event = new CustomEvent('nominatim-select', {
                detail: {
                    address: item.display_name,
                    lat: parseFloat(item.lat),
                    lng: parseFloat(item.lon)
                }
            });
            input.dispatchEvent(event);
            
            hideDropdown();
        };
        
        dropdown.appendChild(div);
    });
    
    document.body.appendChild(dropdown);
};
```

### 📦 Bước 4: Parse Địa Chỉ Sau Khi Chọn

**File:** `profile.js`

```javascript
line1Input.addEventListener('nominatim-select', (e) => {
    const { address, lat, lng } = e.detail;
    
    // Parse địa chỉ VN: "Số nhà, Đường, Phường, Quận, Thành phố"
    const parts = address.split(',').map(p => p.trim());
    
    // Lọc bỏ postal code và "Việt Nam"
    const filtered = parts.filter(part => {
        if (/^\d{5,6}$/.test(part)) return false;  // Postal code
        if (part.toLowerCase().includes('việt nam')) return false;
        if (part.toLowerCase().includes('vietnam')) return false;
        return true;
    });
    
    // Tìm index của Phường/Xã
    const wardIndex = filtered.findIndex(p => 
        p.includes('Phường') || 
        p.includes('Xã') || 
        p.includes('Thị trấn')
    );
    
    if (wardIndex > 0) {
        // Có phường
        const streetParts = filtered.slice(0, wardIndex);
        const ward = filtered[wardIndex];
        const city = filtered[filtered.length - 1];
        
        document.getElementById("line1").value = streetParts.join(', ');
        document.getElementById("ward").value = ward;
        document.getElementById("city").value = city;
    }
    
    // ⭐ LƯU TỌA ĐỘ VÀO BIẾN GLOBAL
    modalAddressLat = lat;
    modalAddressLng = lng;
    
    console.log('✅ Saved coordinates:', lat, lng);
});
```

**Ví dụ Parse:**

| Input | Output |
|-------|--------|
| `"227, Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM, Việt Nam"` | line1: `"227, Nguyễn Văn Cừ"`<br>ward: `"Phường 4"`<br>city: `"Thành phố Hồ Chí Minh"` |

---

## 4. Flow Backend - Geocoding

### 📨 Bước 1: Nhận Request từ Frontend

**File:** `AddressApiController.java`

```java
@PostMapping
public ResponseEntity<AddressDTO> create(@RequestBody AddressDTO dto) {
    Long userId = userService.getCurrentUserId();
    
    // dto chứa:
    // - recipient: "Nguyễn Văn A"
    // - phone: "0901234567"
    // - line1: "227 Nguyễn Văn Cừ"
    // - ward: "Phường 4"
    // - city: "Thành phố Hồ Chí Minh"
    // - latitude: 10.762622      ✅ Từ frontend
    // - longitude: 106.660172    ✅ Từ frontend
    
    AddressDTO saved = addressService.createAddress(userId, dto);
    return ResponseEntity.ok(saved);
}
```

### 🔍 Bước 2: Validate & Xử Lý Tọa Độ

**File:** `AddressService.java`

```java
@Transactional
public AddressDTO createAddress(Long userId, AddressDTO dto) {
    // Tạo entity
    Address address = new Address();
    address.setLine1(dto.line1());
    address.setWard(dto.ward());
    address.setCity(dto.city());
    
    // ⭐ BƯỚC QUAN TRỌNG: Ưu tiên tọa độ từ client
    boolean hasClientCoords = setIfValidCoordinates(
        address, 
        dto.latitude(),   // Từ frontend autocomplete
        dto.longitude()
    );
    
    if (hasClientCoords) {
        System.out.println("✅ Using client coordinates: " + 
            dto.latitude() + ", " + dto.longitude());
    } else {
        // Không có tọa độ từ client → Geocode server-side
        geocodeAddressOnServer(address);
    }
    
    return AddressDTO.from(addressRepository.save(address));
}

// Validate tọa độ
private boolean setIfValidCoordinates(Address addr, Double lat, Double lng) {
    if (lat == null || lng == null) return false;
    
    // Check NaN, Infinity
    if (!Double.isFinite(lat) || !Double.isFinite(lng)) {
        System.out.println("⚠️ Invalid coordinates: NaN or Infinity");
        return false;
    }
    
    // Check Vietnam bounds (8°-24.5°N, 102°-110.5°E)
    if (lat < 8.0 || lat > 24.5 || lng < 102.0 || lng > 110.5) {
        System.out.println("⚠️ Coordinates outside Vietnam");
        return false;
    }
    
    // Valid! Lưu vào entity
    addr.setLatitude(lat);
    addr.setLongitude(lng);
    return true;
}
```

### 🌐 Bước 3: Geocode Server-Side (Nếu Cần)

**File:** `GeocodingService.java`

```java
@Cacheable(value = "geocodeCache", key = "#address")
public Optional<LatLng> geocodeAddress(String address) {
    // 1. Check cache (Spring tự động)
    // Cache hit → return ngay
    
    // 2. Thử Google Maps API (nếu có key)
    if (hasGoogleApiKey()) {
        Optional<LatLng> googleResult = geocodeViaGoogle(address);
        if (googleResult.isPresent()) {
            logger.info("✅ Google geocode success");
            return googleResult;
        }
    }
    
    // 3. Fallback: Nominatim
    return geocodeViaNominatim(address);
}

private Optional<LatLng> geocodeViaNominatim(String address) {
    try {
        // ⚠️ QUAN TRỌNG: Delay để tuân thủ rate limit
        Thread.sleep(1200);  // 1.2 giây
        
        // Build URL
        String url = String.format(
            "https://nominatim.openstreetmap.org/search?" +
            "q=%s&format=json&countrycodes=vn&limit=1",
            URLEncoder.encode(address, UTF_8)
        );
        
        // Set User-Agent (bắt buộc)
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "AloTraWebsite/1.0");
        
        // Call API
        ResponseEntity<String> resp = restTemplate.exchange(
            url, HttpMethod.GET, 
            new HttpEntity<>(headers), 
            String.class
        );
        
        // Parse JSON
        JsonNode results = objectMapper.readTree(resp.getBody());
        if (results.size() == 0) {
            logger.warn("⚠️ No results from Nominatim");
            return Optional.empty();
        }
        
        double lat = results.get(0).get("lat").asDouble();
        double lon = results.get(0).get("lon").asDouble();
        
        logger.info("✅ Nominatim success: lat={}, lng={}", lat, lon);
        return Optional.of(new LatLng(lat, lon));
        
    } catch (Exception e) {
        logger.error("❌ Nominatim error: {}", e.getMessage());
        return Optional.empty();
    }
}
```

**Ví dụ Request:**
```http
GET https://nominatim.openstreetmap.org/search?
    q=227+Nguy%E1%BB%85n+V%C4%83n+C%E1%BB%AB%2C+Ph%C6%B0%E1%BB%9Dng+4%2C+TP.HCM&
    format=json&
    countrycodes=vn&
    limit=1

User-Agent: AloTraWebsite/1.0
```

**Ví dụ Response:**
```json
[
  {
    "lat": "10.7626220",
    "lon": "106.6601720",
    "display_name": "227, Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM"
  }
]
```

### 💾 Bước 4: Lưu Vào Database

```java
Address saved = addressRepository.save(address);

// Database record:
// id: 1
// user_id: 123
// recipient: "Nguyễn Văn A"
// phone: "0901234567"
// line1: "227 Nguyễn Văn Cừ"
// ward: "Phường 4"
// city: "Thành phố Hồ Chí Minh"
// latitude: 10.762622     ✅ Đã có tọa độ!
// longitude: 106.660172   ✅ Đã có tọa độ!
// is_default: true
```

---

## 5. Flow Hoàn Chỉnh - Từ Input đến Database

### 🔄 Scenario 1: User Chọn Từ Autocomplete (HAPPY PATH)

```
1. User gõ: "227 Nguyễn Văn Cừ"
   └─> Delay 500ms
   
2. Frontend gọi Nominatim API
   └─> GET nominatim.openstreetmap.org/search?q=227...
   
3. Nominatim trả về 5 gợi ý
   └─> Hiển thị dropdown
   
4. User click chọn: "227, Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM"
   └─> Trigger event 'nominatim-select'
   └─> Parse thành: line1, ward, city
   └─> Lưu tọa độ: lat=10.762622, lng=106.660172
   
5. User click "Lưu địa chỉ"
   └─> POST /api/addresses
   └─> Body: { line1, ward, city, latitude, longitude }
   
6. Backend nhận request
   └─> Validate tọa độ từ client: ✅ VALID
   └─> KHÔNG GỌI geocoding API (đã có tọa độ)
   └─> Lưu vào database
   
7. ✅ HOÀN THÀNH - Không tốn API call server-side!
```

### ⚠️ Scenario 2: User Nhập Thủ Công (KHÔNG CHỌN AUTOCOMPLETE)

```
1. User gõ trực tiếp: "227 Nguyễn Văn Cừ"
   └─> KHÔNG click chọn từ dropdown
   └─> Tắt modal luôn
   
2. User click "Lưu địa chỉ"
   └─> POST /api/addresses
   └─> Body: { line1, ward, city }
   └─> ❌ KHÔNG có latitude, longitude
   
3. Backend nhận request
   └─> Validate tọa độ: ⚠️ NULL
   └─> Gọi GeocodingService.geocodeAddress()
   
4. GeocodingService xử lý:
   └─> Check cache: MISS
   └─> Thử Google Maps: (nếu có key)
   └─> Fallback Nominatim:
       ├─> Delay 1.2s
       ├─> GET nominatim.../search?q=227+Nguyễn+Văn+Cừ...
       ├─> Parse response: lat=10.762622, lon=106.660172
       └─> Cache kết quả
   
5. AddressService nhận tọa độ
   └─> address.setLatitude(10.762622)
   └─> address.setLongitude(106.660172)
   └─> Lưu vào database
   
6. ✅ HOÀN THÀNH - Có tọa độ nhưng tốn 1 API call + delay 1.2s
```

### 🔄 Scenario 3: Nominatim Thất Bại → Fallback Google

```
1. Frontend autocomplete: ✅ OK (Nominatim)
2. User chọn → Có tọa độ → Submit
3. Backend: ✅ Dùng tọa độ từ client

HOẶC (nếu user nhập thủ công):

1. User nhập thủ công → Không có tọa độ
2. Backend geocode:
   └─> Nominatim: ❌ FAILED (timeout / no results)
   └─> Google Maps: ✅ SUCCESS
   └─> Lưu tọa độ từ Google
```

---

## 6. Các Trường Hợp Xử Lý

### ✅ Case 1: Tọa Độ Từ Client (Ưu Tiên Cao Nhất)

**Điều kiện:**
- Frontend gửi `latitude` và `longitude`
- Tọa độ hợp lệ (finite, trong bounds VN)

**Xử lý:**
```java
if (dto.latitude() != null && dto.longitude() != null) {
    if (isValidCoordinates(dto.latitude(), dto.longitude())) {
        address.setLatitude(dto.latitude());
        address.setLongitude(dto.longitude());
        // ✅ KHÔNG GỌI geocoding API
    }
}
```

**Ưu điểm:**
- ⚡ Nhanh (không cần gọi API)
- 💯 Chính xác (user đã chọn đúng địa điểm)
- 💰 Miễn phí (không tốn quota)

---

### 🌐 Case 2: Server-Side Geocoding (Khi Cần)

**Điều kiện:**
- Frontend KHÔNG gửi tọa độ
- Hoặc tọa độ không hợp lệ

**Xử lý:**
```java
if (address.getLatitude() == null) {
    String fullAddress = address.getFullAddressForGeocoding();
    // "227 Nguyễn Văn Cừ, Phường 4, Thành phố Hồ Chí Minh"
    
    Optional<LatLng> coords = geocodingService.geocodeAddress(fullAddress);
    coords.ifPresent(ll -> {
        address.setLatitude(ll.latitude());
        address.setLongitude(ll.longitude());
    });
}
```

**Flow:**
1. Check cache → Nếu có, return ngay
2. Thử Google Maps API (nếu có key)
3. Fallback Nominatim
4. Cache kết quả để lần sau dùng

---

### ❌ Case 3: Geocoding Thất Bại

**Điều kiện:**
- Cả Google và Nominatim đều thất bại
- Địa chỉ không tồn tại / sai format

**Xử lý:**
```java
Optional<LatLng> coords = geocodingService.geocodeAddress(fullAddress);
if (coords.isEmpty()) {
    logger.warn("⚠️ Cannot geocode address: {}", fullAddress);
    // Vẫn lưu địa chỉ nhưng không có tọa độ
    address.setLatitude(null);
    address.setLongitude(null);
}
addressRepository.save(address);  // Vẫn lưu
```

**Hậu quả:**
- ⚠️ Không tính được phí ship chính xác
- ⚠️ Không tìm được chi nhánh gần nhất
- 💡 User có thể sửa lại địa chỉ sau

---

### 🔄 Case 4: Cập Nhật Địa Chỉ

**Khi user sửa địa chỉ:**

```java
public AddressDTO updateAddress(userId, addressId, dto) {
    Address address = addressRepository.findById(addressId);
    
    // Update fields
    address.setLine1(dto.line1());
    address.setWard(dto.ward());
    address.setCity(dto.city());
    
    // ⚠️ QUAN TRỌNG: Geocode lại nếu địa chỉ thay đổi
    boolean hasNewCoords = setIfValidCoordinates(
        address, dto.latitude(), dto.longitude()
    );
    
    if (!hasNewCoords) {
        // Địa chỉ thay đổi nhưng không có tọa độ mới
        // → Geocode lại
        geocodingService.geocodeAddress(address.getFullAddress())
            .ifPresent(ll -> {
                address.setLatitude(ll.latitude());
                address.setLongitude(ll.longitude());
            });
    }
    
    return AddressDTO.from(addressRepository.save(address));
}
```

---

## 7. Code Examples

### 📄 Example 1: Profile Page - Thêm Địa Chỉ Mới

**HTML:**
```html
<div class="modal" id="addressModal">
    <form id="addressForm">
        <input type="text" id="line1" placeholder="Số nhà, đường...">
        <input type="text" id="ward" placeholder="Phường/Xã">
        <input type="text" id="city" placeholder="Thành phố">
        <button type="button" id="btnSaveAddress">Lưu</button>
    </form>
</div>
```

**JavaScript:**
```javascript
// 1. Init autocomplete khi modal mở
let modalAddressLat = null;
let modalAddressLng = null;

const addressModalEl = document.getElementById('addressModal');
addressModalEl.addEventListener('shown.bs.modal', async () => {
    // Reset tọa độ
    modalAddressLat = null;
    modalAddressLng = null;
    
    // Khởi tạo autocomplete
    const line1Input = document.getElementById('line1');
    const autocomplete = await window.googleMapsLoader
        .createAutocomplete(line1Input);
    
    // Lắng nghe event
    line1Input.addEventListener('nominatim-select', (e) => {
        const { address, lat, lng } = e.detail;
        
        // Parse địa chỉ
        const parts = address.split(',').map(p => p.trim());
        // ... logic parse ...
        
        // Lưu tọa độ
        modalAddressLat = lat;
        modalAddressLng = lng;
    });
});

// 2. Submit form
document.getElementById('btnSaveAddress')
    .addEventListener('click', async () => {
        const addressData = {
            recipient: document.getElementById('recip').value,
            phone: document.getElementById('addrPhone').value,
            line1: document.getElementById('line1').value,
            ward: document.getElementById('ward').value,
            city: document.getElementById('city').value,
            isDefault: document.getElementById('isDefault').checked,
            // ⭐ GỬI TỌA ĐỘ
            latitude: modalAddressLat,
            longitude: modalAddressLng
        };
        
        const res = await fetch('/alotra-website/api/addresses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(addressData)
        });
        
        if (res.ok) {
            showToast('✅ Đã lưu địa chỉ!', 'success');
            await loadAddresses();  // Reload danh sách
        }
    });
```

---

### 📄 Example 2: Backend - Address Service

**Java:**
```java
@Service
@RequiredArgsConstructor
public class AddressService {
    
    private final GeocodingService geocodingService;
    private final AddressRepository addressRepository;
    
    @Transactional
    public AddressDTO createAddress(Long userId, AddressDTO dto) {
        // 1. Tạo entity
        Address address = new Address();
        address.setUser(userRepository.getReferenceById(userId));
        address.setLine1(dto.line1());
        address.setWard(dto.ward());
        address.setCity(dto.city());
        
        // 2. Xử lý tọa độ
        boolean hasClientCoords = setIfValidCoordinates(
            address, 
            dto.latitude(), 
            dto.longitude()
        );
        
        if (!hasClientCoords) {
            // 3. Geocode server-side
            String fullAddress = String.format(
                "%s, %s, %s",
                address.getLine1(),
                address.getWard(),
                address.getCity()
            );
            
            System.out.println("🗺️ Geocoding: " + fullAddress);
            
            geocodingService.geocodeAddress(fullAddress)
                .ifPresent(coords -> {
                    address.setLatitude(coords.latitude());
                    address.setLongitude(coords.longitude());
                    System.out.println("✅ Got coordinates: " + coords);
                });
        } else {
            System.out.println("✅ Using client coordinates");
        }
        
        // 4. Lưu
        Address saved = addressRepository.save(address);
        return AddressDTO.from(saved);
    }
    
    private boolean setIfValidCoordinates(
        Address address, 
        Double lat, 
        Double lng
    ) {
        if (lat == null || lng == null) return false;
        if (!Double.isFinite(lat) || !Double.isFinite(lng)) return false;
        if (lat < 8.0 || lat > 24.5) return false;  // Vietnam bounds
        if (lng < 102.0 || lng > 110.5) return false;
        
        address.setLatitude(lat);
        address.setLongitude(lng);
        return true;
    }
}
```

---

## 8. Troubleshooting

### ❌ Vấn Đề 1: Autocomplete Không Hiện

**Triệu chứng:**
- User gõ địa chỉ nhưng dropdown không hiện

**Nguyên nhân:**
1. User gõ < 3 ký tự
2. Network chậm, delay 500ms chưa đủ
3. Nominatim API down/blocked

**Giải pháp:**
```javascript
// Tăng delay nếu network chậm
timeout = setTimeout(async () => {
    // ...
}, 800);  // Tăng từ 500ms lên 800ms

// Thêm error handling
try {
    const res = await fetch(nominatimUrl);
    if (!res.ok) {
        console.error('Nominatim API error:', res.status);
        showToast('Không thể tải gợi ý địa chỉ', 'warning');
        return;
    }
    const data = await res.json();
    showDropdown(data);
} catch (err) {
    console.error('Network error:', err);
    showToast('Lỗi kết nối, vui lòng thử lại', 'error');
}
```

---

### ❌ Vấn Đề 2: Tọa Độ Bị NULL Trong Database

**Triệu chứng:**
- Địa chỉ lưu thành công nhưng `latitude`, `longitude` = null

**Nguyên nhân:**
1. User nhập thủ công (không chọn autocomplete)
2. Geocoding server-side thất bại
3. Địa chỉ không hợp lệ / không tồn tại

**Kiểm tra:**
```bash
# Check server log
grep "Geocoding" application.log
grep "Nominatim" application.log

# Expected output:
# 🗺️ [AddressService] Geocoding address: 227 Nguyễn Văn Cừ...
# ✅ [GeocodingService] Nominatim SUCCESS: lat=10.762, lng=106.660
```

**Giải pháp:**
```java
// Thêm retry logic trong GeocodingService
for (int attempt = 1; attempt <= 3; attempt++) {
    Optional<LatLng> result = geocodeViaNominatim(address);
    if (result.isPresent()) return result;
    
    logger.warn("Retry {}/3 for address: {}", attempt, address);
    Thread.sleep(2000 * attempt);  // Backoff
}
```

---

### ❌ Vấn Đề 3: Rate Limit Exceeded

**Triệu chứng:**
- Nominatim API trả về 429 Too Many Requests

**Nguyên nhân:**
- Gọi API quá nhanh (> 1 request/second)

**Giải pháp:**
```java
// Đảm bảo delay đủ lâu
private static final long NOMINATIM_DELAY_MS = 1200;  // 1.2s

private Optional<LatLng> geocodeViaNominatim(String address) {
    try {
        // ⚠️ QUAN TRỌNG
        Thread.sleep(NOMINATIM_DELAY_MS);
        
        // ... gọi API ...
        
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

---

### ❌ Vấn Đề 4: Parse Địa Chỉ Sai

**Triệu chứng:**
- Địa chỉ bị split sai, ward vào city, city vào ward

**Nguyên nhân:**
- Format địa chỉ từ Nominatim không chuẩn
- Có postal code lẫn vào

**Giải pháp:**
```javascript
// Lọc bỏ postal code và country name
const filtered = parts.filter(part => {
    // Loại bỏ postal code (5-6 chữ số)
    if (/^\d{5,6}$/.test(part)) return false;
    
    // Loại bỏ "Việt Nam"
    if (part.toLowerCase().includes('việt nam')) return false;
    if (part.toLowerCase().includes('vietnam')) return false;
    
    return true;
});

// Debug log
console.log('Original:', address);
console.log('Filtered:', filtered);
console.log('Parsed:', { line1, ward, city });
```

---

## 📊 Performance & Best Practices

### ✅ DO's

1. **Ưu tiên tọa độ từ client**
   ```java
   if (dto.latitude() != null) {
       // Dùng ngay, KHÔNG geocode lại
       address.setLatitude(dto.latitude());
   }
   ```

2. **Cache kết quả geocoding**
   ```java
   @Cacheable(value = "geocodeCache", key = "#address")
   public Optional<LatLng> geocodeAddress(String address)
   ```

3. **Validate tọa độ**
   ```java
   if (lat < 8.0 || lat > 24.5 || lng < 102.0 || lng > 110.5) {
       // Ngoài bounds Việt Nam
       return false;
   }
   ```

4. **Tuân thủ rate limit**
   ```java
   Thread.sleep(1200);  // 1.2s giữa các request
   ```

5. **Error handling đầy đủ**
   ```java
   try {
       // geocode
   } catch (Exception e) {
       logger.error("Geocoding failed", e);
       // Vẫn lưu địa chỉ (không có tọa độ)
   }
   ```

### ❌ DON'Ts

1. **ĐỪNG geocode lại nếu đã có tọa độ từ client**
   ```java
   // ❌ BAD
   address.setLatitude(dto.latitude());
   // Vẫn gọi geocoding → Lãng phí!
   geocodingService.geocodeAddress(address.getFullAddress());
   ```

2. **ĐỪNG gọi API quá nhanh**
   ```javascript
   // ❌ BAD - Không delay
   timeout = setTimeout(() => fetch(nominatimUrl), 0);
   ```

3. **ĐỪNG bỏ qua validation**
   ```java
   // ❌ BAD - Không kiểm tra
   address.setLatitude(dto.latitude());  // Có thể là NaN!
   ```

4. **ĐỪNG hardcode API key**
   ```java
   // ❌ BAD
   String apiKey = "AIzaSyABC123...";
   
   // ✅ GOOD
   @Value("${google.maps.apiKey:}")
   private String apiKey;
   ```

---

## 📈 Metrics & Monitoring

### Các chỉ số cần theo dõi:

1. **Tỷ lệ có tọa độ từ client**
   ```sql
   SELECT 
       COUNT(*) AS total_addresses,
       SUM(CASE WHEN latitude IS NOT NULL THEN 1 ELSE 0 END) AS has_coords,
       ROUND(SUM(CASE WHEN latitude IS NOT NULL THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS percent
   FROM address;
   ```

2. **Thời gian geocoding trung bình**
   ```java
   long start = System.currentTimeMillis();
   geocodingService.geocodeAddress(addr);
   long duration = System.currentTimeMillis() - start;
   logger.info("Geocoding took {}ms", duration);
   ```

3. **Cache hit rate**
   ```java
   @CacheEvict(value = "geocodeCache", allEntries = true)
   public void clearCache() {
       logger.info("Geocode cache cleared");
   }
   ```

---

## 🎯 Kết Luận

### Luồng Tổng Quát:

```
USER INPUT
    ↓ (Nominatim Autocomplete)
FRONTEND PARSE
    ↓ (Gửi tọa độ lên server)
BACKEND VALIDATE
    ↓ (Nếu không có tọa độ → Geocode)
GEOCODING SERVICE
    ↓ (Cache → Google → Nominatim)
DATABASE
    ↓
✅ ĐỊA CHỈ CÓ TỌA ĐỘ GPS
```

### Key Takeaways:

1. ✅ **Frontend dùng Nominatim** (miễn phí, đủ tốt)
2. ✅ **Backend ưu tiên tọa độ từ client** (nhanh, chính xác)
3. ✅ **Server-side geocoding chỉ khi cần** (fallback)
4. ✅ **Cache để giảm API calls** (tối ưu hiệu năng)
5. ✅ **Validate đầy đủ** (tránh lỗi runtime)

---

**📝 Tài liệu này được cập nhật:** 28/10/2025  
**🔗 Related docs:**
- [NOMINATIM_IMPLEMENTATION.md](./NOMINATIM_IMPLEMENTATION.md)
- [ADDRESS_PARSING_FIX.md](./ADDRESS_PARSING_FIX.md)
- [COORDINATE_CALCULATION_GUIDE.md](./COORDINATE_CALCULATION_GUIDE.md)
