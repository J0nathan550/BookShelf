export interface LendingRecordDto {
  id: number;
  lenderUserId: string;
  lenderName?: string;
  borrowerName: string;
  lendingDate: string;
  returnDate?: string;
  isReturned: boolean;
}

export interface ReadingStatusDto {
  status: string;
  rating?: number;
  completionDate?: string;
}

export interface BookNoteDto {
  id: number;
  noteText: string;
  createdDate: string;
  modifiedDate?: string;
  authorName?: string;
}

export interface BookGenreDto {
  id: number;
  name: string;
}

export interface BookFormatDto {
  id: number;
  name: string;
}

export interface BookDto {
  id: number;
  title: string;
  author: string;
  genre?: BookGenreDto;
  format?: BookFormatDto;
  pages?: number;
  coverImageUrl?: string;
  dateAdded: string;
  isApproved: boolean;
  readingStatus?: ReadingStatusDto;
  averageRating?: number;
  lendingRecord?: LendingRecordDto;
  lendingHistory?: LendingRecordDto[];
  notes?: BookNoteDto[];
  submittedByName?: string;
}

export interface CreateBookDto {
  title: string;
  author: string;
  genreId?: number;
  formatId?: number;
  pages?: number;
  coverImageUrl?: string;
}

export interface UpdateBookDto {
  title: string;
  author: string;
  genreId?: number;
  formatId?: number;
  pages?: number;
  coverImageUrl?: string;
}

export interface UpdateReadingStatusDto {
  status: string;
  rating?: number;
  completionDate?: string;
}

export interface NoteTextDto {
  noteText: string;
}

export interface GenreDto {
  id: number;
  name: string;
}

export interface FormatDto {
  id: number;
  name: string;
}

export interface IsbnLookupDto {
  title?: string;
  author?: string;
  pages?: number;
  coverImageUrl?: string;
}
