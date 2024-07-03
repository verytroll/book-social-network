import { Component, OnInit } from '@angular/core';
import { BookRequest } from '../../../../services/models';
import { BookService } from '../../../../services/services';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'app-manage-book',
    templateUrl: './manage-book.component.html',
    styleUrl: './manage-book.component.scss'
})
export class ManageBookComponent implements OnInit {

    bookRequest: BookRequest = { authorName: '', isbn: '', synopsis: '', title: '' };
    errorMessages: Array<string> = [];
    selectedBookCover: any;
    selectedPicture: string | undefined;

    constructor(
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private bookService: BookService
    ) {}

    ngOnInit(): void {
        let bookId = this.activatedRoute.snapshot.params['bookId'];
        if(bookId) {
            this.bookService.findBookById({
                'book-id': bookId
            }).subscribe({
                next: (res) => {
                    this.bookRequest = {
                        id: res.id,
                        title: res.title as string,
                        authorName: res.authorName as string,
                        isbn: res.isbn as string,
                        synopsis: res.synopsis as string,
                        shareable: res.shareable
                    }
                    if(res.cover) {
                        this.selectedPicture = 'data:image/jpg;base64,' + res.cover;
                    }
                }
            });
        }
    }

    onFileSelected(event: any) {
        this.selectedBookCover = event.target.files[0];
        if (this.selectedBookCover) {
            let reader: FileReader = new FileReader();
            reader.onload = () => {
                this.selectedPicture = reader.result as string;
            }
            reader.readAsDataURL(this.selectedBookCover);
        }
    }

    saveBook() {
        this.bookService.saveBook({
            body: this.bookRequest
        }).subscribe({
            next: (bookId) => {
                this.bookService.uploadBookCoverPicture({
                    'book-id': bookId,
                    body: {
                        file: this.selectedBookCover
                    }
                }).subscribe({
                    next: () => {
                        this.router.navigate(['/books/my-books']);
                    }
                });
            },
            error: (err) => {
                this.errorMessages = err.error.validationErrors;
            }
        });
    }
}
