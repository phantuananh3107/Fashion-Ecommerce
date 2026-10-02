package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryObject {
    private int categoryId;
    private String categoryName;
    private Integer parentId; // Dùng Integer thay vì int để có thể chứa giá trị null (Danh mục gốc)

    // Danh sách chứa các menu con (Ví dụ: "Nữ" sẽ chứa "Áo", "Quần"...)
    private List<CategoryObject> children = new ArrayList<>();
}