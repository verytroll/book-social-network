package com.verytroll.book_network.feedback;

import com.verytroll.book_network.book.Book;
import com.verytroll.book_network.book.BookRepository;
import com.verytroll.book_network.common.PageResponse;
import com.verytroll.book_network.exception.OperationNotPermittedException;
import com.verytroll.book_network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final BookRepository bookRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + request.bookId()));
        if(!book.isArchived() && book.isShareable()) {
            User user = (User)connectedUser.getPrincipal();
            if(!Objects.equals(book.getOwner().getId(), user.getId())) {
                Feedback feedback = feedbackMapper.toFeedback(request);
                return feedbackRepository.save(feedback).getId();
            } else {
                throw new OperationNotPermittedException("You can not give a feedback to your own book");
            }
        } else {
            throw new OperationNotPermittedException("You can not give a feedback for an archived or not shareable book");
        }
    }

    public PageResponse<FeedbackResponse> findAllFeedbackByBookId(Integer bookId, int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size);
        User user = (User)connectedUser.getPrincipal();
        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();
        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }
}
