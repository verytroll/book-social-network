package com.verytroll.book_network.book;

import com.verytroll.book_network.common.BaseEntity;
import com.verytroll.book_network.feedback.Feedback;
import com.verytroll.book_network.history.BookTransactionHistory;
import com.verytroll.book_network.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Book extends BaseEntity {
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archived;
    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @Transient
    public double getRate() {
        double result = 0.0;
        if(feedbacks != null && !feedbacks.isEmpty()) {
            var rate = feedbacks.stream()
                    .mapToDouble(Feedback::getNote)
                    .average()
                    .orElse(0.0);
            result = Math.round(rate * 10.0) / 10.0;
        }
        return(result);
    }
}
