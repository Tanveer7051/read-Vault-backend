package in.ReadVault.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    @Enumerated(EnumType.STRING)
    private Category category=Category.LITERATURE;
    private String pdfurl;
    private  String pdfPublicId;
    private String description;
    private int totalCopies;
    private int availableCopies;
    private String imgUrl;
    private String imagePublicId;
    @Enumerated(EnumType.STRING)
    private BookType bookType;
    @JsonManagedReference(value = "book-reservation")
    @OneToMany(mappedBy = "book")
    private List<Reservation> reservations= new ArrayList<>();

    @JsonManagedReference(value = "book-borrow")
    @OneToMany(mappedBy = "book")
    private List<BorrowRecords> borrowRecords= new ArrayList<>();
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;
}