# Z-Serialize Wire Format Specification

**Version**: 1.0  
**Status**: Stable  
**Author**: zifang  

---

## 1. Overview

Z-Serialize is a cross-language, schema-first, zero-copy binary serialization format. It combines the efficiency of Protocol Buffers with schema evolution capabilities and optional compression/encryption.

### Design Goals

- **Compact**: Variable-length encoding (varint) for integers
- **Extensible**: Field ID-based schema evolution (add/remove fields safely)
- **Interoperable**: Same wire format across Java, C++, Python, Go, Rust
- **Optional features**: Compression (gzip/zstd/lz4) and encryption (AES-GCM)

---

## 2. Wire Format Layout

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER (固定)                       │
├──────────┬──────────┬──────────┬──────────┬─────────────┤
│ magic[2] │ ver[1]   │ flags[1] │ schemaId[4] │ ...      │
├──────────┴──────────┴──────────┴──────────┴─────────────┤
│                   VARIABLE HEADER                        │
├─────────────────────────────────────────────────────────┤
│ schemaNameLen[1] │ schemaName[N] (可选)                  │
├─────────────────────────────────────────────────────────┤
│ bodyLen[varint]                                         │
├─────────────────────────────────────────────────────────┤
│                      BODY                                │
├─────────────────────────────────────────────────────────┤
│ tag[1-N] │ value[0-N] │ tag[1-N] │ value[0-N] │ ...   │
├─────────────────────────────────────────────────────────┤
│                   FIELD INDEX (可选)                     │
├─────────────────────────────────────────────────────────┤
│ indexCount[varint] │ [fieldId,varint,offset,varint,    │
│                      length,varint] × indexCount        │
├─────────────────────────────────────────────────────────┤
│                   FOOTER (可选, 仅加密)                   │
├─────────────────────────────────────────────────────────┤
│ HMAC-SHA256 truncated (16 bytes)                        │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Header Structure

### 3.1 Fixed Header (8 bytes)

| Offset | Size | Field | Description |
|--------|------|-------|-------------|
| 0 | 2 | magic | `0xBA 0xBE` (Binary Architecture Encoding) |
| 2 | 1 | version | Wire format version (`0x01`) |
| 3 | 1 | flags | Feature flags (see below) |
| 4 | 4 | schemaId | Little-endian int32, from `@ZMessage.id` |

### 3.2 Flags Byte

| Bit | Name | Description |
|-----|------|-------------|
| 0 | COMPRESSED | Body is compressed |
| 1 | ENCRYPTED | Body is encrypted |
| 2 | INDEXED | Field index is present |
| 3-7 | Reserved | Must be 0 |

### 3.3 Variable Header

| Field | Type | Description |
|-------|------|-------------|
| schemaNameLen | varint | Length of schema name (0 = no name) |
| schemaName | bytes | UTF-8 encoded schema name |
| bodyLen | varint | Length of body in bytes |

---

## 4. Body Encoding

### 4.1 Tag-Value Format

Each field is encoded as:

```
[tag varint] [value bytes]
```

**Tag** = `(field_id << 3) | wire_type`

### 4.2 Wire Types

| Value | Name | Description |
|-------|------|-------------|
| 0 | VARINT | Variable-length integer (int32, int64, uint32, uint64, bool, enum) |
| 1 | FIXED64 | 64-bit fixed (double, int64 fixed) |
| 2 | LENGTH_DELIMITED | Length-prefixed bytes (string, bytes, embedded message, packed repeated) |
| 5 | FIXED32 | 32-bit fixed (float, int32 fixed) |

### 4.3 Varint Encoding

Varint uses little-endian, 7 bits per byte, MSB as continuation flag:

```
value 0-127:     [0xxxxxxx]           (1 byte)
value 128-16383: [1xxxxxxx][0xxxxxxx] (2 bytes)
...
```

**Signed integers** use ZigZag encoding before varint:

```
ZigZag(n) = (n << 1) ^ (n >> 31)   // for int32
ZigZag(n) = (n << 1) ^ (n >> 63)   // for int64
```

Examples:
- 0 → 0
- -1 → 1
- 1 → 2
- -2 → 3
- 2147483647 → 4294967294

### 4.4 Length-Delimited Encoding

```
[varint length] [bytes]
```

For strings: UTF-8 encoded bytes.  
For embedded messages: nested message body (without header).  
For packed repeated: multiple values without individual tags.

### 4.5 Packed Repeated (Optimization)

For `VARINT` type lists with `@ZField(packed=true)`:

```
[tag] [length-delimited packed bytes]
```

Packed bytes contain multiple varint values without individual tags.

---

## 5. Field Index (Optional)

When `FLAG_INDEXED` is set, a field index is appended after the body.

### 5.1 Index Format

```
[indexCount varint]                    // Number of indexed fields
[fieldId varint] [offset varint] [length varint]  × indexCount
```

| Field | Type | Description |
|-------|------|-------------|
| indexCount | varint | Number of indexed fields |
| fieldId | varint | Field ID (from @ZField.id) |
| offset | varint | Byte offset in body where field starts |
| length | varint | Byte length of field (including tag) |

### 5.2 Use Cases

- **Skip decoding**: Read index first, then seek to specific fields
- **Range query**: Find all fields in a given ID range
- **Partial deserialization**: Only decode needed fields

---

## 6. Compression (Optional)

When `FLAG_COMPRESSED` is set, the body is compressed before encryption.

### 6.1 Supported Algorithms

| Algorithm | Header Flag | Notes |
|-----------|-------------|-------|
| gzip | `compressed` | JDK built-in, moderate ratio/speed |
| zstd | `compressed` | High ratio + fast (requires zstd-jni) |
| lz4 | `compressed` | Fastest (requires lz4-java) |

### 6.2 Compression Pipeline

```
Original Body → Compress → Encrypt → Final Body
```

---

## 7. Encryption (Optional)

When `FLAG_ENCRYPTED` is set, the body is encrypted.

### 7.1 AES-GCM Format

```
[IV 12 bytes] [ciphertext + tag 16 bytes]
```

- **IV**: Random 12-byte initialization vector
- **Ciphertext**: AES-256-GCM encrypted body
- **Tag**: 128-bit authentication tag (provides integrity)

### 7.2 Decryption Pipeline

```
Final Body → Decrypt → Decompress → Original Body
```

---

## 8. Cross-Language Implementation Notes

### 8.1 Java

```java
// Serialization
byte[] bytes = ZSerializer.INSTANCE.toBytes(message);

// Deserialization
User user = ZDeserializer.INSTANCE.fromBytes(bytes, User.class);

// With compression + encryption
CodecConfig config = CodecConfig.builder()
    .compressor(GzipCompressor.INSTANCE)
    .encryptor(new AesGcmEncryptor(key))
    .build();
ZSerializer.INSTANCE.useConfig(config);
```

### 8.2 C++ (Planned)

```cpp
// Schema definition via codegen
auto user = ZSerialize::encode<User>({.id = 42, .name = "alice"});
auto bytes = user.toBytes();

// Deserialization
auto loaded = ZSerialize::decode<User>(bytes);
```

### 8.3 Python (Planned)

```python
# Schema-first approach
user = User(id=42, name="alice")
bytes = zserialize.encode(user)

# Deserialization
loaded = zserialize.decode(bytes, User)
```

### 8.4 Go (Planned)

```go
// Generated codec
user := &User{ID: 42, Name: "alice"}
data := zserialize.Encode(user)

// Deserialization
var loaded User
zserialize.Decode(data, &loaded)
```

---

## 9. Schema Evolution Rules

### 9.1 Adding Fields

- Assign a new field ID (never reuse IDs)
- Old readers skip unknown fields automatically
- New readers use default values for missing fields

### 9.2 Removing Fields

- Mark as `@ZField(removed = "2.0")`
- Stop writing the field
- Skip reading the field

### 9.3 Field ID Constraints

- Must be in range [1, 2^29 - 1]
- IDs [19000, 19999] are reserved
- Never change an existing field's ID

---

## 10. Performance Characteristics

| Operation | Java (Reflect) | Java (CodeGen) | Notes |
|-----------|----------------|----------------|-------|
| Write (ops/s) | ~10M | ~13M | CodeGen 1.3x faster |
| Read (ops/s) | ~5M | ~12M | CodeGen 2.4x faster |
| Wire size | baseline | baseline | Same format |
| Compression | - | 90-95% | For repetitive data |

---

## Appendix A: Example Wire Format

### Simple User Message

```java
@ZMessage(id = 100, name = "user.v1.User")
public class User {
    @ZField(id = 1, type = FieldType.VARINT) public long id;
    @ZField(id = 2, type = FieldType.LENGTH_DELIMITED) public String name;
    @ZField(id = 3, type = FieldType.VARINT) public int age;
}
```

Instance: `{ id: 42, name: "alice", age: 30 }`

### Wire Bytes

```
BA BE              // magic
01                 // version
00                 // flags (no compression/encryption)
64 00 00 00        // schemaId = 100 (little-endian)
00                 // schemaNameLen = 0
09                 // bodyLen = 9
08 54              // tag(1,VARINT) + ZigZag(42) = 84
12 05 61 6C 69 63 65  // tag(2,LEN) + len(5) + "alice"
18 3C              // tag(3,VARINT) + ZigZag(30) = 60
```

Total: 19 bytes
