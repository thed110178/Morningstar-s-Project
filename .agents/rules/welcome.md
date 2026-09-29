---
trigger: always_on
---

# Project Unknown - Workspace Rules

## Chào mừng đến với Project Unknown

Trước khi bắt đầu hỗ trợ dự án, AI Agent phải dành thời gian tìm hiểu về cấu trúc, tài liệu và mã nguồn của Project Unknown.

---

# 1. Quy trình tìm hiểu dự án

## Bước 1 - Đọc tài liệu (nếu c)

Đầu tiên, hãy đọc toàn bộ tài liệu trong:

```
ProjectUnknown/Assets/Docs
```

để nắm được:

- Tổng quan dự án.
- Thiết kế gameplay.
- Kiến trúc hệ thống.
- Quy tắc phát triển.

Nếu chưa biết cấu trúc thư mục tài liệu, hãy đọc trước:

```
Assets/Docs/Project_Summary.md
```

---

## Bước 2 - Đọc mã nguồn

Sau khi hiểu tài liệu, tiếp tục đọc mã nguồn trong:

```
ProjectUnknown/Assets/Script
```

để hiểu:

- Kiến trúc code.
- Chức năng của từng System.
- API hiện có.
- Quan hệ giữa các thành phần.

Mã nguồn của dự án đã được chú thích (comment) nhằm hỗ trợ cả lập trình viên và AI Agent hiểu nhanh chức năng của từng đoạn code.

---

## Bước 3 - Thực hiện yêu cầu

Sau khi hoàn thành hai bước trên, AI Agent mới bắt đầu xử lý yêu cầu của người dùng.

Trong toàn bộ quá trình làm việc, luôn tuân thủ các quy tắc được định nghĩa trong **Global Rules**.

---

# 2. Quy định về tài liệu

## 2.1 Cấu trúc thư mục

Toàn bộ tài liệu được lưu tại:

```
ProjectUnknown/Assets/Docs
```

Thư mục được tổ chức theo dạng:

```
Docs
│
├── Object
│
├── Checklist
│
├── Feature Category
│   ├── System A
│   ├── System B
│   └── ...
│
└── Update
```

Nếu chưa hiểu cách phân loại, hãy đọc:

```
Assets/Docs/Project_Summary.md
```

---

## 2.2 Cập nhật tài liệu

Khi người dùng yêu cầu cập nhật tài liệu Markdown, AI Agent phải thực hiện đầy đủ các bước sau.

### Cập nhật thời gian

Ở đầu mỗi file Markdown, thêm dòng:

```
Cập nhật mới nhất vào ngày: dd/mm/yyyy
```

---

### Tạo tài liệu mới

Nếu tính năng là hoàn toàn mới:

- Được phép tạo file Markdown mới.
- Đặt file vào đúng thư mục phân loại.
- Không tự ý tạo thư mục mới.

Nếu cần tạo một nhóm phân loại (Folder) mới, AI Agent phải hỏi ý kiến người dùng trước.

---

### Cấu trúc file mô tả tính năng

Khuyến nghị sử dụng cấu trúc sau:

```
1. Mô tả

2. Quy trình hoạt động

3. API (nếu có)

4. Inspector Settings (nếu có)

5. Quan hệ với hệ thống khác

6. Sửa đổi / Cập nhật

7. Lỗi đã sửa
```

Không phải mục nào cũng bắt buộc, tùy từng hệ thống.

---

## 2.3 Ghi nhận thay đổi

Mỗi lần được yêu cầu cập nhật tài liệu , trừ khi tạo và cập nhâ tài liệu kế hoạch trong Assets\Docs\PlanForNextFeature, AI Agent phải tự động tạo một báo cáo trong:

```
Assets/Docs/Update
```

Ngay cả khi người dùng không yêu cầu viết báo cáo, mà là chỉnh sửa.

Tên file:

```
yy-mm-dd_[số thứ tự trong ngày].md
```

Ví dụ:

```
26-07-11_01.md
26-07-11_02.md
```

Báo cáo cần ghi rõ chi tiết:

- Những gì đã thay đổi.
- File nào đã chỉnh sửa.
- File mới được tạo.
- Lý do chỉnh sửa.
- Ghi chú quan trọng (nếu có).
- Nếu có 1 lỗi, Tính năng mới, hãy mô tả rõ nguyên nhân/lí do, mục đích, cách sửa/ các vận hành.

---

Báo cáo không chỉ là liệt kê, mà là mô tả chi tiết cập nhật!

Nếu người dùng sử dụng agent để update liên tục, thì nên viết báo cáo trên cùng 1 file, mỗi yêu cầu sẽ được ghi đè, chỉnh sửa liên tục lên file đó.

Khi người dùng yêu cầu update sau ít nhất 1h kể từ yêu cầu chỉnh sửa gần nhất thì mới tạo file update mới để phòng tránh folder có quá nhiều file

## 2.4 Khi cập nhật hệ thống đã tồn tại

Nếu chỉnh sửa một hệ thống đã có tài liệu:

### Bổ sung tính năng

Thêm vào mục:

```
Sửa đổi / Cập nhật
```

Bao gồm:

- Ngày cập nhật.
- Nội dung thay đổi.
- Mô tả chi tiết cách hoạt động.

---

### Sửa lỗi

Thêm vào mục:

```
Lỗi đã sửa
```

Bao gồm:

- Ngày sửa.
- Nguyên nhân lỗi.
- Cách khắc phục.
- Ảnh hưởng tới hệ thống (nếu có).

Không được ghi đè hoặc xóa lịch sử cập nhật cũ.

## 2.5 Quy định về Comment mã nguồn

Khi sửa đổi hoặc viết mới mã nguồn, AI Agent phải bổ sung hoặc cập nhật comment cho các đoạn code liên quan.

### Bắt buộc

- Mọi hàm (Method) mới phải có comment tóm tắt chức năng (`/// <summary>` nếu phù hợp).
- Các biến hoặc thuộc tính quan trọng cần có comment giải thích vai trò nếu tên gọi chưa đủ rõ nghĩa.
- Khi thay đổi logic của một hệ thống, phải cập nhật comment của các đoạn code liên quan để phản ánh đúng hành vi mới.
- Nếu một hàm hoặc đoạn code được refactor đáng kể, comment cũ phải được sửa hoặc xóa nếu không còn chính xác.

---

### Nội dung comment

Comment không chỉ mô tả **code đang làm gì**, mà còn phải giải thích:

- Tại sao cần đoạn code đó.
- Đoạn code giải quyết vấn đề gì.
- Những hệ thống nào phụ thuộc vào đoạn code này (nếu có).
- Các trường hợp đặc biệt (Edge Cases) cần lưu ý.

Ưu tiên giải thích **WHY** hơn là **WHAT**.

Ví dụ tốt:

```csharp
// Prevent facing jitter caused by Camera Smooth Follow.
// When the cursor is close to the player, ScreenToWorldPoint may
// cause CursorDeltaX to oscillate every frame.
// Temporarily lock the facing direction until the cursor moves away.
```

Ví dụ chưa tốt:

```csharp
// Set facing direction.
```

---

### Đồng bộ comment

Khi chỉnh sửa một tính năng, AI Agent phải kiểm tra các comment liên quan trong cùng hệ thống.

Nếu hành vi đã thay đổi nhưng comment vẫn mô tả logic cũ, phải cập nhật lại ngay.

Không được để comment và mã nguồn mâu thuẫn với nhau.

---

### Mục tiêu

Comment phải giúp:

- Người mới tham gia dự án hiểu nhanh hệ thống.
- AI Agent khác hiểu đúng ý đồ thiết kế.
- Giảm thời gian đọc và bảo trì mã nguồn trong tương lai.

# 3. Quy định về Vibe Coding

Mục tiêu của Project Unknown là xây dựng mã nguồn có tính nhất quán, dễ mở rộng và dễ bảo trì.

AI Agent không chỉ có nhiệm vụ hoàn thành yêu cầu, mà còn phải giữ cho toàn bộ dự án phát triển theo cùng một phong cách.

---

## 3.1 Tư duy khi lập trình

Ưu tiên:

- Đơn giản hơn là thông minh.
- Dễ đọc hơn là ngắn.
- Dễ mở rộng hơn là tối ưu sớm.
- Refactor khi cần, không over-engineering.

---

## 3.2 Không phá vỡ kiến trúc

Không được:

- Nhét thêm logic vào một System nếu logic đó có thể tách riêng.
- Biến một class thành God Object.
- Tự ý thay đổi kiến trúc hiện có nếu người dùng không yêu cầu.

Nếu phát hiện kiến trúc chưa hợp lý:

- Đưa ra đề xuất.
- Giải thích ưu và nhược điểm.
- Chỉ thực hiện sau khi người dùng đồng ý.

---

## 3.3 Khi viết code

Ưu tiên:

- Hàm ngắn.
- Một hàm chỉ nên thực hiện một nhiệm vụ.
- Tên biến thể hiện rõ mục đích.
- Không lặp code nếu có thể tái sử dụng.

---

## 3.4 Khi Refactor

Mục tiêu của Refactor là:

- Giữ nguyên hành vi.
- Cải thiện cấu trúc.
- Không thay đổi gameplay.

Nếu gameplay cần thay đổi, phải được người dùng xác nhận trước.

---

## 3.5 Khi bổ sung tính năng

Mỗi tính năng mới cần trả lời được:

- Vì sao hệ thống này tồn tại?
- Nó thuộc domain nào?
- Có nên là một System riêng không?
- Có ảnh hưởng tới hệ thống nào khác không?

Nếu có thể tồn tại độc lập, ưu tiên tạo thành một System riêng.

---

## 3.6 Chuẩn hóa mã nguồn

Mọi mã nguồn mới cần tuân thủ phong cách của dự án hiện tại.

Không được:

- Đổi phong cách đặt tên.
- Đổi cách tổ chức code.
- Đổi cách comment.
- Đổi kiến trúc chỉ vì sở thích cá nhân.

Mục tiêu là toàn bộ dự án có cảm giác như được viết bởi **một lập trình viên duy nhất**.

---

## 3.7 Đặt câu hỏi khi không chắc chắn

Nếu yêu cầu của người dùng có thể ảnh hưởng đến:

- Gameplay.
- Kiến trúc.
- Khả năng mở rộng.

AI Agent nên hỏi lại trước khi thực hiện, thay vì tự đưa ra quyết định.

AI không được tự ý "cải tiến" gameplay nếu chưa có yêu cầu rõ ràng.

## 3.X Quy định về Animator Controller

Nếu người dùng yêu cầu chỉnh sửa hoặc bổ sung Animator Controller, AI Agent phải tuân thủ các quy định sau.

### Khi AI có khả năng chỉnh sửa Animator Controller

AI được phép:

- Tạo State mới.
- Xóa State không còn sử dụng.
- Tạo hoặc xóa Transition.
- Thêm, sửa hoặc xóa Condition.
- Sắp xếp lại vị trí các State để Animator dễ đọc hơn.

Tuy nhiên, việc chỉnh sửa chỉ được thực hiện đúng theo yêu cầu của người dùng và không tự ý thay đổi gameplay.

---

### Chuẩn hóa bố cục Animator

Khi thêm hoặc sắp xếp State, AI phải nhóm các State theo chức năng.

Ví dụ:

```

MovementLayer:
Locomotion
├── Idle
├── Walk
├── Run
└── Crouch

Jump
├── Jump
├── Falling
└── Landing

Dodge
├── Roll
└── AirDash
Climbing
├── RopeClimb
├── LedgeGrab
└── ClimbUp

CombatLayer:
Combat
├── Attack1
├── Attack2
├── Attack3
└── Recovery

SurvivalLayer:
Heal
├── PreHeal
└── Healing
```

Các nhóm nên được đặt gần nhau, hạn chế đường nối chồng chéo và giữ bố cục dễ quan sát.

---

### Khi AI không thể chỉnh sửa Animator Controller

Nếu AI không thể trực tiếp chỉnh sửa Animator Controller, phải hướng dẫn người dùng chi tiết.

Hướng dẫn cần bao gồm:

- State bắt đầu.
- State kết thúc.
- Transition cần tạo hoặc chỉnh sửa.
- Có bật **Has Exit Time** hay không.
- Exit Time (nếu cần).
- Transition Duration (nếu cần).
- Danh sách Condition.
- Giá trị của từng Condition.
- Chiều nối của Transition.
- Các thiết lập đặc biệt (Interruption Source, Fixed Duration...) nếu liên quan.

Ví dụ:

```
Transition

Attack1
↓

Attack2

Has Exit Time:
✔ Enabled

Exit Time:
0.9

Transition Duration:
0.05

Condition:
ComboIndex == 2
```

---

### Không tự ý thay đổi State Machine

AI không được:

- Tự ý đổi tên State.
- Tự ý thay đổi Parameter.
- Tự ý thay đổi Blend Tree.
- Tự ý sửa Animation Clip.
- Tự ý thay đổi luồng Animation nếu người dùng chưa yêu cầu.

Nếu việc sửa đổi có thể ảnh hưởng đến gameplay hoặc hệ thống Animation hiện có, AI phải giải thích trước và chờ người dùng xác nhận.

---

### Mục tiêu

Animator Controller phải:

- Dễ đọc.
- Dễ bảo trì.
- Dễ mở rộng.
- Các State được phân nhóm rõ ràng theo chức năng.
- Hạn chế tối đa các đường Transition giao nhau.