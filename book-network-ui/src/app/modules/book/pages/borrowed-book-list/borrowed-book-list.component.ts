import { Component, OnInit } from '@angular/core';
import { BorrowedBookResponse, FeedbackRequest, PageResponseBorrowedBookResponse } from '../../../../services/models';
import { BookService, FeedbackService } from '../../../../services/services';

@Component({
    selector: 'app-borrowed-book-list',
    templateUrl: './borrowed-book-list.component.html',
    styleUrl: './borrowed-book-list.component.scss'
})
export class BorrowedBookListComponent implements OnInit {

    borrowedBooks: PageResponseBorrowedBookResponse = {};
    selectedBook: BorrowedBookResponse | undefined = undefined;
    feedbackRequest: FeedbackRequest = {bookId: 0, comment: '', note: 0};
    page: number = 0;
    size: number = 5;

    constructor(
        private bookService: BookService,
        private feedbackService: FeedbackService
    ) {}

    ngOnInit(): void {
        this.findAllBorrowedBooks();
    }

    findAllBorrowedBooks() {
        this.bookService.findAllBorrowedBooks({
            page: this.page,
            size: this.size
        }).subscribe({
            next: (res) => {
                this.borrowedBooks = res;
            }
        });
    }

    returnBorrowedBook(book: BorrowedBookResponse) {
        this.selectedBook = book;
        this.feedbackRequest.bookId = book.id as number;
    }

    giveFeedback() {
        this.feedbackService.saveFeedback({
            body: this.feedbackRequest
        }).subscribe({
            next: () => {
            
            }
        });
    }

    returnBook(withFeedback: boolean) {
        this.bookService.returnBorrowedBook({
            'book-id': this.selectedBook?.id as number
        }).subscribe({
            next: () => {
                if(withFeedback) {
                    this.giveFeedback();
                }
                this.selectedBook = undefined;
                this.findAllBorrowedBooks();
            }
        });
    }

    goToFirstPage() {
        this.page = 0;
        this.findAllBorrowedBooks();
    }

    goToLastPage() {
        this.page = (this.borrowedBooks.totalPage as number - 1);
        this.findAllBorrowedBooks();
    }

    goToPreviousPage() {
        --this.page;
        this.findAllBorrowedBooks();
    }

    goToNextPage() {
        ++this.page;
        this.findAllBorrowedBooks();
    }

    goToPage(pageIndex: number) {
        this.page = pageIndex;
        this.findAllBorrowedBooks();
    }

    isLastPage(): boolean {
        let result = true;
        if (this.borrowedBooks.totalPage) {
            result = (this.page == (this.borrowedBooks.totalPage - 1));
        }
        return result;
    }
}
