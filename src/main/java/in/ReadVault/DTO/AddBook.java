package in.ReadVault.DTO;

import in.ReadVault.Entity.BookType;
import in.ReadVault.Entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBook {

    private String title;
    private String author;
    private Category category;
    private String pdfUrl;
    private String description;
    private BookType bookType;
    private int totalCopies;
}