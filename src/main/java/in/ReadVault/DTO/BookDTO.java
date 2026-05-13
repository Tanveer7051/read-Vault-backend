package in.ReadVault.DTO;

import in.ReadVault.Entity.BookType;
import in.ReadVault.Entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private Category category;
    private String description;
    private BookType bookType;
    private int totalCopies;
    private int availableCopies;
    private String imgUrl;
    private String pdfurl;
    private String publishedBy;
}