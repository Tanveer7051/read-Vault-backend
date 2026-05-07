package in.ReadVault.DTO;

import in.ReadVault.Entity.BookType;
import in.ReadVault.Entity.Category;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBook {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 to 100 characters")
    private String title;

    @NotBlank(message = "Author name is required")
    @Pattern(regexp = "^[A-Za-z .]+$", message = "Enter valid author name")
    private String author;

    @NotNull(message = "Category is required")
    private Category category;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 300, message = "Description must be between 10 to 300 characters")
    private String description;

    @NotNull(message = "Book type is required")
    private BookType bookType;

    private int totalCopies;
}