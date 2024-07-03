import { Component, OnInit } from '@angular/core';
import { BookResponse, PageResponseBookResponse } from '../../../../services/models';
import { Router } from '@angular/router';
import { BookService } from '../../../../services/services';

@Component({
    selector: 'app-my-books',
    templateUrl: './my-books.component.html',
    styleUrl: './my-books.component.scss'
})
export class MyBooksComponent implements OnInit {

    bookResponse: PageResponseBookResponse = {};
    page: number = 0;
    size: number = 5;

    constructor(
        private router: Router,
        private bookService: BookService
    ) {
    }

    ngOnInit(): void {
        this.findAllBooks();
    }

    private findAllBooks(): void {
        this.bookService.findAllBooksByOwner({
            page: this.page,
            size: this.size
        }).subscribe({
            next: (res) => {
                this.bookResponse = res;
            }
        });
    }

    goToFirstPage() {
        this.page = 0;
        this.findAllBooks();
    }

    goToLastPage() {
        this.page = (this.bookResponse.totalPage as number - 1);
        this.findAllBooks();
    }

    goToPreviousPage() {
        --this.page;
        this.findAllBooks();
    }

    goToNextPage() {
        ++this.page;
        this.findAllBooks();
    }

    goToPage(pageIndex: number) {
        this.page = pageIndex;
        this.findAllBooks();
    }

    isLastPage(): boolean {
        let result = true;
        if (this.bookResponse.totalPage) {
            result = (this.page == (this.bookResponse.totalPage - 1));
        }
        return result;
    }

    editBook(book: BookResponse) {
        this.router.navigate(['books', 'manage', book.id]);
    }

    shareBook(book: BookResponse) {
        this.bookService.updateShareableStatus({
            'book-id': book.id as number
        }).subscribe({
            next: () => {
                book.shareable = !book.shareable;
            }
        });
    }

    archiveBook(book: BookResponse) {
        this.bookService.updateArchivedStatus({
            'book-id': book.id as number
        }).subscribe({
            next: () => {
                book.archived = !book.archived;
            }
        });
    }
}
