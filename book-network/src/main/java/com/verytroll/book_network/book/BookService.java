package com.verytroll.book_network.book;

import com.verytroll.book_network.common.PageResponse;
import com.verytroll.book_network.exception.OperationNotPermittedException;
import com.verytroll.book_network.file.FileStorageService;
import com.verytroll.book_network.history.BookTransactionHistory;
import com.verytroll.book_network.history.BookTransactionHistoryRepository;
import com.verytroll.book_network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final BookMapper bookMapper;
    private final FileStorageService fileStorageService;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page,int size, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(user.getId(), pageable);
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookTransactionHistory> histories = transactionHistoryRepository.findAllBorrowedBooks(user.getId(), pageable);
        List<BorrowedBookResponse> bookResponse = histories.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                histories.getNumber(),
                histories.getSize(),
                histories.getTotalElements(),
                histories.getTotalPages(),
                histories.isFirst(),
                histories.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookTransactionHistory> histories = transactionHistoryRepository.findAllReturnedBooks(user.getId(), pageable);
        List<BorrowedBookResponse> bookResponse = histories.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                histories.getNumber(),
                histories.getSize(),
                histories.getTotalElements(),
                histories.getTotalPages(),
                histories.isFirst(),
                histories.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
        User user = (User)connectedUser.getPrincipal();
        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            book.setShareable(!book.isShareable());
            bookRepository.save(book);
            return bookId;
        } else {
            throw new OperationNotPermittedException("You can not update other books shareable status");
        }
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
        User user = (User)connectedUser.getPrincipal();
        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            book.setArchived(!book.isArchived());
            bookRepository.save(book);
            return bookId;
        } else {
            throw new OperationNotPermittedException("You can not update other books archived status");
        }
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
        if(!book.isArchived() && book.isShareable()) {
            User user = (User)connectedUser.getPrincipal();
            if(!Objects.equals(book.getOwner().getId(), user.getId())) {
                final boolean isAlreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
                if(!isAlreadyBorrowed) {
                    BookTransactionHistory history = BookTransactionHistory.builder()
                            .user(user)
                            .book(book)
                            .returned(false)
                            .returnApproved(false)
                            .build();
                    return transactionHistoryRepository.save(history).getId();
                } else {
                    throw new OperationNotPermittedException("The requested book is already borrowed");
                }
            } else {
                throw new OperationNotPermittedException("You can not borrow your own book");
            }
        } else {
            throw new OperationNotPermittedException("The requested book can not be borrowed since it is archived or not shareable");
        }
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
        if(!book.isArchived() && book.isShareable()) {
            User user = (User)connectedUser.getPrincipal();
            if(!Objects.equals(book.getOwner().getId(), user.getId())) {
                BookTransactionHistory history = transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                        .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));
                history.setReturned(true);
                return transactionHistoryRepository.save(history).getId();
            } else {
                throw new OperationNotPermittedException("You can not return your own book");
            }
        } else {
            throw new OperationNotPermittedException("The requested book can not be borrowed since it is archived or not shareable");
        }
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
        if(!book.isArchived() && book.isShareable()) {
            User user = (User)connectedUser.getPrincipal();
            if(Objects.equals(book.getOwner().getId(), user.getId())) {
                BookTransactionHistory history = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                        .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You can not approve its return"));
                history.setReturnApproved(true);
                return transactionHistoryRepository.save(history).getId();
            } else {
                throw new OperationNotPermittedException("You can not aprrove the return of a book that you do not own");
            }
        } else {
            throw new OperationNotPermittedException("The requested book can not be approved since it is archived or not shareable");
        }
    }

    public void uploadBookCoverPicture(Integer bookId, Authentication connectedUser, MultipartFile file) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the id: " + bookId));
        User user = (User)connectedUser.getPrincipal();
        String bookCover = fileStorageService.saveFile(user.getId(), file);
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
